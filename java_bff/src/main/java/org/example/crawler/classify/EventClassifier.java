package org.example.crawler.classify;

import org.example.supplyradar.enums.EventStatus;
import org.example.supplyradar.enums.EventType;
import org.example.supplyradar.enums.StandardCategory;
import org.example.supplyradar.enums.VerificationStatus;
import org.example.supplyradar.enums.VenueType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 活动分类器：组合类型/地点/状态识别，并按需求 §3.7.9 推导标准分类。
 */
@Component
public class EventClassifier {

    private final EventTypeResolver typeResolver;
    private final VenueTypeResolver venueResolver;
    private final EventStatusResolver statusResolver;

    public EventClassifier(EventTypeResolver typeResolver,
                           VenueTypeResolver venueResolver,
                           EventStatusResolver statusResolver) {
        this.typeResolver = typeResolver;
        this.venueResolver = venueResolver;
        this.statusResolver = statusResolver;
    }

    /**
     * 分类结果。
     */
    public static class Classification {
        public EventType eventType;
        public VenueType venueType;
        public StandardCategory standardCategory;
        public EventStatus eventStatus;
        public VerificationStatus verificationStatus;
    }

    /**
     * 执行分类（§3.7.9 全 6 种 venueType 覆盖）。
     */
    public Classification classify(String title, String text, String originalType,
                                   String venueName, String address,
                                   LocalDate eventDate, String startTime, String endTime) {
        Classification c = new Classification();
        c.eventType = typeResolver.resolve(title, text, originalType);
        c.venueType = venueResolver.resolve(venueName, address, text);
        c.eventStatus = statusResolver.resolve(eventDate, startTime, endTime, text);
        c.verificationStatus = VerificationStatus.PENDING;

        boolean talk = c.eventType == EventType.TALK;
        boolean fair = c.eventType == EventType.JOB_FAIR;

        if (talk && c.venueType == VenueType.ON_CAMPUS) {
            c.standardCategory = StandardCategory.ON_CAMPUS_TALK;
        } else if (talk && c.venueType == VenueType.OFF_CAMPUS) {
            c.standardCategory = StandardCategory.OFF_CAMPUS_TALK;
        } else if (fair && c.venueType == VenueType.ON_CAMPUS) {
            c.standardCategory = StandardCategory.ON_CAMPUS_FAIR;
        } else if (fair && c.venueType == VenueType.OFF_CAMPUS) {
            c.standardCategory = StandardCategory.OFF_CAMPUS_FAIR;
        } else if ((talk || fair) && c.venueType == VenueType.ONLINE) {
            // 线上活动归入其他，不臆断校内/校外
            c.standardCategory = StandardCategory.OTHER;
        } else if ((talk || fair) && c.venueType == VenueType.HYBRID) {
            c.standardCategory = StandardCategory.PENDING_REVIEW;
            c.verificationStatus = VerificationStatus.NEED_REVIEW;
        } else if ((talk || fair) && c.venueType == VenueType.UNKNOWN) {
            // 保留 eventType，地点未知进入复核
            c.standardCategory = StandardCategory.PENDING_REVIEW;
            c.verificationStatus = VerificationStatus.NEED_REVIEW;
        } else if (c.eventType == EventType.UNKNOWN) {
            c.standardCategory = StandardCategory.PENDING_REVIEW;
            c.verificationStatus = VerificationStatus.NEED_REVIEW;
        } else {
            c.standardCategory = StandardCategory.OTHER;
        }
        return c;
    }
}
