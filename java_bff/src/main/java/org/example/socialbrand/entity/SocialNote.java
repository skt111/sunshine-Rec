package org.example.socialbrand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 重点企业公开笔记 {@code social_note}。
 *
 * <p>只保存元数据与原始链接，不落正文全文与图片/视频文件。</p>
 */
@Data
@TableName("social_note")
public class SocialNote {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String platform;
    private String noteUrl;
    private String title;
    /** 清洗前原始标题（首次清洗时留档） */
    private String titleRaw;
    private String contentSummary;
    private LocalDateTime publishedAt;
    private LocalDateTime firstObservedAt;
    private LocalDateTime lastObservedAt;
    private Long companyId;
    private Long accountId;
    /** OFFICIAL / USER_DISCUSSION / UNKNOWN */
    private String ownerType;
    /** MANUAL / AUTHORIZED_API / AUTHORIZED_SERVICE / PUBLIC_PAGE */
    private String sourceType;
    /** VERIFIED / PENDING / FAILED */
    private String verificationStatus;
    /** 主题标签，逗号分隔 */
    private String topicTags;
    private String missingReason;
    /** 清洗方式：LLM / RULE / NONE */
    private String cleanSource;
    /** 清洗依据与置信度 */
    private String cleanBasis;
    private LocalDateTime cleanedAt;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
