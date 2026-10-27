package org.example.supplyradar.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 招聘活动表 {@code employment_event}。
 */
@Data
@TableName("employment_event")
public class EmploymentEvent {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long schoolId;
    private String schoolName;
    private String schoolCity;
    private String eventTitle;
    private String originalTitle;
    private String eventType;
    private String eventTypeOriginal;
    private String originalCategory;
    private String venueType;
    private String standardCategory;
    private LocalDate eventDate;
    private String startTime;
    private String originalTimeText;
    private String endTime;
    private String venueName;
    private String eventAddress;
    private String organizer;
    private String companyNames;
    /** 重点企业（仅宣讲会） */
    private Long companyId;
    private String companyName;
    private String companyCategory;
    private String recruitmentEntity;
    private LocalDateTime registrationDeadline;
    private LocalDateTime registrationStartAt;
    private String registrationMethod;
    private String registrationRequirements;
    private String registrationStatus;
    private String registrationUrl;
    private String eventDescription;
    private String sourceListUrl;
    private String sourceUrl;
    private LocalDateTime sourcePublishedAt;
    private LocalDateTime crawledAt;
    private LocalDateTime lastSeenAt;
    private String eventStatus;
    private String verificationStatus;
    private String remoteId;
    private String fingerprint;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
