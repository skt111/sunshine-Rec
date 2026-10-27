package org.example.supplyradar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动详情 VO（11 项详情要素 + 变更记录）。
 */
@Data
public class EventDetailVO {

    private Long id;
    private Long schoolId;
    private String schoolName;
    private String schoolCity;

    /** 活动名称（前端详情抽屉首屏标题，契约 §4 必填） */
    private String eventTitle;

    // 类型与分类
    private String eventType;
    private String eventTypeOriginal;
    private String venueType;
    private String standardCategory;

    // 日期时间
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;
    private String startTime;
    private String endTime;

    // 地点
    private String venueName;
    private String eventAddress;

    // 主办与企业
    private String organizer;
    private String companyNames;

    // 重点企业（宣讲会）
    private Long companyId;
    private String companyName;
    private String companyCategory;
    private String recruitmentEntity;

    // 报名（双选会）
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationStartAt;
    private String registrationMethod;
    private String registrationRequirements;
    private String registrationStatus;

    // 原始数据留存
    private String originalTitle;
    private String originalTimeText;

    // 报名
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDeadline;
    private String registrationUrl;

    // 说明
    private String eventDescription;

    // 来源
    private String sourceListUrl;
    private String sourceUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sourcePublishedAt;

    // 采集与核验
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime crawledAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSeenAt;

    private String eventStatus;
    private String verificationStatus;

    // 变更记录
    private List<ChangeVO> changes;
}
