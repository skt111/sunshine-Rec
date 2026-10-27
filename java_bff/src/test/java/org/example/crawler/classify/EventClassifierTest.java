package org.example.crawler.classify;

import org.example.supplyradar.enums.EventStatus;
import org.example.supplyradar.enums.EventType;
import org.example.supplyradar.enums.StandardCategory;
import org.example.supplyradar.enums.VerificationStatus;
import org.example.supplyradar.enums.VenueType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 分类器单测：覆盖 §3.7.9 全 6 种 venueType 与兜底分支。
 */
class EventClassifierTest {

    private final EventClassifier classifier = new EventClassifier(
            new EventTypeResolver(), new VenueTypeResolver(), new EventStatusResolver());

    @Test
    void onCampusTalk() {
        EventClassifier.Classification c = classifier.classify(
                "校园宣讲会", "地点：校内 教学楼 A101", null,
                "教学楼 A101", null, LocalDate.now().plusDays(3), "14:00", "16:00");
        assertEquals(EventType.TALK, c.eventType);
        assertEquals(VenueType.ON_CAMPUS, c.venueType);
        assertEquals(StandardCategory.ON_CAMPUS_TALK, c.standardCategory);
        assertEquals(EventStatus.UPCOMING, c.eventStatus);
    }

    @Test
    void offCampusFair() {
        EventClassifier.Classification c = classifier.classify(
                "大型双选会", "地点：国际会展中心 3 号馆", null,
                "国际会展中心 3 号馆", null, LocalDate.now().plusDays(5), "09:00", "12:00");
        assertEquals(EventType.JOB_FAIR, c.eventType);
        assertEquals(VenueType.OFF_CAMPUS, c.venueType);
        assertEquals(StandardCategory.OFF_CAMPUS_FAIR, c.standardCategory);
    }

    @Test
    void onlineTalkGoesToOther() {
        EventClassifier.Classification c = classifier.classify(
                "线上宣讲会", "腾讯会议 直播", null,
                "线上会议室", null, LocalDate.now().plusDays(2), "19:00", "20:00");
        assertEquals(EventType.TALK, c.eventType);
        assertEquals(VenueType.ONLINE, c.venueType);
        assertEquals(StandardCategory.OTHER, c.standardCategory);
    }

    @Test
    void hybridGoesToPendingReview() {
        EventClassifier.Classification c = classifier.classify(
                "双选会", "线上+线下 同步 体育馆", null,
                "体育馆 与 线上直播", null, LocalDate.now().plusDays(1), "09:00", "11:00");
        assertEquals(VenueType.HYBRID, c.venueType);
        assertEquals(StandardCategory.PENDING_REVIEW, c.standardCategory);
        assertEquals(VerificationStatus.NEED_REVIEW, c.verificationStatus);
    }

    @Test
    void unknownVenueKeepsTypeButPendingReview() {
        EventClassifier.Classification c = classifier.classify(
                "宣讲会", "详情待更新", null,
                null, null, LocalDate.now().plusDays(4), null, null);
        assertEquals(EventType.TALK, c.eventType);
        assertEquals(VenueType.UNKNOWN, c.venueType);
        assertEquals(StandardCategory.PENDING_REVIEW, c.standardCategory);
        assertEquals(VerificationStatus.NEED_REVIEW, c.verificationStatus);
    }

    @Test
    void serviceActivityGoesToOther() {
        EventClassifier.Classification c = classifier.classify(
                "就业指导讲座", "简历撰写技巧", null,
                "校内报告厅", null, LocalDate.now().plusDays(6), "15:00", "17:00");
        assertEquals(EventType.EMPLOYMENT_SERVICE, c.eventType);
        assertEquals(StandardCategory.OTHER, c.standardCategory);
    }

    @Test
    void cancelledByText() {
        EventClassifier.Classification c = classifier.classify(
                "宣讲会（已取消）", "本场活动取消", null,
                "教学楼", null, LocalDate.now().plusDays(7), "10:00", "11:00");
        assertEquals(EventStatus.CANCELLED, c.eventStatus);
    }
}
