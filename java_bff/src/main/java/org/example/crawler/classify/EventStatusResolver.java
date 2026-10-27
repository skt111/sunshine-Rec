package org.example.crawler.classify;

import org.example.supplyradar.enums.EventStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 活动状态推导：基于活动日期/时间与当前时间。
 */
@Component
public class EventStatusResolver {

    /**
     * 推导活动状态。
     */
    public EventStatus resolve(LocalDate eventDate, String startTime, String endTime, String text) {
        // P1-16: 仅标题级短文本参与“取消/延期”判定；正文（含“报名后取消/如遇不可抗力延期”）不改变状态
        boolean titleLevel = text != null && text.length() < 300;
        if (titleLevel && (text.contains("取消") || text.contains("停办"))) {
            return EventStatus.CANCELLED;
        }
        if (titleLevel && (text.contains("延期") || text.contains("推迟"))) {
            return EventStatus.POSTPONED;
        }
        if (eventDate == null) {
            return EventStatus.UNKNOWN;
        }

        LocalDate today = LocalDate.now();
        if (eventDate.isAfter(today)) {
            return EventStatus.UPCOMING;
        }
        if (eventDate.isBefore(today)) {
            return EventStatus.FINISHED;
        }
        // 同一天：按结束时间判断
        LocalTime end = parseTime(endTime, LocalTime.MAX);
        LocalTime start = parseTime(startTime, LocalTime.MIN);
        LocalDateTime now = LocalDateTime.now();
        if (now.toLocalTime().isBefore(start)) {
            return EventStatus.UPCOMING;
        }
        if (now.toLocalTime().isAfter(end)) {
            return EventStatus.FINISHED;
        }
        return EventStatus.ONGOING;
    }

    private LocalTime parseTime(String value, LocalTime fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String s = value.trim().replace("：", ":");
        try {
            if (s.length() >= 5) {
                return LocalTime.parse(s.substring(0, 5));
            }
            return LocalTime.parse(s);
        } catch (Exception e) {
            return fallback;
        }
    }
}
