package org.example.crawler.classify;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 报名状态七态回归（审计 P1-6 / P1-15）。
 */
class RegistrationStatusResolverTest {

    private final RegistrationStatusResolver resolver = new RegistrationStatusResolver();

    @Test
    void deadlineMissingReturnsNotPublished() {
        assertEquals("NOT_PUBLISHED", resolver.resolve(null, null, null));
    }

    @Test
    void pastDeadlineReturnsClosedEvenIfStartInFuture() {
        LocalDateTime now = LocalDateTime.now();
        assertEquals("CLOSED",
                resolver.resolve(now.plusDays(3), now.minusDays(1), null));
    }

    @Test
    void futureStartReturnsNotOpen() {
        LocalDateTime now = LocalDateTime.now();
        assertEquals("NOT_OPEN",
                resolver.resolve(now.plusDays(2), now.plusDays(5), null));
    }

    @Test
    void within72HoursReturnsClosingSoon() {
        LocalDateTime now = LocalDateTime.now();
        assertEquals("CLOSING_SOON",
                resolver.resolve(now.minusDays(1), now.plusHours(12), null));
    }

    @Test
    void normalWindowReturnsOpen() {
        LocalDateTime now = LocalDateTime.now();
        assertEquals("OPEN",
                resolver.resolve(now.minusDays(1), now.plusDays(10), null));
    }

    @Test
    void contradictoryRangeReturnsNeedsReview() {
        LocalDateTime now = LocalDateTime.now();
        assertEquals("NEEDS_REVIEW",
                resolver.resolve(now.plusDays(5), now.plusDays(2), null));
    }

    @Test
    void cancelledEventWins() {
        LocalDateTime now = LocalDateTime.now();
        assertEquals("CANCELLED",
                resolver.resolve(now.minusDays(1), now.plusDays(5), "CANCELLED"));
    }
}
