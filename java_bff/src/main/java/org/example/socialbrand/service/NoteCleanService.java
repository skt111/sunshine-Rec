package org.example.socialbrand.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.service.LlmService;
import org.example.socialbrand.dto.CleanNotesRequest;
import org.example.socialbrand.entity.SocialNote;
import org.example.socialbrand.mapper.SocialNoteMapper;
import org.example.supplyradar.entity.TargetCompany;
import org.example.supplyradar.mapper.TargetCompanyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 大模型数据清洗。
 *
 * <p>清洗三件事：标题去噪（作者名/发布时间/点赞数）、归属类型判定、主题标签。
 * 原值写入 {@code title_raw}，输出写入 {@code title / owner_type / topic_tags}，
 * 并记录 {@code clean_source / clean_basis / cleaned_at} 便于追溯。</p>
 *
 * <p>大模型不可用或返回不合法时，自动降级为规则清洗，不会把脏数据写回。</p>
 */
@Service
public class NoteCleanService {

    private static final Logger log = LoggerFactory.getLogger(NoteCleanService.class);

    private static final Set<String> OWNER_TYPES = new LinkedHashSet<>(Arrays.asList("OFFICIAL", "USER_DISCUSSION", "UNKNOWN"));

    private static final String SYSTEM_PROMPT =
            "你是招聘情报的数据清洗助手，只输出 JSON，不要解释，不要加代码块标记。";

    private final SocialNoteMapper noteMapper;
    private final TargetCompanyMapper companyMapper;
    private final LlmService llmService;
    private final ObjectMapper objectMapper;

    @Value("${social-brand.clean.llm-enabled:true}")
    private boolean llmEnabled;

    @Value("${social-brand.clean.max-batch:20}")
    private int maxBatch;

    public NoteCleanService(SocialNoteMapper noteMapper,
                            TargetCompanyMapper companyMapper,
                            LlmService llmService,
                            ObjectMapper objectMapper) {
        this.noteMapper = noteMapper;
        this.companyMapper = companyMapper;
        this.llmService = llmService;
        this.objectMapper = objectMapper;
    }

