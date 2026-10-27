package org.example.supplyradar.controller;

import org.example.common.Result;
import org.example.supplyradar.dto.EventDetailVO;
import org.example.supplyradar.query.EventQuery;
import org.example.supplyradar.service.EventService;
import org.example.supplyradar.service.GroupService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 活动列表 / 详情接口（含 groupBy=school 分组模式）。
 */
@RestController
@RequestMapping("/api/supply-radar")
public class EventController {

    private final EventService eventService;
    private final GroupService groupService;

    public EventController(EventService eventService, GroupService groupService) {
        this.eventService = eventService;
        this.groupService = groupService;
    }

    @GetMapping("/events")
    public Result<Map<String, Object>> events(
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
            @RequestParam(required = false) Boolean hasRegistrationUrl,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "eventDate") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String groupBy) {

        EventQuery q = new EventQuery();
        q.setSchoolIds(parseIds(schoolIds));
        q.setCity(city);
        q.setCategory(category);
        q.setEventType(eventType);
        q.setExcludeEventTypes(parseList(excludeEventTypes));
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
        q.setPage(page);
        q.setSize(size);
        q.setSortBy(sortBy);
        q.setSortDirection(sortDirection);
        q.setGroupBy(groupBy);

        if ("school".equalsIgnoreCase(groupBy)) {
            return Result.success(groupService.groups(q));
        }
        return Result.success(eventService.page(q));
    }

    @GetMapping("/events/{id}")
    public Result<EventDetailVO> detail(@PathVariable Long id) {
        EventDetailVO vo = eventService.detail(id);
        if (vo == null) {
            return Result.fail("活动不存在");
        }
        return Result.success(vo);
    }

    private List<String> parseList(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return null;
        }
        List<String> out = new ArrayList<>();
        for (String part : csv.split(",")) {
            String s = part.trim();
            if (!s.isEmpty()) {
                out.add(s);
            }
        }
        return out.isEmpty() ? null : out;
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
                // 忽略非法 ID
            }
        }
        return ids.isEmpty() ? null : ids;
    }
}
