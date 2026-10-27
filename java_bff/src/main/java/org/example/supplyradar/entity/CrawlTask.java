package org.example.supplyradar.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 采集任务表 {@code crawl_task}。
 */
@Data
@TableName("crawl_task")
public class CrawlTask {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskType;
    private String targetSchoolIds;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private String errorSummary;
    private String triggeredBy;
    private LocalDateTime createdAt;
}
