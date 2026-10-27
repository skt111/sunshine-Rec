package org.example.socialbrand.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.socialbrand.dto.ManualNoteRequest;
import org.example.socialbrand.entity.SocialAccount;
import org.example.socialbrand.entity.SocialNote;
import org.example.socialbrand.entity.SocialNoteMetricSnapshot;
import org.example.socialbrand.mapper.SocialAccountMapper;
import org.example.socialbrand.mapper.SocialNoteMapper;
import org.example.socialbrand.mapper.SocialNoteMetricSnapshotMapper;
import org.example.supplyradar.entity.TargetCompany;
import org.example.supplyradar.mapper.TargetCompanyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/**
 * 人工录入写入服务。
 *
 * <p>只接受"公开页面上能看见的信息"，并对链接、企业归属、指标与枚举做校验；
 * 任何一项不合法都直接拒绝，不做"猜测补齐"。</p>
 */
@Service
public class SocialNoteWriteService {

    private static final Logger log = LoggerFactory.getLogger(SocialNoteWriteService.class);

    private static final List<String> OWNER_TYPES = Arrays.asList("OFFICIAL", "USER_DISCUSSION", "UNKNOWN");
    private static final List<String> VERIFY_STATUS = Arrays.asList("VERIFIED", "PENDING", "FAILED");
    private static final DateTimeFormatter SOURCE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final SocialNoteMapper noteMapper;
    private final SocialNoteMetricSnapshotMapper snapshotMapper;
    private final SocialAccountMapper accountMapper;
    private final TargetCompanyMapper companyMapper;

    public SocialNoteWriteService(SocialNoteMapper noteMapper,
                                  SocialNoteMetricSnapshotMapper snapshotMapper,
                                  SocialAccountMapper accountMapper,
                                  TargetCompanyMapper companyMapper) {
        this.noteMapper = noteMapper;
        this.snapshotMapper = snapshotMapper;
        this.accountMapper = accountMapper;
        this.companyMapper = companyMapper;
    }

