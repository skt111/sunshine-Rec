package org.example.socialbrand.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 榜单条目。
 */
@Data
public class RankingItemVO {

    private int rank;
    private Long noteId;
    private String title;
    private String companyName;
    private String noteUrl;
    /** 单指标榜的取值；主题榜为出现次数 */
    private Long metricValue;
    /** 综合互动榜的加权分；单指标榜为空 */
    private Long score;
    private LocalDateTime metricObservedAt;
    /** 主题榜的标签名 */
    private String topic;
}
