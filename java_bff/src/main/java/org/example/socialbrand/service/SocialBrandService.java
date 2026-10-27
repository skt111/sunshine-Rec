package org.example.socialbrand.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.socialbrand.dto.RankingItemVO;
import org.example.socialbrand.dto.RankingVO;
import org.example.socialbrand.dto.SocialAccountVO;
import org.example.socialbrand.dto.SocialMetricSnapshotVO;
import org.example.socialbrand.dto.SocialNoteDetailVO;
import org.example.socialbrand.dto.SocialNoteVO;
import org.example.socialbrand.dto.SocialStatsVO;
import org.example.socialbrand.entity.SocialAccount;
import org.example.socialbrand.entity.SocialNote;
import org.example.socialbrand.entity.SocialNoteMetricSnapshot;
import org.example.socialbrand.mapper.SocialAccountMapper;
import org.example.socialbrand.mapper.SocialNoteMapper;
import org.example.socialbrand.mapper.SocialNoteMetricSnapshotMapper;
import org.example.socialbrand.query.SocialNoteQuery;
import org.example.supplyradar.entity.TargetCompany;
import org.example.supplyradar.mapper.TargetCompanyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 雇主品牌（重点企业公开笔记）查询服务。
 *
 * <p>口径约定：</p>
 * <ul>
 *   <li>所有数字来自库内真实数据，无数据即 0，不回填任何模拟值；</li>
 *   <li>指标缺失一律为 null，不参与排名、不按 0 计入；</li>
 *   <li>综合分 = 点赞 * w1 + 收藏 * w2 + 评论 * w3，权重可由 application.yml 覆盖，
 *       该公式为<b>业务候选口径</b>，非平台官方标准。</li>
 * </ul>
 */
@Service
public class SocialBrandService {

    private static final Logger log = LoggerFactory.getLogger(SocialBrandService.class);

    public static final String PLATFORM = "XIAOHONGSHU";

    /** 主题标签白名单（与方案 5.4 一致） */
    public static final List<String> TOPIC_TAGS = Collections.unmodifiableList(Arrays.asList(
            "校园招聘", "秋招/春招/补录", "实习招聘", "岗位与专业要求", "薪酬福利", "职业发展",
            "企业文化", "员工体验", "面试与求职经验", "人才培养", "工作环境", "其他", "待分类"));

    /** 阶段一人工录入量级小，内存排序上限；接自动同步后改 SQL 分页 */
    private static final int MAX_SCAN = 2000;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final SocialNoteMapper noteMapper;
    private final SocialAccountMapper accountMapper;
    private final SocialNoteMetricSnapshotMapper snapshotMapper;
    private final TargetCompanyMapper companyMapper;

    @Value("${social-brand.composite-score.like:1}")
    private int scoreLike;
    @Value("${social-brand.composite-score.favorite:2}")
    private int scoreFavorite;
    @Value("${social-brand.composite-score.comment:3}")
    private int scoreComment;
    @Value("${social-brand.min-sample-size:10}")
    private int minSampleSize;
    @Value("${social-brand.high-interaction-percentile:0.8}")
    private double highInteractionPercentile;

    public SocialBrandService(SocialNoteMapper noteMapper,
                              SocialAccountMapper accountMapper,
                              SocialNoteMetricSnapshotMapper snapshotMapper,
                              TargetCompanyMapper companyMapper) {
        this.noteMapper = noteMapper;
        this.accountMapper = accountMapper;
        this.snapshotMapper = snapshotMapper;
        this.companyMapper = companyMapper;
    }

    // ==================== 统计 ====================

