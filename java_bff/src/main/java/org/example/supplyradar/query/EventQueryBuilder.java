package org.example.supplyradar.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.supplyradar.entity.EmploymentEvent;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 查询条件构建器：统计 / 列表 / 分组**共用同一口径**（需求 B7）。
 *
 * <p>全部条件参数化（MP 自动处理）；排序字段走白名单；列名使用 DB snake_case。</p>
 */
@Component
public class EventQueryBuilder {

    private static final List<String> SORT_WHITELIST =
            Arrays.asList("eventDate", "lastSeenAt", "sourcePublishedAt", "registrationDeadline");

    /** category=OTHER 等价于 IN ('OTHER','PENDING_REVIEW') */
    private static final List<String> OTHER_WITH_PENDING =
            Arrays.asList("OTHER", "PENDING_REVIEW");

    /**
     * 构建筛选条件（不含排序/分页）。
     */
    public QueryWrapper<EmploymentEvent> buildFilterWrapper(EventQuery q) {
        QueryWrapper<EmploymentEvent> w = new QueryWrapper<>();

        // 时间筛选：启用时 BETWEEN 自动排除 NULL
        if (Boolean.TRUE.equals(q.getTimeFilterEnabled())
                && q.getStartDate() != null && q.getEndDate() != null) {
            w.between("event_date", q.getStartDate(), q.getEndDate());
        }
        if (q.getSchoolIds() != null && !q.getSchoolIds().isEmpty()) {
            w.in("school_id", q.getSchoolIds());
        }
        if (notBlank(q.getCity())) {
            w.eq("school_city", q.getCity());
        }
        if (notBlank(q.getCategory()) && !"ALL".equalsIgnoreCase(q.getCategory())) {
            if ("OTHER".equalsIgnoreCase(q.getCategory())) {
                w.in("standard_category", OTHER_WITH_PENDING);
            } else {
                w.eq("standard_category", q.getCategory());
            }
        }
        if (notBlank(q.getEventType())) {
            w.eq("event_type", q.getEventType());
        }
        if (q.getExcludeEventTypes() != null && !q.getExcludeEventTypes().isEmpty()) {
            w.notIn("event_type", q.getExcludeEventTypes());
        }
        if (notBlank(q.getVenueType())) {
            w.eq("venue_type", q.getVenueType());
        }
        if (notBlank(q.getStatus())) {
            w.eq("event_status", q.getStatus());
        }
        if (notBlank(q.getVerificationStatus())) {
            w.eq("verification_status", q.getVerificationStatus());
        }
        if (notBlank(q.getKeyword())) {
            final String kw = q.getKeyword().trim();
            w.and(x -> x.like("event_title", kw).or().like("school_name", kw)
                    .or().like("company_name", kw));
        }
        // 重点企业筛选（宣讲会）
        if (q.getCompanyId() != null) {
            w.eq("company_id", q.getCompanyId());
        }
        if (notBlank(q.getCompanyName())) {
            w.eq("company_name", q.getCompanyName());
        }
        if (notBlank(q.getCompanyCategory())) {
            w.eq("company_category", q.getCompanyCategory());
        }
        // 报名状态（双选会）
        if (notBlank(q.getRegistrationStatus())) {
            w.eq("registration_status", q.getRegistrationStatus());
        }
        if (Boolean.TRUE.equals(q.getHasRegistrationUrl())) {
            w.isNotNull("registration_url").ne("registration_url", "");
        }
        return w;
    }

    /**
     * 应用排序（白名单）。
     */
    public void applySort(QueryWrapper<EmploymentEvent> w, EventQuery q) {
        String sortBy = SORT_WHITELIST.contains(q.getSortBy()) ? q.getSortBy() : "eventDate";
        boolean asc = !"desc".equalsIgnoreCase(q.getSortDirection());

        switch (sortBy) {
            case "lastSeenAt":
                w.orderBy(true, asc, "last_seen_at");
                break;
            case "sourcePublishedAt":
                w.orderBy(true, asc, "source_published_at");
                break;
            case "registrationDeadline":
                w.orderBy(true, asc, "registration_deadline").orderByAsc("id");
                break;
            default:
                // 双选会优先展示（产品要求：重点突出双选会），再按日期；
                // P1-4: 契约要求 event_date IS NULL 置尾（MySQL 升序下 NULL 会排最前）
                w.last("ORDER BY (event_type = 'JOB_FAIR') DESC, (event_date IS NULL) ASC, event_date "
                        + (asc ? "ASC" : "DESC") + ", id ASC");
                break;
        }
    }

    /**
     * 校验分页与枚举参数（越界 → IllegalArgumentException → 全局壳 fail）。
     */
    public void validatePaging(EventQuery q) {
        if (q.getPage() == null || q.getPage() < 1) {
            q.setPage(1);
        }
        if (q.getSize() == null || q.getSize() < 1 || q.getSize() > 100) {
            throw new IllegalArgumentException("size 需在 1~100 之间");
        }
        if (notBlank(q.getSortBy()) && !SORT_WHITELIST.contains(q.getSortBy())) {
            throw new IllegalArgumentException(
                    "sortBy 仅支持 eventDate/lastSeenAt/sourcePublishedAt/registrationDeadline");
        }
        if (notBlank(q.getGroupBy()) && !"school".equalsIgnoreCase(q.getGroupBy())) {
            throw new IllegalArgumentException("groupBy 仅支持 school");
        }
    }

    /**
     * 类型计数聚合（按 event_type：宣讲 / 双选 / 其他）。
     */
    public QueryWrapper<EmploymentEvent> buildTypeCount(EventQuery q) {
        QueryWrapper<EmploymentEvent> w = buildFilterWrapper(q);
        w.select("event_type AS type", "COUNT(*) AS cnt").groupBy("event_type");
        return w;
    }

    /**
     * 分类计数聚合。
     */
    public QueryWrapper<EmploymentEvent> buildCategoryCount(EventQuery q) {
        QueryWrapper<EmploymentEvent> w = buildFilterWrapper(q);
        w.select("standard_category AS category", "COUNT(*) AS cnt")
                .groupBy("standard_category");
        return w;
    }

    /**
     * 分组聚合（按 school_id，禁按校名模糊匹配）。
     */
    public QueryWrapper<EmploymentEvent> buildGroupBySchool(EventQuery q) {
        QueryWrapper<EmploymentEvent> w = buildFilterWrapper(q);
        w.select("school_id AS schoolId", "school_name AS schoolName",
                        "school_city AS schoolCity", "COUNT(*) AS eventCount")
                .groupBy("school_id", "school_name", "school_city")
                .orderByDesc("eventCount");
        return w;
    }

    public boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
