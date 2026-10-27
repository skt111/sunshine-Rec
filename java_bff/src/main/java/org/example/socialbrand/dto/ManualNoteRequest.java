package org.example.socialbrand.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 人工录入笔记请求体。
 *
 * <p>只接收页面可见的公开信息；指标字段可为空，null 表示"未公开"，
 * 不得由前端或后端猜测补齐。</p>
 */
@Data
public class ManualNoteRequest {

    private String noteUrl;
    private String title;
    private String contentSummary;
    private LocalDateTime publishedAt;
    private Long companyId;
    /** 可为空：普通用户讨论没有可归属的企业账号 */
    private Long accountId;
    /** OFFICIAL / USER_DISCUSSION / UNKNOWN */
    private String ownerType;
    /** VERIFIED / PENDING / FAILED */
    private String verificationStatus;
    private List<String> topicTags = new ArrayList<>();
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private Integer shareCount;
    private Integer viewCount;
    /** 指标缺失原因（如"收藏数未公开"） */
    private String missingReason;
    private String remark;
}
