package org.example.supplyradar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 活动列表项 VO（卡片所需字段）。
 */
@Data
public class EventVO {

    private Long id;
    private Long schoolId;
    private String schoolName;
    private String schoolCity;
    private String eventTitle;
    private String eventType;
    private String eventTypeOriginal;
    private String venueType;
    private String standardCategory;

    /** 重点企业（宣讲会） */
    private Long companyId;
    private String companyName;
    private String companyCategory;
    private String recruitmentEntity;

    /** 双选会报名信息 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime registrationStartAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime registrationDeadline;

    private String registrationMethod;
    private String registrationRequirements;
    private String registrationStatus;
    private String registrationUrl;
    private String organizer;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;

    private String startTime;
    private String endTime;
    private String venueName;
    private String eventAddress;
    private String eventStatus;
    private String verificationStatus;
    private String sourceUrl;

    /** 来源列表页（活动详情缺失时作为入口兜底） */
    private String sourceListUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime crawledAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSeenAt;
}
