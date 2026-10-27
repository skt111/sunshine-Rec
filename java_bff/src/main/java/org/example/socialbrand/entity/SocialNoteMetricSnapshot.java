package org.example.socialbrand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公开笔记互动指标快照 {@code social_note_metric_snapshot}。
 *
 * <p>字段为 NULL 表示来源未提供，禁止用 0 或点赞数顶替。</p>
 */
@Data
@TableName("social_note_metric_snapshot")
public class SocialNoteMetricSnapshot {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noteId;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private Integer shareCount;
    private Integer viewCount;
    private LocalDateTime metricObservedAt;
    private String metricSource;
    private LocalDateTime createdAt;
}
