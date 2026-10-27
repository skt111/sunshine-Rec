package org.example.crawler.classify;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 双选会企业报名状态计算（文档 4.4）。
 *
 * <p>状态：NOT_OPEN / OPEN / CLOSING_SOON / CLOSED / NOT_PUBLISHED / CANCELLED / NEEDS_REVIEW。
 * 截止时间未知时不得显示 OPEN 或 CLOSING_SOON（文档 4.4）。</p>
 */
@Component
public class RegistrationStatusResolver {

    /** "即将截止"阈值（小时），文档建议配置化 */
    private static final long CLOSING_SOON_HOURS = 72L;

    /**
     * 计算报名状态。
     *
     * @param startAt     报名开始时间（可为空）
     * @param deadline    报名截止时间（可为空）
     * @param eventStatus 活动状态（CANCELLED 优先）
     */
    public String resolve(LocalDateTime startAt, LocalDateTime deadline, String eventStatus) {
        if ("CANCELLED".equals(eventStatus)) {
            return "CANCELLED";
        }
        if (deadline == null) {
            return "NOT_PUBLISHED";
        }
        LocalDateTime now = LocalDateTime.now();
        // 先判已截止（事实优先：deadline 已过则 startAt 异常不影响“已截止”）
        if (now.isAfter(deadline)) {
            return "CLOSED";
        }
        // 矛盾数据（开始晚于/等于截止且未截止）→ 待核验（文档 4.4 七态）
        if (startAt != null && !startAt.isBefore(deadline)) {
            return "NEEDS_REVIEW";
        }
        if (startAt != null && now.isBefore(startAt)) {
            return "NOT_OPEN";
        }
        if (now.plusHours(CLOSING_SOON_HOURS).isAfter(deadline)) {
            return "CLOSING_SOON";
        }
        return "OPEN";
    }

    /**
     * 兼容：仅拿到活动日期时用于展示层判断（不参与入库）。
     */
    public boolean isClosingSoon(LocalDateTime deadline) {
        return deadline != null
                && LocalDateTime.now().plusHours(CLOSING_SOON_HOURS).isAfter(deadline)
                && LocalDateTime.now().isBefore(deadline);
    }

    /**
     * 活动状态：基于举办日期（入库时使用）。
     */
    public String resolveEventStatus(LocalDate eventDate) {
        if (eventDate == null) {
            return "UNKNOWN";
        }
        LocalDate today = LocalDate.now();
        if (eventDate.isBefore(today)) {
            return "FINISHED";
        }
        if (eventDate.isEqual(today)) {
            return "ONGOING";
        }
        return "UPCOMING";
    }
}
