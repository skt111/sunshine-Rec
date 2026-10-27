package org.example.supplyradar.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.supplyradar.dto.StatisticsVO;
import org.example.supplyradar.entity.CrawlTask;
import org.example.supplyradar.mapper.CrawlTaskMapper;
import org.example.supplyradar.mapper.EmploymentEventMapper;
import org.example.supplyradar.mapper.UniversitySiteMapper;
import org.example.supplyradar.query.EventQuery;
import org.example.supplyradar.query.EventQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 统计指标服务（口径 Q1~Q4）。
 */
@Service
public class StatisticsService {

    private final UniversitySiteMapper siteMapper;
    private final EmploymentEventMapper eventMapper;
    private final CrawlTaskMapper taskMapper;
    private final EventQueryBuilder queryBuilder;

    public StatisticsService(UniversitySiteMapper siteMapper,
                             EmploymentEventMapper eventMapper,
                             CrawlTaskMapper taskMapper,
                             EventQueryBuilder queryBuilder) {
        this.siteMapper = siteMapper;
        this.eventMapper = eventMapper;
        this.taskMapper = taskMapper;
        this.queryBuilder = queryBuilder;
    }

    public StatisticsVO statistics(EventQuery q) {
        StatisticsVO vo = new StatisticsVO();

        // Q1 监测高校数
        QueryWrapper<org.example.supplyradar.entity.UniversitySite> siteWrapper = new QueryWrapper<>();
        siteWrapper.eq("enabled", 1);
        vo.setMonitoredSchoolCount(siteMapper.selectCount(siteWrapper));

        // Q2 活动总数（同筛选）
        vo.setTotalEventCount(eventMapper.selectCount(queryBuilder.buildFilterWrapper(q)));

        // Q4 分类计数（不自我过滤 category，否则页签计数全归零）
        EventQuery countQuery = new EventQuery();
        countQuery.setSchoolIds(q.getSchoolIds());
        countQuery.setCity(q.getCity());
        countQuery.setEventType(q.getEventType());
        countQuery.setVenueType(q.getVenueType());
        countQuery.setStartDate(q.getStartDate());
        countQuery.setEndDate(q.getEndDate());
        countQuery.setTimeFilterEnabled(q.getTimeFilterEnabled());
        countQuery.setStatus(q.getStatus());
        countQuery.setVerificationStatus(q.getVerificationStatus());
        countQuery.setKeyword(q.getKeyword());
        countQuery.setCompanyId(q.getCompanyId());
        countQuery.setCompanyName(q.getCompanyName());
        countQuery.setCompanyCategory(q.getCompanyCategory());
        countQuery.setRegistrationStatus(q.getRegistrationStatus());
        countQuery.setHasRegistrationUrl(q.getHasRegistrationUrl());
        List<Map<String, Object>> rows = eventMapper.selectMaps(queryBuilder.buildCategoryCount(countQuery));

        // 类型计数（页签：全部/宣讲会/双选会/其他）——不受 eventType/exclude 影响
        EventQuery typeQuery = copyForTypeCount(countQuery);
        List<Map<String, Object>> typeRows = eventMapper.selectMaps(queryBuilder.buildTypeCount(typeQuery));
        StatisticsVO.TypeCounts typeCounts = vo.getTypeCounts();
        long typeAll = 0L;
        for (Map<String, Object> row : typeRows) {
            String type = str(row.get("type"));
            long cnt = num(row.get("cnt"));
            typeAll += cnt;
            if ("TALK".equals(type)) {
                typeCounts.setTalk(cnt);
            } else if ("JOB_FAIR".equals(type)) {
                typeCounts.setJobFair(cnt);
            } else {
                typeCounts.setOther(typeCounts.getOther() + cnt);
            }
        }
        typeCounts.setAll(typeAll);
        StatisticsVO.CategoryCounts counts = vo.getCategoryCounts();
        long all = 0L;
        for (Map<String, Object> row : rows) {
            String category = str(row.get("category"));
            long cnt = num(row.get("cnt"));
            all += cnt;
            switch (category == null ? "" : category) {
                case "ON_CAMPUS_TALK":
                    counts.setOnCampusTalk(cnt);
                    break;
                case "OFF_CAMPUS_TALK":
                    counts.setOffCampusTalk(cnt);
                    break;
                case "ON_CAMPUS_FAIR":
                    counts.setOnCampusFair(cnt);
                    break;
                case "OFF_CAMPUS_FAIR":
                    counts.setOffCampusFair(cnt);
                    break;
                case "OTHER":
                case "PENDING_REVIEW":
                    counts.setOther(counts.getOther() + cnt);
                    break;
                default:
                    break;
            }
        }
        counts.setAll(all);

        // Q3 近期宣讲/双选
        vo.setRecentTalkCount(counts.getOnCampusTalk() + counts.getOffCampusTalk());
        vo.setRecentFairCount(counts.getOnCampusFair() + counts.getOffCampusFair());

        // 待核验：verificationStatus ∈ (PENDING, NEED_REVIEW) 或 standardCategory=PENDING_REVIEW
        QueryWrapper<org.example.supplyradar.entity.EmploymentEvent> pendingWrapper = queryBuilder.buildFilterWrapper(q);
        pendingWrapper.and(w -> w.in("verification_status", "PENDING", "NEED_REVIEW")
                .or().eq("standard_category", "PENDING_REVIEW"));
        vo.setPendingVerificationCount(eventMapper.selectCount(pendingWrapper));

        // 需求侧：报名状态统计（与列表同筛选口径）
        QueryWrapper<org.example.supplyradar.entity.EmploymentEvent> openWrapper = queryBuilder.buildFilterWrapper(q);
        openWrapper.in("registration_status", "OPEN", "CLOSING_SOON");
        vo.setOpenRegistrationCount(eventMapper.selectCount(openWrapper));

        QueryWrapper<org.example.supplyradar.entity.EmploymentEvent> soonWrapper = queryBuilder.buildFilterWrapper(q);
        soonWrapper.eq("registration_status", "CLOSING_SOON");
        vo.setClosingSoonCount(eventMapper.selectCount(soonWrapper));

        QueryWrapper<org.example.supplyradar.entity.EmploymentEvent> regPendingWrapper = queryBuilder.buildFilterWrapper(q);
        regPendingWrapper.eq("registration_status", "NOT_PUBLISHED");
        vo.setRegistrationPendingCount(eventMapper.selectCount(regPendingWrapper));

        // 供给侧：命中重点企业的宣讲数
        QueryWrapper<org.example.supplyradar.entity.EmploymentEvent> talkWrapper = queryBuilder.buildFilterWrapper(q);
        talkWrapper.eq("event_type", "TALK").isNotNull("company_id");
        vo.setCompanyTalkCount(eventMapper.selectCount(talkWrapper));

        // 供给侧：覆盖高校数（去重）
        QueryWrapper<org.example.supplyradar.entity.EmploymentEvent> coverWrapper = queryBuilder.buildFilterWrapper(q);
        coverWrapper.select("school_id").groupBy("school_id");
        vo.setCoveredSchoolCount((long) eventMapper.selectMaps(coverWrapper).size());

        // 最近任务
        QueryWrapper<CrawlTask> taskWrapper = new QueryWrapper<>();
        taskWrapper.orderByDesc("id").last("limit 1");
        CrawlTask last = taskMapper.selectOne(taskWrapper);
        if (last != null) {
            vo.setLastCrawlAt(last.getFinishedAt() != null ? last.getFinishedAt() : last.getStartedAt());
            vo.setLastCrawlTaskStatus(last.getStatus());
        }
        return vo;
    }

    private EventQuery copyForTypeCount(EventQuery q) {
        EventQuery sub = new EventQuery();
        sub.setSchoolIds(q.getSchoolIds());
        sub.setCity(q.getCity());
        sub.setVenueType(q.getVenueType());
        sub.setStartDate(q.getStartDate());
        sub.setEndDate(q.getEndDate());
        sub.setTimeFilterEnabled(q.getTimeFilterEnabled());
        sub.setStatus(q.getStatus());
        sub.setVerificationStatus(q.getVerificationStatus());
        sub.setKeyword(q.getKeyword());
        sub.setCompanyId(q.getCompanyId());
        sub.setCompanyName(q.getCompanyName());
        sub.setCompanyCategory(q.getCompanyCategory());
        sub.setRegistrationStatus(q.getRegistrationStatus());
        sub.setHasRegistrationUrl(q.getHasRegistrationUrl());
        // 清除类型条件，保证四个页签同时有值
        sub.setEventType(null);
        sub.setExcludeEventTypes(null);
        sub.setCategory(null);
        return sub;
    }

    private String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private long num(Object v) {
        if (v == null) {
            return 0L;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return 0L;
        }
    }
}
