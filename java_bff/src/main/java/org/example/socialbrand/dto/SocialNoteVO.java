package org.example.socialbrand.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 公开笔记列表项。
 */
@Data
public class SocialNoteVO {

    private Long id;
    private String noteUrl;
    private String title;
    private String contentSummary;
    private LocalDateTime publishedAt;
    private LocalDateTime firstObservedAt;
    private LocalDateTime lastObservedAt;
    private Long companyId;
    private String companyName;
    private String companyCategory;
    private Long accountId;
    private String accountName;
    private String accountNature;
    private String accountVerifiedStatus;
    private Boolean accountVerified;
    /** OFFICIAL / USER_DISCUSSION / UNKNOWN */
    private String ownerType;
    /** MANUAL / AUTHORIZED_API / AUTHORIZED_SERVICE / PUBLIC_PAGE */
    private String sourceType;
    /** VERIFIED / PENDING / FAILED */
    private String verificationStatus;
    private List<String> topicTags = new ArrayList<>();
    private String missingReason;
    /** LLM / RULE / null（未清洗） */
    private String cleanSource;
    private String cleanBasis;
    private LocalDateTime cleanedAt;
    private String remark;
    /** 最近一次合法快照，null 表示尚无指标 */
    private SocialMetricSnapshotVO metrics;
}
