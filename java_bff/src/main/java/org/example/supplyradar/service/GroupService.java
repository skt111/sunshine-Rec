package org.example.supplyradar.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.supplyradar.dto.EventVO;
import org.example.supplyradar.dto.GroupVO;
import org.example.supplyradar.entity.EmploymentEvent;
import org.example.supplyradar.entity.UniversitySite;
import org.example.supplyradar.mapper.EmploymentEventMapper;
import org.example.supplyradar.mapper.UniversitySiteMapper;
import org.example.supplyradar.query.EventQuery;
import org.example.supplyradar.query.EventQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 高校分组视图服务（groupBy=school，口径 Q5）。
 */
@Service
public class GroupService {

    private static final int RECENT_LIMIT = 3;

    private final EmploymentEventMapper eventMapper;
    private final UniversitySiteMapper siteMapper;
    private final EventQueryBuilder queryBuilder;
    private final EventService eventService;

    public GroupService(EmploymentEventMapper eventMapper,
                        UniversitySiteMapper siteMapper,
                        EventQueryBuilder queryBuilder,
                        EventService eventService) {
        this.eventMapper = eventMapper;
        this.siteMapper = siteMapper;
        this.queryBuilder = queryBuilder;
        this.eventService = eventService;
    }

    /**
     * 分组查询（total = 高校组数）。
     */
    public Map<String, Object> groups(EventQuery q) {
        queryBuilder.validatePaging(q);

        // 分组模式下 schoolIds 作为过滤，不清空
        List<Map<String, Object>> rows = eventMapper.selectMaps(queryBuilder.buildGroupBySchool(q));

        int from = (q.getPage() - 1) * q.getSize();
        int to = Math.min(from + q.getSize(), rows.size());
        List<Map<String, Object>> pageRows = from >= rows.size()
                ? new ArrayList<>() : rows.subList(from, to);

        // 批量取高校状态（禁 N+1）
        List<Long> schoolIds = new ArrayList<>();
        for (Map<String, Object> r : pageRows) {
            Long id = asLong(r.get("schoolId"));
            if (id != null) {
                schoolIds.add(id);
            }
        }
        Map<Long, UniversitySite> siteMap = loadSites(schoolIds);

        // 批量取每校近期活动（一次查询）
        Map<Long, List<EventVO>> recentMap = loadRecentEvents(schoolIds, q);

        List<GroupVO> list = new ArrayList<>(pageRows.size());
        for (Map<String, Object> r : pageRows) {
            GroupVO g = new GroupVO();
            Long schoolId = asLong(r.get("schoolId"));
            g.setSchoolId(schoolId);
            g.setSchoolName(asString(r.get("schoolName")));
            g.setSchoolCity(asString(r.get("schoolCity")));
            g.setEventCount(asLong(r.get("eventCount")));
            UniversitySite site = schoolId == null ? null : siteMap.get(schoolId);
            if (site != null) {
                g.setLastCrawlAt(site.getLastCrawlAt());
                g.setLastCrawlStatus(site.getCrawlStatus());
            }
            g.setRecentEvents(recentMap.getOrDefault(schoolId, new ArrayList<>()));
            list.add(g);
        }

        long total = rows.size();
        long totalPages = q.getSize() <= 0 ? 0 : (total + q.getSize() - 1) / q.getSize();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", q.getPage());
        result.put("size", q.getSize());
        result.put("totalPages", totalPages);
        return result;
    }

    private Map<Long, UniversitySite> loadSites(List<Long> schoolIds) {
        Map<Long, UniversitySite> map = new HashMap<>();
        if (schoolIds.isEmpty()) {
            return map;
        }
        QueryWrapper<UniversitySite> w = new QueryWrapper<>();
        w.in("id", schoolIds);
        for (UniversitySite s : siteMapper.selectList(w)) {
            map.put(s.getId(), s);
        }
        return map;
    }

    private Map<Long, List<EventVO>> loadRecentEvents(List<Long> schoolIds, EventQuery q) {
        Map<Long, List<EventVO>> map = new HashMap<>();
        // P2-7：按校分别查询 + LIMIT，避免一次性拉全量后在内存截取
        for (Long schoolId : schoolIds) {
            EventQuery sub = copyForRecent(q);
            sub.setSchoolIds(java.util.Collections.singletonList(schoolId));
            QueryWrapper<EmploymentEvent> w = queryBuilder.buildFilterWrapper(sub);
            w.orderByAsc("event_date").orderByAsc("id").last("LIMIT " + RECENT_LIMIT);
            for (EmploymentEvent e : eventMapper.selectList(w)) {
                map.computeIfAbsent(e.getSchoolId(), k -> new ArrayList<>())
                        .add(eventService.toVO(e));
            }
        }
        return map;
    }

    private EventQuery copyForRecent(EventQuery q) {
        EventQuery sub = new EventQuery();
        sub.setSchoolIds(null);
        sub.setCity(q.getCity());
        sub.setCategory(q.getCategory());
        sub.setEventType(q.getEventType());
        sub.setVenueType(q.getVenueType());
        sub.setStartDate(q.getStartDate());
        sub.setEndDate(q.getEndDate());
        sub.setTimeFilterEnabled(q.getTimeFilterEnabled());
        sub.setStatus(q.getStatus());
        sub.setVerificationStatus(q.getVerificationStatus());
        sub.setKeyword(q.getKeyword());
        // 重点企业 / 报名条件（与列表同口径，防漏筛）
        sub.setCompanyId(q.getCompanyId());
        sub.setCompanyName(q.getCompanyName());
        sub.setCompanyCategory(q.getCompanyCategory());
        sub.setRegistrationStatus(q.getRegistrationStatus());
        sub.setHasRegistrationUrl(q.getHasRegistrationUrl());
        sub.setPage(1);
        sub.setSize(100);
        return sub;
    }

    private Long asLong(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    private String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}
