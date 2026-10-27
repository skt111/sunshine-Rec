package org.example.supplyradar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统计指标 VO。
 */
@Data
public class StatisticsVO {

    private Long monitoredSchoolCount;
    private Long totalEventCount;
    private Long recentTalkCount;
    private Long recentFairCount;
    private Long pendingVerificationCount;

    /** 需求侧：开放报名数（OPEN + CLOSING_SOON） */
    private Long openRegistrationCount;
    /** 需求侧：即将截止数（7 天内） */
    private Long closingSoonCount;
    /** 需求侧：报名时间未公布数 */
    private Long registrationPendingCount;
    /** 供给侧：被覆盖高校数（按 school_id 去重） */
    private Long coveredSchoolCount;
    /** 供给侧：命中重点企业的活动数 */
    private Long companyTalkCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCrawlAt;

    /** 任务级状态（crawlTaskStatus 枚举） */
    private String lastCrawlTaskStatus;

    private CategoryCounts categoryCounts = new CategoryCounts();

    /** 类型计数（页签：全部 / 宣讲会 / 双选会 / 其他） */
    private TypeCounts typeCounts = new TypeCounts();

    @Data
    public static class TypeCounts {
        private Long all = 0L;
        private Long talk = 0L;
        private Long jobFair = 0L;
        private Long other = 0L;
    }

    /**
     * 分类计数（other = OTHER + PENDING_REVIEW）。
     */
    @Data
    public static class CategoryCounts {
        private Long all = 0L;
        private Long onCampusTalk = 0L;
        private Long offCampusTalk = 0L;
        private Long onCampusFair = 0L;
        private Long offCampusFair = 0L;
        private Long other = 0L;
    }
}