    /**
     * 新建一条人工录入笔记，并在有指标时写入首条快照。
     *
     * @return 新笔记 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createManual(ManualNoteRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        String url = trim(req.getNoteUrl());
        if (url.isEmpty()) {
            throw new IllegalArgumentException("笔记链接不能为空");
        }
        validateUrl(url);

        String title = trim(req.getTitle());
        if (title.isEmpty()) {
            throw new IllegalArgumentException("标题不能为空（录入时以页面标题为准）");
        }
        if (title.length() > 500) {
            throw new IllegalArgumentException("标题过长（上限 500 字）");
        }

        Long companyId = req.getCompanyId();
        if (companyId == null) {
            throw new IllegalArgumentException("必须选择关联企业");
        }
        TargetCompany company = companyMapper.selectById(companyId);
        if (company == null || company.getEnabled() == null || company.getEnabled() != 1) {
            throw new IllegalArgumentException("企业不存在或未启用");
        }

        Long accountId = req.getAccountId();
        if (accountId != null) {
            SocialAccount account = accountMapper.selectById(accountId);
            if (account == null) {
                throw new IllegalArgumentException("账号不存在");
            }
            if (!companyId.equals(account.getCompanyId())) {
                throw new IllegalArgumentException("账号与企业不匹配");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        if (req.getPublishedAt() != null && req.getPublishedAt().isAfter(now.plusDays(1))) {
            throw new IllegalArgumentException("发布时间不能晚于当前时间");
        }

        checkMetric(req.getLikeCount(), "点赞数");
        checkMetric(req.getFavoriteCount(), "收藏数");
        checkMetric(req.getCommentCount(), "评论数");
        checkMetric(req.getShareCount(), "分享数");
        checkMetric(req.getViewCount(), "浏览量");

        boolean hasMetric = req.getLikeCount() != null || req.getFavoriteCount() != null
                || req.getCommentCount() != null || req.getShareCount() != null || req.getViewCount() != null;

        // 同一链接再次观察：不新增笔记，追加一条指标快照（保留历史）
        SocialNote existing = noteMapper.selectOne(new QueryWrapper<SocialNote>().eq("note_url", url));
        if (existing != null) {
            if (!hasMetric) {
                throw new IllegalArgumentException("该链接已录入（本次未提供新指标，不重复写入）");
            }
            insertSnapshot(existing.getId(), req, now);
            // 详情页补录时允许回填摘要（不覆盖已清洗过的标题）
            String summary = trimToNull(req.getContentSummary(), 1000, "摘要");
            if (summary != null) {
                existing.setContentSummary(summary);
            }
            existing.setLastObservedAt(now);
            existing.setUpdatedAt(now);
            noteMapper.updateById(existing);
            log.info("同一链接追加快照 noteId={} company={}", existing.getId(), existing.getCompanyId());
            return existing.getId();
        }

        String ownerType = trim(req.getOwnerType()).isEmpty() ? "UNKNOWN" : trim(req.getOwnerType()).toUpperCase(Locale.ROOT);
        if (!OWNER_TYPES.contains(ownerType)) {
            throw new IllegalArgumentException("归属类型不合法（OFFICIAL / USER_DISCUSSION / UNKNOWN）");
        }
        String verifyStatus = trim(req.getVerificationStatus()).isEmpty()
                ? "PENDING" : trim(req.getVerificationStatus()).toUpperCase(Locale.ROOT);
        if (!VERIFY_STATUS.contains(verifyStatus)) {
            throw new IllegalArgumentException("核验状态不合法（VERIFIED / PENDING / FAILED）");
        }

        LinkedHashSet<String> tags = new LinkedHashSet<>();
        if (req.getTopicTags() != null) {
            for (String tag : req.getTopicTags()) {
                String t = trim(tag);
                if (t.isEmpty()) {
                    continue;
                }
                if (!SocialBrandService.TOPIC_TAGS.contains(t)) {
                    throw new IllegalArgumentException("主题标签不在允许范围内：" + t);
                }
                tags.add(t);
            }
        }

        SocialNote note = new SocialNote();
        note.setPlatform(SocialBrandService.PLATFORM);
        note.setNoteUrl(url);
        note.setTitle(title);
        note.setContentSummary(trimToNull(req.getContentSummary(), 1000, "摘要"));
        note.setPublishedAt(req.getPublishedAt());
        note.setFirstObservedAt(now);
        note.setLastObservedAt(now);
        note.setCompanyId(companyId);
        note.setAccountId(accountId);
        note.setOwnerType(ownerType);
        // 阶段一只有人工录入通道，来源固定
        note.setSourceType("MANUAL");
        note.setVerificationStatus(verifyStatus);
        note.setTopicTags(tags.isEmpty() ? null : String.join(",", tags));
        String missingReason = trimToNull(req.getMissingReason(), 256, "缺失原因");
        if (missingReason == null && !hasMetric) {
            missingReason = "来源未提供互动指标";
        }
        note.setMissingReason(missingReason);
        note.setRemark(trimToNull(req.getRemark(), 512, "备注"));
        note.setCreatedAt(now);
        note.setUpdatedAt(now);
        noteMapper.insert(note);

        if (hasMetric) {
            insertSnapshot(note.getId(), req, now);
        }

        log.info("人工录入笔记 id={} company={} metrics={}", note.getId(), companyId, hasMetric);
        return note.getId();
    }

    private void insertSnapshot(Long noteId, ManualNoteRequest req, LocalDateTime now) {
        SocialNoteMetricSnapshot s = new SocialNoteMetricSnapshot();
        s.setNoteId(noteId);
        s.setLikeCount(req.getLikeCount());
        s.setFavoriteCount(req.getFavoriteCount());
        s.setCommentCount(req.getCommentCount());
        s.setShareCount(req.getShareCount());
        s.setViewCount(req.getViewCount());
        s.setMetricObservedAt(now);
        s.setMetricSource("MANUAL_" + now.format(SOURCE_FMT));
        s.setCreatedAt(now);
        snapshotMapper.insert(s);
    }

    private void validateUrl(String url) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("链接格式不合法");
        }
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme == null || host == null
                || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
            throw new IllegalArgumentException("链接必须是 http/https 完整地址");
        }
        String h = host.toLowerCase(Locale.ROOT);
        boolean ok = h.equals("xiaohongshu.com") || h.endsWith(".xiaohongshu.com")
                || h.equals("xhslink.com") || h.endsWith(".xhslink.com");
        if (!ok) {
            throw new IllegalArgumentException("仅支持小红书笔记链接（xiaohongshu.com / xhslink.com）");
        }
    }

    private void checkMetric(Integer value, String label) {
        if (value != null && value < 0) {
            throw new IllegalArgumentException(label + "必须为非负整数");
        }
    }

    private String trim(String v) {
        return v == null ? "" : v.trim();
    }

    private String trimToNull(String v, int max, String label) {
        String s = trim(v);
        if (s.isEmpty()) {
            return null;
        }
        if (s.length() > max) {
            throw new IllegalArgumentException(label + "过长（上限 " + max + " 字）");
        }
        return s;
    }
}
