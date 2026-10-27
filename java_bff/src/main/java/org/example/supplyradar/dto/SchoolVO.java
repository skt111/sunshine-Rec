package org.example.supplyradar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 高校列表 VO。
 */
@Data
public class SchoolVO {

    private Long schoolId;
    private String schoolCode;
    private String schoolName;
    private String schoolCity;
    private String province;
    private String siteUrl;
    private String adapterType;
    private Integer enabled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCrawlAt;

    /** 高校级状态（schoolCrawlStatus 枚举） */
    private String lastCrawlStatus;
    private String lastFailReason;
}
