package org.example.supplyradar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 高校分组 VO（groupBy=school）。
 */
@Data
public class GroupVO {

    private Long schoolId;
    private String schoolName;
    private String schoolCity;
    private Long eventCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCrawlAt;

    /** 高校级状态（schoolCrawlStatus 枚举） */
    private String lastCrawlStatus;

    /** 近期活动摘要（最多 3 条） */
    private List<EventVO> recentEvents;
}
