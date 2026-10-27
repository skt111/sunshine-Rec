package org.example.socialbrand.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 榜单返回体：必须带上时间范围、样本数、指标来源与数据完整度，
 * 避免"只给排名不给口径"。
 */
@Data
public class RankingVO {

    /** like / favorite / comment / composite / topic */
    private String type;
    /** 该榜单是否可出（如无收藏数据则 false） */
    private boolean available;
    /** 不可出时的原因（供前端直接展示） */
    private String reason;
    private String range;
    private int sampleSize;
    private int totalNotes;
    /** 完整度：metricAvailable / sampleSize，如 "7/9" */
    private String completeness;
    /** 样本不足（< min-sample-size） */
    private boolean insufficient;
    private String metricSource;
    private List<RankingItemVO> items = new ArrayList<>();
}