    /**
     * 统计卡数据。
     */
    public SocialStatsVO stats(LocalDate start, LocalDate end) {
        SocialStatsVO vo = new SocialStatsVO();
        List<TargetCompany> companies = enabledCompanies();
        List<SocialAccount> accounts = allAccounts();

        Set<Long> verifiedCompanyIds = new HashSet<>();
        int enabledAccounts = 0;
        int verifiedAccounts = 0;
        int pendingAccounts = 0;
        for (SocialAccount a : accounts) {
            if (a.getEnabled() != null && a.getEnabled() == 1) {
                enabledAccounts++;
                if ("VERIFIED".equals(a.getVerifiedStatus())) {
                    verifiedAccounts++;
                    verifiedCompanyIds.add(a.getCompanyId());
                } else if ("PENDING".equals(a.getVerifiedStatus())) {
                    pendingAccounts++;
                }
            }
        }

        List<SocialNote> notes = notesInRange(start, end, null);
        Map<Long, SocialNoteMetricSnapshot> latest = latestSnapshots(idsOf(notes));

        List<Long> scores = new ArrayList<>();
        for (SocialNote n : notes) {
            Long s = compositeScore(latest.get(n.getId()));
            if (s != null) {
                scores.add(s);
            }
        }
        vo.setHighInteractionInsufficient(scores.size() < minSampleSize);
        if (!vo.isHighInteractionInsufficient() && !scores.isEmpty()) {
            Collections.sort(scores);
            int idx = (int) Math.floor(scores.size() * highInteractionPercentile);
            if (idx >= scores.size()) {
                idx = scores.size() - 1;
            }
            long threshold = scores.get(idx);
            int high = 0;
            for (Long s : scores) {
                if (s >= threshold) {
                    high++;
                }
            }
            vo.setHighInteractionCount(high);
        }

        int pendingNotes = 0;
        LocalDateTime lastObserved = null;
        for (SocialNote n : notes) {
            if ("PENDING".equals(n.getVerificationStatus())) {
                pendingNotes++;
            }
            if (n.getLastObservedAt() != null && (lastObserved == null || n.getLastObservedAt().isAfter(lastObserved))) {
                lastObserved = n.getLastObservedAt();
            }
        }

        vo.setVerifiedCompanyCount(verifiedCompanyIds.size());
        vo.setEnabledAccountCount(enabledAccounts);
        vo.setVerifiedAccountCount(verifiedAccounts);
        vo.setPendingAccountCount(pendingAccounts);
        vo.setNoteCount(notes.size());
        vo.setPendingNoteCount(pendingNotes);
        vo.setRangeStart(start == null ? "" : start.format(DATE_FMT));
        vo.setRangeEnd(end == null ? "" : end.format(DATE_FMT));
        vo.setLastObservedAt(lastObserved);
        vo.setSourceNote("数据来源：人工录入的公开笔记快照 · 非实时抓取 · 指标以录入时页面可见为准");
        log.debug("统计完成：企业{} 笔记{} 账号{}", companies.size(), notes.size(), accounts.size());
        return vo;
    }

    // ==================== 账号 ====================

    /**
     * 账号列表（仅启用账号；含核验状态）。
     */
    public List<SocialAccountVO> accounts(Long companyId, String companyCategory, String nature, String verifiedStatus) {
        Map<Long, TargetCompany> companyMap = companyMap();
        QueryWrapper<SocialAccount> w = new QueryWrapper<>();
        w.eq("enabled", 1);
        if (companyId != null) {
            w.eq("company_id", companyId);
        }
        if (nature != null && !nature.trim().isEmpty()) {
            w.eq("account_nature", nature.trim());
        }
        if (verifiedStatus != null && !verifiedStatus.trim().isEmpty()) {
            w.eq("verified_status", verifiedStatus.trim());
        }
        w.orderByAsc("company_id").orderByAsc("id");

        List<SocialAccount> rows = accountMapper.selectList(w);
        List<SocialAccountVO> list = new ArrayList<>();
        for (SocialAccount a : rows) {
            TargetCompany c = companyMap.get(a.getCompanyId());
            if (c == null) {
                continue;
            }
            if (companyCategory != null && !companyCategory.trim().isEmpty()
                    && !"ALL".equalsIgnoreCase(companyCategory)
                    && !companyCategory.trim().equalsIgnoreCase(c.getCompanyCategory())) {
                continue;
            }
            list.add(toAccountVO(a, c));
        }
        return list;
    }

