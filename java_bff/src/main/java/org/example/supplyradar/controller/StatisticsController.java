package org.example.supplyradar.controller;

import org.example.common.Result;
import org.example.supplyradar.dto.StatisticsVO;
import org.example.supplyradar.query.EventQuery;
import org.example.supplyradar.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 统计指标接口（与列表同筛选口径）。
 */
@RestController
@RequestMapping("/api/supply-radar")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/statistics")
    public Result<StatisticsVO> statistics(
            @RequestParam(required = false) String schoolIds,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String excludeEventTypes,
            @RequestParam(required = false) String venueType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Boolean timeFilterEnabled,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String verificationStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String companyCategory,
            @RequestParam(required = false) String registrationStatus,
            @RequestParam(required = false) Boolean hasRegistrationUrl) {

        EventQuery q = new EventQuery();
        q.setSchoolIds(parseIds(schoolIds));
        q.setCity(city);
        q.setCategory(category);
        q.setEventType(eventType);
        if (excludeEventTypes != null && !excludeEventTypes.trim().isEmpty()) {
            List<String> ex = new ArrayList<>();
            for (String p : excludeEventTypes.split(",")) {
                String s = p.trim();
                if (!s.isEmpty()) {
                    ex.add(s);
                }
            }
            q.setExcludeEventTypes(ex.isEmpty() ? null : ex);
        }
        q.setVenueType(venueType);
        q.setStartDate(startDate);
        q.setEndDate(endDate);
        q.setTimeFilterEnabled(timeFilterEnabled == null ? Boolean.TRUE : timeFilterEnabled);
        q.setStatus(status);
        q.setVerificationStatus(verificationStatus);
        q.setKeyword(keyword);
        q.setCompanyId(companyId);
        q.setCompanyName(companyName);
        q.setCompanyCategory(companyCategory);
        q.setRegistrationStatus(registrationStatus);
        q.setHasRegistrationUrl(hasRegistrationUrl);
        return Result.success(statisticsService.statistics(q));
    }

    private List<Long> parseIds(String schoolIds) {
        if (schoolIds == null || schoolIds.trim().isEmpty()) {
            return null;
        }
        List<Long> ids = new ArrayList<>();
        for (String part : schoolIds.split(",")) {
            String s = part.trim();
            if (s.isEmpty()) {
                continue;
            }
            try {
                ids.add(Long.parseLong(s));
            } catch (NumberFormatException ignore) {
                // ignore
            }
        }
        return ids.isEmpty() ? null : ids;
    }
}
