package org.example.supplyradar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 采集任务 VO。
 */
@Data
public class CrawlTaskVO {

    private Long taskId;
    private String taskType;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishedAt;

    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private String errorSummary;
}