    // ==================== 笔记列表 ====================

    /**
     * 笔记分页列表（内部内存排序 + 切片，返回结构与供给端雷达一致）。
     */
    public Map<String, Object> notes(SocialNoteQuery q) {
        int page = Math.max(1, q.getPage());
        int size = Math.min(100, Math.max(1, q.getSize()));

        QueryWrapper<SocialNote> w = new QueryWrapper<>();
        applyFilters(w, q);
        w.last("limit " + MAX_SCAN);
        List<SocialNote> rows = noteMapper.selectList(w);

        Map<Long, TargetCompany> companyMap = companyMap();
        Map<Long, SocialAccount> accountMap = accountMap();
        Map<Long, SocialNoteMetricSnapshot> latest = latestSnapshots(idsOf(rows));

        List<SocialNoteVO> list = new ArrayList<>();
        for (SocialNote n : rows) {
            if (q.getMetricType() != null && !q.getMetricType().trim().isEmpty()) {
                if (metricValue(latest.get(n.getId()), q.getMetricType()) == null) {
                    continue;
                }
            }
            list.add(toNoteVO(n, companyMap.get(n.getCompanyId()),
                    n.getAccountId() == null ? null : accountMap.get(n.getAccountId()),
                    latest.get(n.getId())));
        }

        sortNotes(list, q);
        long total = list.size();
        int from = Math.min((page - 1) * size, list.size());
        int to = Math.min(from + size, list.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", new ArrayList<>(list.subList(from, to)));
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", size <= 0 ? 0 : (total + size - 1) / size);
        return result;
    }

    /**
     * 笔记详情（含全部快照）。
     */
    public SocialNoteDetailVO detail(Long id) {
        SocialNote n = noteMapper.selectById(id);
        if (n == null) {
            return null;
        }
        TargetCompany c = companyMap().get(n.getCompanyId());
        SocialAccount a = n.getAccountId() == null ? null : accountMapper.selectById(n.getAccountId());

        QueryWrapper<SocialNoteMetricSnapshot> sw = new QueryWrapper<>();
        sw.eq("note_id", id).orderByDesc("metric_observed_at").orderByDesc("id");
        List<SocialNoteMetricSnapshot> snapshots = snapshotMapper.selectList(sw);

        SocialNoteDetailVO vo = new SocialNoteDetailVO();
        copyNote(n, c, a, snapshots.isEmpty() ? null : snapshots.get(0), vo);
        for (SocialNoteMetricSnapshot s : snapshots) {
            vo.getSnapshots().add(toSnapshotVO(s));
        }
        return vo;
    }

    // ==================== 榜单 ====================

    /**
     * 榜单：like / favorite / comment / composite / topic。
     */
    public RankingVO rankings(String type, LocalDate start, LocalDate end, Long companyId, Integer limit) {
        String t = type == null || type.trim().isEmpty() ? "composite" : type.trim();
        int max = limit == null ? 10 : Math.max(1, Math.min(50, limit));

        List<SocialNote> notes = notesInRange(start, end, companyId);
        Map<Long, SocialNoteMetricSnapshot> latest = latestSnapshots(idsOf(notes));
        Map<Long, TargetCompany> companyMap = companyMap();

        RankingVO vo = new RankingVO();
        vo.setType(t);
        vo.setRange(rangeText(start, end));
        vo.setTotalNotes(notes.size());
        vo.setMetricSource(metricSourceOf(notes, latest));

        if ("topic".equals(t)) {
            Map<String, Integer> counter = new HashMap<>();
            for (SocialNote n : notes) {
                for (String tag : splitTags(n.getTopicTags())) {
                    counter.merge(tag, 1, Integer::sum);
                }
            }
            List<RankingItemVO> items = new ArrayList<>();
            for (Map.Entry<String, Integer> e : counter.entrySet()) {
                RankingItemVO item = new RankingItemVO();
                item.setTopic(e.getKey());
                item.setTitle(e.getKey());
                item.setMetricValue((long) e.getValue());
                items.add(item);
            }
            items.sort(Comparator.comparing(RankingItemVO::getMetricValue).reversed());
            vo.setAvailable(!items.isEmpty());
            vo.setReason(items.isEmpty() ? "当前范围内没有已标注主题的笔记" : "");
            vo.setSampleSize(items.size());
            vo.setCompleteness(items.size() + "/" + notes.size());
            vo.setInsufficient(notes.size() < minSampleSize);
            vo.setItems(items.size() > max ? new ArrayList<>(items.subList(0, max)) : items);
            return vo;
        }

        boolean composite = "composite".equals(t);
        boolean favorite = "favorite".equals(t);

        List<RankingItemVO> items = new ArrayList<>();
        int metricAvailable = 0;
        for (SocialNote n : notes) {
            SocialNoteMetricSnapshot s = latest.get(n.getId());
            Long value;
            Long score = null;
            if (composite) {
                score = compositeScore(s);
                value = score;
            } else {
                value = metricValue(s, t);
            }
            if (value == null) {
                continue;
            }
            metricAvailable++;
            RankingItemVO item = new RankingItemVO();
            item.setNoteId(n.getId());
            item.setTitle(n.getTitle());
            item.setNoteUrl(n.getNoteUrl());
            item.setMetricValue(value);
            item.setScore(score);
            item.setMetricObservedAt(s == null ? null : s.getMetricObservedAt());
            TargetCompany c = companyMap.get(n.getCompanyId());
            item.setCompanyName(c == null ? "" : c.getCompanyName());
            items.add(item);
        }

        items.sort(Comparator.comparing(RankingItemVO::getMetricValue).reversed());
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setRank(i + 1);
        }

        vo.setSampleSize(metricAvailable);
        vo.setCompleteness(metricAvailable + "/" + notes.size());
        vo.setInsufficient(metricAvailable < minSampleSize);
        vo.setMetricSource(metricSourceOf(notes, latest));
        if (metricAvailable == 0) {
            vo.setAvailable(false);
            vo.setReason(composite
                    ? "当前范围内没有同时具备点赞/收藏/评论的笔记，综合互动榜暂不可出"
                    : (favorite ? "当前范围内没有笔记提供收藏数，收藏榜暂不可出" : "当前范围内没有可用的该指标数据"));
        } else {
            vo.setAvailable(true);
            vo.setReason("");
        }
        vo.setItems(items.size() > max ? new ArrayList<>(items.subList(0, max)) : items);
        return vo;
    }

