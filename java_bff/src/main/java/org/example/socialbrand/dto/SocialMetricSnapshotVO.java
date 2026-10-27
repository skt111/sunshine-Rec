package org.example.socialbrand.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 互动指标快照视图。
 *
 * <p>字段为 null 表示来源未提供；{@code missingFields} 列出缺失指标的中文名，
 * 前端据此显示"未公开"，不得显示为 0。</p>
 */
@Data
public class SocialMetricSnapshotVO {

    private Long id;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private Integer shareCount;
    private Integer viewCount;
    private LocalDateTime metricObservedAt;
    private String metricSource;
    private List<String> missingFields = new ArrayList<>();
}