    /**
     * 批量清洗。
     */
    public Map<String, Object> clean(CleanNotesRequest req) {
        List<SocialNote> notes = pick(req);
        if (notes.isEmpty()) {
            throw new IllegalArgumentException("没有待清洗的笔记（可勾选“含已清洗”）");
        }

        List<Map<String, Object>> items = new ArrayList<>(notes.size());
        int changed = 0;
        int failed = 0;

        for (SocialNote note : notes) {
            try {
                boolean ok = cleanOne(note);
                if (ok) {
                    changed++;
                }
                items.add(item(note, ok, null));
            } catch (Exception e) {
                failed++;
                log.warn("清洗失败 noteId={}: {}", note.getId(), e.getMessage());
                items.add(item(note, false, e.getMessage()));
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", notes.size());
        result.put("changed", changed);
        result.put("failed", failed);
        result.put("items", items);
        log.info("清洗完成 total={} changed={} failed={}", notes.size(), changed, failed);
        return result;
    }

    private List<SocialNote> pick(CleanNotesRequest req) {
        int limit = req == null || req.getLimit() == null ? 10 : Math.max(1, Math.min(maxBatch, req.getLimit()));
        QueryWrapper<SocialNote> w = new QueryWrapper<>();
        if (req != null && req.getIds() != null && !req.getIds().isEmpty()) {
            w.in("id", req.getIds());
        } else if (req != null && req.getCompanyId() != null) {
            w.eq("company_id", req.getCompanyId());
        }
        if (req == null || !Boolean.TRUE.equals(req.getIncludeCleaned())) {
            w.isNull("cleaned_at");
        }
        w.orderByDesc("id").last("limit " + limit);
        List<SocialNote> rows = noteMapper.selectList(w);
        return rows == null ? new ArrayList<>() : rows;
    }

    /**
     * 清洗单条；返回是否发生变更。
     */
    private boolean cleanOne(SocialNote note) {
        String raw = note.getTitleRaw() != null && !note.getTitleRaw().isEmpty()
                ? note.getTitleRaw() : note.getTitle();
        if (raw == null || raw.trim().isEmpty()) {
            return false;
        }
        String companyName = companyName(note.getCompanyId());

        CleanResult r = null;
        if (llmEnabled) {
            try {
                r = llmClean(raw, companyName, note.getContentSummary());
                r.source = "LLM";
            } catch (Exception e) {
                log.warn("大模型清洗失败，降级规则清洗 noteId={}: {}", note.getId(), e.getMessage());
            }
        }
        if (r == null) {
            r = ruleClean(raw);
            r.source = "RULE";
        }

        boolean changed = !eq(r.title, note.getTitle())
                || !eq(r.ownerType, note.getOwnerType())
                || !eq(r.tags, note.getTopicTags());

        if (note.getTitleRaw() == null || note.getTitleRaw().isEmpty()) {
            note.setTitleRaw(raw);
        }
        note.setTitle(r.title);
        // 已人工核验的归属不被模型覆盖
        if (!"VERIFIED".equals(note.getVerificationStatus()) && OWNER_TYPES.contains(r.ownerType)) {
            note.setOwnerType(r.ownerType);
        }
        note.setTopicTags(r.tags);
        note.setCleanSource(r.source);
        note.setCleanBasis(r.basis);
        note.setCleanedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.updateById(note);
        return changed;
    }

    private CleanResult llmClean(String rawTitle, String companyName, String summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("把小红书笔记标题清洗成干净标题：去掉作者名、发布时间、点赞数等噪声，保留原意；\n")
                .append("判断归属类型：OFFICIAL=企业官方账号发布，USER_DISCUSSION=个人/学生发布，UNKNOWN=无法判断；\n")
                .append("主题只能从这个列表里选 1-3 个：").append(String.join(",", SocialBrandService.TOPIC_TAGS)).append("\n")
                .append("只输出 JSON：{\"title\":\"\",\"ownerType\":\"\",\"topicTags\":[],\"confidence\":0.0}\n")
                .append("所属企业：").append(companyName.isEmpty() ? "未知" : companyName).append("\n")
                .append("原始标题：").append(rawTitle);
        if (summary != null && !summary.isEmpty()) {
            sb.append("\n摘要（参考，不要放进标题）：").append(summary.length() > 100 ? summary.substring(0, 100) : summary);
        }

        String reply = llmService.chat(sb.toString(), SYSTEM_PROMPT);
        JsonNode node = parseJson(reply);
        if (node == null) {
            throw new IllegalStateException("模型未返回可解析 JSON");
        }

        CleanResult r = new CleanResult();
        String title = node.path("title").asText("").trim();
        if (title.isEmpty() || title.length() > 200) {
            throw new IllegalStateException("模型返回标题不合法");
        }
        r.title = title;

        String owner = node.path("ownerType").asText("UNKNOWN").trim().toUpperCase(Locale.ROOT);
        r.ownerType = OWNER_TYPES.contains(owner) ? owner : "UNKNOWN";

        List<String> tags = new ArrayList<>();
        JsonNode arr = node.path("topicTags");
        if (arr.isArray()) {
            for (JsonNode t : arr) {
                String tag = t.asText("").trim();
                if (SocialBrandService.TOPIC_TAGS.contains(tag) && !tags.contains(tag)) {
                    tags.add(tag);
                }
            }
        }
        if (tags.isEmpty()) {
            tags.add("待分类");
        }
        r.tags = String.join(",", tags.subList(0, Math.min(3, tags.size())));

        double confidence = node.path("confidence").asDouble(0);
        r.basis = "LLM 置信度 " + String.format(Locale.ROOT, "%.2f", confidence);
        return r;
    }

    /**
     * 规则兜底：剥掉尾部点赞数与时间，按关键词打标签。
     */
    private CleanResult ruleClean(String raw) {
        String title = raw.trim();
        for (int i = 0; i < 3; i++) {
            String before = title;
            title = title.replaceAll("\\s+\\d+(\\.\\d+)?\\s*(万|w|W)?$", "");
            title = title.replaceAll("\\s+(刚刚|\\d+秒前|\\d+分钟前|\\d+小时前|\\d+天前|\\d+周前|昨天|今天|前天)$", "");
            title = title.replaceAll("\\s+\\d{1,2}-\\d{1,2}$", "");
            title = title.replaceAll("\\s+\\d{1,2}:\\d{2}$", "");
            if (before.equals(title)) {
                break;
            }
        }
        if (title.isEmpty()) {
            title = raw.trim();
        }

        CleanResult r = new CleanResult();
        r.title = title.length() > 200 ? title.substring(0, 200) : title;
        r.ownerType = "UNKNOWN";

        LinkedHashSet<String> tags = new LinkedHashSet<>();
        if (title.contains("面经") || title.contains("面试") || title.contains("笔试")) {
            tags.add("面试与求职经验");
        }
        if (title.contains("实习")) {
            tags.add("实习招聘");
        }
        if (title.contains("薪酬") || title.contains("薪资") || title.contains("待遇")) {
            tags.add("薪酬福利");
        }
        if (title.contains("秋招") || title.contains("春招") || title.contains("补录")) {
            tags.add("秋招/春招/补录");
        } else if (title.contains("校招") || title.contains("校园招聘")) {
            tags.add("校园招聘");
        }
        if (title.contains("培养") || title.contains("管培")) {
            tags.add("人才培养");
        }
        if (tags.isEmpty()) {
            tags.add("待分类");
        }
        r.tags = String.join(",", new ArrayList<>(tags).subList(0, Math.min(3, tags.size())));
        r.basis = "规则兜底";
        return r;
    }

    private JsonNode parseJson(String reply) {
        if (reply == null) {
            return null;
        }
        String s = reply.trim();
        if (s.startsWith("```")) {
            s = s.replaceAll("^```[a-zA-Z]*", "").replaceAll("```$", "").trim();
        }
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        try {
            return objectMapper.readTree(s.substring(start, end + 1));
        } catch (Exception e) {
            log.warn("模型 JSON 解析失败: {}", s.substring(0, Math.min(120, s.length())));
            return null;
        }
    }

    private String companyName(Long companyId) {
        if (companyId == null) {
            return "";
        }
        TargetCompany c = companyMapper.selectById(companyId);
        return c == null ? "" : c.getCompanyName();
    }

    private Map<String, Object> item(SocialNote note, boolean changed, String error) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", note.getId());
        m.put("changed", changed);
        m.put("beforeTitle", note.getTitleRaw() == null ? note.getTitle() : note.getTitleRaw());
        m.put("afterTitle", note.getTitle());
        m.put("ownerType", note.getOwnerType());
        m.put("topicTags", note.getTopicTags());
        m.put("source", note.getCleanSource());
        m.put("basis", note.getCleanBasis());
        m.put("error", error);
        return m;
    }

    private boolean eq(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private static final class CleanResult {
        String title;
        String ownerType;
        String tags;
        String source;
        String basis;
    }
}