    // ==================== 内部方法 ====================

    private void applyFilters(QueryWrapper<SocialNote> w, SocialNoteQuery q) {
        if (q.getCompanyIds() != null && !q.getCompanyIds().isEmpty()) {
            w.in("company_id", q.getCompanyIds());
        }
        if (q.getCompanyCategory() != null && !q.getCompanyCategory().trim().isEmpty()
                && !"ALL".equalsIgnoreCase(q.getCompanyCategory())) {
            List<Long> ids = new ArrayList<>();
            for (TargetCompany c : enabledCompanies()) {
                if (q.getCompanyCategory().trim().equalsIgnoreCase(c.getCompanyCategory())) {
                    ids.add(c.getId());
                }
            }
            if (ids.isEmpty()) {
                w.eq("id", -1L);
            } else {
                w.in("company_id", ids);
            }
        }
        if (q.getAccountNature() != null && !q.getAccountNature().trim().isEmpty()) {
            List<Long> ids = new ArrayList<>();
            for (SocialAccount a : allAccounts()) {
                if (q.getAccountNature().trim().equalsIgnoreCase(a.getAccountNature())) {
                    ids.add(a.getId());
                }
            }
            if (ids.isEmpty()) {
                w.eq("id", -1L);
            } else {
                w.in("account_id", ids);
            }
        }
        if (q.getTopic() != null && !q.getTopic().trim().isEmpty()) {
            w.like("topic_tags", q.getTopic().trim());
        }
        if (q.getStartDate() != null) {
            w.ge("published_at", q.getStartDate().atStartOfDay());
        }
        if (q.getEndDate() != null) {
            w.le("published_at", q.getEndDate().atTime(LocalTime.MAX));
        }
        if (q.getSourceType() != null && !q.getSourceType().trim().isEmpty()) {
            w.eq("source_type", q.getSourceType().trim());
        }
        if (q.getVerificationStatus() != null && !q.getVerificationStatus().trim().isEmpty()) {
            w.eq("verification_status", q.getVerificationStatus().trim());
        }
        if (q.getOwnerType() != null && !q.getOwnerType().trim().isEmpty()) {
            w.eq("owner_type", q.getOwnerType().trim());
        }
        if (q.getKeyword() != null && !q.getKeyword().trim().isEmpty()) {
            String kw = q.getKeyword().trim();
            w.and(x -> x.like("title", kw).or().like("content_summary", kw));
        }
    }

    private void sortNotes(List<SocialNoteVO> list, SocialNoteQuery q) {
        String sortBy = q.getSortBy() == null ? "publishedAt" : q.getSortBy();
        boolean asc = "asc".equalsIgnoreCase(q.getSortDirection());
        Comparator<SocialNoteVO> cmp;
        if ("like".equals(sortBy) || "favorite".equals(sortBy) || "comment".equals(sortBy)) {
            cmp = Comparator.comparing((SocialNoteVO v) -> metricOf(v, sortBy),
                    Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("score".equals(sortBy)) {
            cmp = Comparator.comparing(this::scoreOf, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            cmp = Comparator.comparing(SocialNoteVO::getPublishedAt, Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if (!asc) {
            cmp = cmp.reversed();
        }
        list.sort(cmp.thenComparing(SocialNoteVO::getId, Comparator.nullsLast(Comparator.reverseOrder())));
    }

    private Long metricOf(SocialNoteVO v, String type) {
        if (v.getMetrics() == null) {
            return null;
        }
        switch (type) {
            case "like":
                return v.getMetrics().getLikeCount() == null ? null : v.getMetrics().getLikeCount().longValue();
            case "favorite":
                return v.getMetrics().getFavoriteCount() == null ? null : v.getMetrics().getFavoriteCount().longValue();
            case "comment":
                return v.getMetrics().getCommentCount() == null ? null : v.getMetrics().getCommentCount().longValue();
            default:
                return null;
        }
    }

    private Long scoreOf(SocialNoteVO v) {
        return compositeScore(v.getMetrics());
    }

    private Long compositeScore(SocialMetricSnapshotVO m) {
        if (m == null || m.getLikeCount() == null || m.getFavoriteCount() == null || m.getCommentCount() == null) {
            return null;
        }
        return (long) m.getLikeCount() * scoreLike
                + (long) m.getFavoriteCount() * scoreFavorite
                + (long) m.getCommentCount() * scoreComment;
    }

    private Long compositeScore(SocialNoteMetricSnapshot s) {
        if (s == null || s.getLikeCount() == null || s.getFavoriteCount() == null || s.getCommentCount() == null) {
            return null;
        }
        return (long) s.getLikeCount() * scoreLike
                + (long) s.getFavoriteCount() * scoreFavorite
                + (long) s.getCommentCount() * scoreComment;
    }

    private Long metricValue(SocialNoteMetricSnapshot s, String type) {
        if (s == null) {
            return null;
        }
        switch (type) {
            case "like":
                return s.getLikeCount() == null ? null : s.getLikeCount().longValue();
            case "favorite":
                return s.getFavoriteCount() == null ? null : s.getFavoriteCount().longValue();
            case "comment":
                return s.getCommentCount() == null ? null : s.getCommentCount().longValue();
            case "share":
                return s.getShareCount() == null ? null : s.getShareCount().longValue();
            case "view":
                return s.getViewCount() == null ? null : s.getViewCount().longValue();
            default:
                return null;
        }
    }

    private List<SocialNote> notesInRange(LocalDate start, LocalDate end, Long companyId) {
        QueryWrapper<SocialNote> w = new QueryWrapper<>();
        if (start != null) {
            w.ge("published_at", start.atStartOfDay());
        }
        if (end != null) {
            w.le("published_at", end.atTime(LocalTime.MAX));
        }
        if (companyId != null) {
            w.eq("company_id", companyId);
        }
        w.orderByDesc("published_at").orderByDesc("id");
        w.last("limit " + MAX_SCAN);
        List<SocialNote> rows = noteMapper.selectList(w);
        return rows == null ? new ArrayList<>() : rows;
    }

    private Map<Long, SocialNoteMetricSnapshot> latestSnapshots(List<Long> noteIds) {
        Map<Long, SocialNoteMetricSnapshot> map = new HashMap<>();
        if (noteIds == null || noteIds.isEmpty()) {
            return map;
        }
        int step = 500;
        for (int i = 0; i < noteIds.size(); i += step) {
            List<Long> chunk = noteIds.subList(i, Math.min(i + step, noteIds.size()));
            QueryWrapper<SocialNoteMetricSnapshot> w = new QueryWrapper<>();
            w.in("note_id", chunk).orderByDesc("metric_observed_at").orderByDesc("id");
            List<SocialNoteMetricSnapshot> rows = snapshotMapper.selectList(w);
            for (SocialNoteMetricSnapshot s : rows) {
                map.putIfAbsent(s.getNoteId(), s);
            }
        }
        return map;
    }

    private List<Long> idsOf(List<SocialNote> notes) {
        List<Long> ids = new ArrayList<>(notes.size());
        for (SocialNote n : notes) {
            ids.add(n.getId());
        }
        return ids;
    }

    private String rangeText(LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return "不限时间";
        }
        return (start == null ? "不限" : start.format(DATE_FMT)) + " ~ " + (end == null ? "不限" : end.format(DATE_FMT));
    }

    private String metricSourceOf(List<SocialNote> notes, Map<Long, SocialNoteMetricSnapshot> latest) {
        Set<String> sources = new LinkedHashSet<>();
        for (SocialNote n : notes) {
            SocialNoteMetricSnapshot s = latest.get(n.getId());
            if (s != null && s.getMetricSource() != null) {
                sources.add(s.getMetricSource());
            }
        }
        if (sources.isEmpty()) {
            return "";
        }
        if (sources.size() == 1) {
            return sources.iterator().next();
        }
        return "多来源（" + sources.size() + "）";
    }

    private List<TargetCompany> enabledCompanies() {
        QueryWrapper<TargetCompany> w = new QueryWrapper<>();
        w.eq("enabled", 1).orderByAsc("company_category").orderByAsc("id");
        List<TargetCompany> rows = companyMapper.selectList(w);
        return rows == null ? new ArrayList<>() : rows;
    }

    private Map<Long, TargetCompany> companyMap() {
        Map<Long, TargetCompany> map = new HashMap<>();
        for (TargetCompany c : enabledCompanies()) {
            map.put(c.getId(), c);
        }
        return map;
    }

    private List<SocialAccount> allAccounts() {
        List<SocialAccount> rows = accountMapper.selectList(new QueryWrapper<>());
        return rows == null ? new ArrayList<>() : rows;
    }

    private Map<Long, SocialAccount> accountMap() {
        Map<Long, SocialAccount> map = new HashMap<>();
        for (SocialAccount a : allAccounts()) {
            map.put(a.getId(), a);
        }
        return map;
    }

    private SocialAccountVO toAccountVO(SocialAccount a, TargetCompany c) {
        SocialAccountVO vo = new SocialAccountVO();
        vo.setId(a.getId());
        vo.setCompanyId(a.getCompanyId());
        vo.setCompanyName(c.getCompanyName());
        vo.setCompanyCategory(c.getCompanyCategory());
        vo.setAccountName(a.getAccountName());
        vo.setHomeUrl(a.getHomeUrl());
        vo.setAccountNature(a.getAccountNature());
        vo.setVerifiedStatus(a.getVerifiedStatus());
        vo.setVerifyBasis(a.getVerifyBasis());
        vo.setVerifiedAt(a.getVerifiedAt());
        vo.setCompanyConfidence(a.getCompanyConfidence());
        vo.setEnabled(a.getEnabled());
        return vo;
    }

    private void copyNote(SocialNote n, TargetCompany c, SocialAccount a,
                          SocialNoteMetricSnapshot latest, SocialNoteVO vo) {
        vo.setId(n.getId());
        vo.setNoteUrl(n.getNoteUrl());
        vo.setTitle(n.getTitle());
        vo.setContentSummary(n.getContentSummary());
        vo.setPublishedAt(n.getPublishedAt());
        vo.setFirstObservedAt(n.getFirstObservedAt());
        vo.setLastObservedAt(n.getLastObservedAt());
        vo.setCompanyId(n.getCompanyId());
        vo.setCompanyName(c == null ? "" : c.getCompanyName());
        vo.setCompanyCategory(c == null ? "" : c.getCompanyCategory());
        vo.setAccountId(n.getAccountId());
        vo.setAccountName(a == null ? "" : a.getAccountName());
        vo.setAccountNature(a == null ? "UNKNOWN" : a.getAccountNature());
        vo.setAccountVerifiedStatus(a == null ? "" : a.getVerifiedStatus());
        vo.setAccountVerified(a != null && "VERIFIED".equals(a.getVerifiedStatus()));
        vo.setOwnerType(n.getOwnerType());
        vo.setSourceType(n.getSourceType());
        vo.setVerificationStatus(n.getVerificationStatus());
        vo.setTopicTags(splitTags(n.getTopicTags()));
        vo.setMissingReason(n.getMissingReason());
        vo.setCleanSource(n.getCleanSource());
        vo.setCleanBasis(n.getCleanBasis());
        vo.setCleanedAt(n.getCleanedAt());
        vo.setRemark(n.getRemark());
        vo.setMetrics(latest == null ? null : toSnapshotVO(latest));
    }

    private SocialNoteVO toNoteVO(SocialNote n, TargetCompany c, SocialAccount a, SocialNoteMetricSnapshot latest) {
        SocialNoteVO vo = new SocialNoteVO();
        copyNote(n, c, a, latest, vo);
        return vo;
    }

    private SocialMetricSnapshotVO toSnapshotVO(SocialNoteMetricSnapshot s) {
        SocialMetricSnapshotVO vo = new SocialMetricSnapshotVO();
        vo.setId(s.getId());
        vo.setLikeCount(s.getLikeCount());
        vo.setFavoriteCount(s.getFavoriteCount());
        vo.setCommentCount(s.getCommentCount());
        vo.setShareCount(s.getShareCount());
        vo.setViewCount(s.getViewCount());
        vo.setMetricObservedAt(s.getMetricObservedAt());
        vo.setMetricSource(s.getMetricSource());
        if (s.getLikeCount() == null) {
            vo.getMissingFields().add("点赞");
        }
        if (s.getFavoriteCount() == null) {
            vo.getMissingFields().add("收藏");
        }
        if (s.getCommentCount() == null) {
            vo.getMissingFields().add("评论");
        }
        if (s.getShareCount() == null) {
            vo.getMissingFields().add("分享");
        }
        if (s.getViewCount() == null) {
            vo.getMissingFields().add("浏览");
        }
        return vo;
    }

    private List<String> splitTags(String raw) {
        List<String> list = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return list;
        }
        for (String part : raw.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) {
                list.add(t);
            }
        }
        return list;
    }
}
