package org.example.crawler.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.crawler.classify.CompanyResolver;
import org.example.crawler.classify.RegistrationStatusResolver;
import org.example.crawler.fetch.PageFetcher;
import org.example.crawler.persist.EventUpserter;
import org.example.supplyradar.entity.EmploymentEvent;
import org.example.supplyradar.entity.UniversitySite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * frontpage 平台通用适配器（公开 JSON 接口，非绕过访问控制）。
 *
 * <p>适用站点（adapter_key=FRONTPAGE）：北航 / 北大 / 北理工 / 农大 / 北师大 / 北科大 / 中传 等。
 * 接口：{@code GET {origin}/f/bilateralchosefair/ajax_frontBilateralchosefair?pageNo=N&pageSize=16}
 * 返回：{@code {state:1, object:{count, totalPage, lastPage, pageNo, list:[...]}}}。</p>
 *
 * <p>list 中直接含 {@code regStartTimeExport / regEndTimeExport}（企业报名起止），
 * 满足文档 4.2「严格区分报名开始 / 报名截止 / 举办时间」。</p>
 *
 * <p>双选会不采集参会企业名单（文档 4.3）：{@code company_names} 置空。</p>
 */
@Component
public class FrontpageCrawler {

    private static final Logger log = LoggerFactory.getLogger(FrontpageCrawler.class);

    /** 兼容旧 key BUAA_FRONTPAGE */
    private static final String KEY_FRONTPAGE = "FRONTPAGE";
    private static final String KEY_BUAA = "BUAA_FRONTPAGE";

    private static final String FAIR_API_PATH = "/f/bilateralchosefair/ajax_frontBilateralchosefair";
    /** 宣讲会接口（北航等 frontpage 平台） */
    private static final String TALK_API_PATH = "/f/recruitmentFair/ajax_frontRecruitfair";
    private static final String FAIR_LIST_PATH = "/frontpage/%s/html/bilateralchosefairList.html?type=4";

    private static final int MAX_PAGES = 10;
    private static final int PAGE_SIZE = 16;

    /** 部分站点 WAF 仅拦截桌面 UA（如北邮）：改用移动端 UA */
    private static final String MOBILE_UA =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 "
                    + "(KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1";

    private String uaFor(UniversitySite site) {
        return "BUPT".equalsIgnoreCase(site.getSchoolCode()) ? MOBILE_UA : null;
    }

    /** 正文中的报名截止（详情接口无独立字段） */
    private static final java.util.regex.Pattern REG_DEADLINE = java.util.regex.Pattern.compile(
            "(?:报名截止|报名截止时间|截止时间|请于|报名至)[^0-9]{0,12}(20\\d{2})\\s*[-/年.]\\s*(\\d{1,2})\\s*[-/月.]\\s*(\\d{1,2})");
    /** 正文中的报名开始 */
    private static final java.util.regex.Pattern REG_START = java.util.regex.Pattern.compile(
            "(?:报名开始|报名时间|报名起始)[^0-9]{0,12}(20\\d{2})\\s*[-/年.]\\s*(\\d{1,2})\\s*[-/月.]\\s*(\\d{1,2})");

    private final PageFetcher fetcher;
    private final ObjectMapper objectMapper;
    private final EventUpserter upserter;
    private final RegistrationStatusResolver registrationStatusResolver;
    private final CompanyResolver companyResolver;

    public FrontpageCrawler(PageFetcher fetcher,
                            ObjectMapper objectMapper,
                            EventUpserter upserter,
                            RegistrationStatusResolver registrationStatusResolver,
                            CompanyResolver companyResolver) {
        this.fetcher = fetcher;
        this.objectMapper = objectMapper;
        this.upserter = upserter;
        this.registrationStatusResolver = registrationStatusResolver;
        this.companyResolver = companyResolver;
    }

    public boolean supports(UniversitySite site) {
        if (site == null || site.getAdapterKey() == null) {
            return false;
        }
        String key = site.getAdapterKey();
        return KEY_FRONTPAGE.equalsIgnoreCase(key) || KEY_BUAA.equalsIgnoreCase(key);
    }

    /**
     * 采集该校双选会 + 宣讲会（前 N 页）。
     */
    public int crawl(UniversitySite site, Long taskId) {
        int fairs = crawlFairs(site, taskId);
        int talks = crawlTalks(site, taskId);
        log.info("{} 采集汇总：双选会 {} 条，宣讲会 {} 条", site.getSchoolName(), fairs, talks);
        return fairs + talks;
    }

    private int crawlFairs(UniversitySite site, Long taskId) {
        String origin = origin(site.getSiteUrl());
        if (origin == null) {
            log.warn("无法解析站点域名: {}", site.getSiteUrl());
            return 0;
        }
        String listPage = origin + String.format(FAIR_LIST_PATH, site.getSchoolCode() == null
                ? "html" : site.getSchoolCode().toLowerCase());
        int saved = 0;
        for (int page = 1; page <= MAX_PAGES; page++) {
            String url = origin + FAIR_API_PATH + "?pageNo=" + page + "&pageSize=" + PAGE_SIZE;
            String json = fetcher.fetch(url, url, uaFor(site));
            if (json == null || json.isBlank()) {
                log.info("{} 双选会接口无响应 page={}", site.getSchoolName(), page);
                break;
            }
            try {
                JsonNode root = objectMapper.readTree(json);
                JsonNode obj = root.path("object");
                JsonNode list = obj.path("list");
                if (!list.isArray() || list.size() == 0) {
                    break;
                }
                for (JsonNode n : list) {
                    try {
                        if (save(site, n, listPage, taskId)) {
                            saved++;
                        }
                    } catch (Exception e) {
                        log.debug("{} 单条解析失败: {}", site.getSchoolName(), e.getMessage());
                    }
                }
                log.info("{} 双选会 page={} 解析 {} 条，累计入库 {}",
                        site.getSchoolName(), page, list.size(), saved);
                if (obj.path("lastPage").asBoolean(false)) {
                    break;
                }
            } catch (Exception e) {
                log.warn("{} 双选会 JSON 解析失败 page={}: {}", site.getSchoolName(), page, e.getMessage());
                break;
            }
        }
        return saved;
    }

    private boolean save(UniversitySite site, JsonNode n, String listPage, Long taskId) {
        String title = text(n, "title");
        if (title == null || title.length() < 4) {
            return false;
        }
        // 部分站点（北理工/北邮/北大/农大）双选会接口 startTimeExport 为空，回退 startTime，
        // 否则 event_date 落空，未来场次无法进入时间窗口筛选
        String startTime = firstNonBlank(text(n, "startTimeExport"), text(n, "startTime"));
        LocalDate eventDate = parseDate(startTime);

        LocalDateTime regStart = parseDateTime(text(n, "regStartTimeExport"), false);
        LocalDateTime regEnd = parseDateTime(text(n, "regEndTimeExport"), true);

        EmploymentEvent e = new EmploymentEvent();
        e.setSchoolId(site.getId());
        e.setSchoolName(site.getSchoolName());
        e.setSchoolCity(site.getSchoolCity());
        e.setEventTitle(truncate(title, 500));
        e.setOriginalTitle(truncate(title, 500));
        e.setEventType("JOB_FAIR");
        e.setEventTypeOriginal("双选会");
        e.setOriginalCategory("双选会");
        e.setVenueType(resolveVenueType(text(n, "realPlace")));
        e.setStandardCategory("ON_CAMPUS_FAIR");
        e.setEventDate(eventDate);
        e.setStartTime(parseTime(startTime));
        e.setOriginalTimeText(truncate(startTime, 256));
        e.setVenueName(truncate(firstNonBlank(text(n, "realPlace"), text(n, "place")), 256));
        e.setEventAddress(truncate(text(n, "placeExport"), 512));
        // 双选会不采集参会企业名单（文档 4.3）
        e.setCompanyNames(null);
        e.setRegistrationStartAt(regStart);
        e.setRegistrationDeadline(regEnd);
        e.setRegistrationStatus(registrationStatusResolver.resolve(regStart, regEnd, null));
        e.setRegistrationUrl(truncate(text(n, "linkUrl"), 512));
        e.setEventStatus(mapStatus(text(n, "holdStatus")));
        e.setVerificationStatus("VERIFIED");
        e.setSourceListUrl(listPage);
        e.setSourceUrl(resolveDetailUrl(site, n));
        e.setRemoteId(truncate(text(n, "id"), 128));
        enrichIfUpcoming(origin(site.getSiteUrl()), n, e);
        return upserter.upsert(e, taskId);
    }

    /**
     * 活动详情页链接：优先 detailsUrl；部分站点（北科大 / 北大 / 北理工）该字段为空，
     * 但接口返回相对路径 url（/f/bilateralchosefair/show?bilateralchosefairId=...），
     * 拼接为完整详情链接 —— 即用人单位点击进入该活动的报名入口。
     */
    private String resolveDetailUrl(UniversitySite site, JsonNode n) {
        String detailsUrl = text(n, "detailsUrl");
        if (detailsUrl != null && !detailsUrl.isBlank()) {
            return truncate(detailsUrl, 1000);
        }
        String rel = text(n, "url");
        String origin = origin(site.getSiteUrl());
        if (rel == null || rel.isBlank() || origin == null) {
            return null;
        }
        return truncate(origin + (rel.startsWith("/") ? rel : "/" + rel), 1000);
    }

    /**
     * 仅对未结束的活动抓详情（content 含报名信息），避免对历史数据无效请求。
     */
    private void enrichIfUpcoming(String origin, JsonNode n, EmploymentEvent e) {
        String status = e.getEventStatus();
        if (origin == null || (!"UPCOMING".equals(status) && !"ONGOING".equals(status))) {
            return;
        }
        String id = text(n, "id");
        if (id == null) {
            return;
        }
        String url = origin + "/f/bilateralchosefair/ajax_show?bilateralchosefairId=" + id;
        String json = fetcher.fetch(url, url);
        if (json == null || json.isBlank()) {
            return;
        }
        try {
            JsonNode bf = objectMapper.readTree(json).path("object").path("bilateralchosefair");
            String content = bf.path("content").asText("");
            if (content.isBlank()) {
                return;
            }
            java.util.regex.Matcher md = REG_DEADLINE.matcher(content);
            if (md.find()) {
                LocalDateTime d = parseDateTime(md.group(1) + "-" + md.group(2) + "-" + md.group(3), true);
                if (d != null) {
                    e.setRegistrationDeadline(d);
                }
            }
            java.util.regex.Matcher ms = REG_START.matcher(content);
            if (ms.find()) {
                LocalDateTime s = parseDateTime(ms.group(1) + "-" + ms.group(2) + "-" + ms.group(3), false);
                if (s != null) {
                    e.setRegistrationStartAt(s);
                }
            }
            String organizer = bf.path("organizer").asText("");
            if (!organizer.isBlank()) {
                e.setOrganizer(truncate(organizer, 256));
            }
            String entity = bf.path("undertakeUnit").asText("");
            if (!entity.isBlank() && e.getOrganizer() == null) {
                e.setOrganizer(truncate(entity, 256));
            }
            e.setEventDescription(truncate(content, 60000));
            e.setRegistrationStatus(registrationStatusResolver.resolve(
                    e.getRegistrationStartAt(), e.getRegistrationDeadline(), e.getEventStatus()));
        } catch (Exception ex) {
            log.debug("详情补全失败 id={}: {}", id, ex.getMessage());
        }
    }

    /**
     * 采集宣讲会（重点企业宣讲定位）。
     */
    private int crawlTalks(UniversitySite site, Long taskId) {
        String origin = origin(site.getSiteUrl());
        if (origin == null) {
            return 0;
        }
        String listPage = origin + "/frontpage/" + schoolSlug(site) + "/html/recruitmentFairList.html?type=3";
        int saved = 0;
        for (int page = 1; page <= MAX_PAGES; page++) {
            String url = origin + TALK_API_PATH + "?pageNo=" + page + "&pageSize=" + PAGE_SIZE;
            String json = fetcher.fetch(url, url, uaFor(site));
            if (json == null || json.isBlank()) {
                break;
            }
            try {
                JsonNode obj = objectMapper.readTree(json).path("object");
                JsonNode list = obj.path("list");
                if (!list.isArray() || list.size() == 0) {
                    break;
                }
                for (JsonNode n : list) {
                    try {
                        if (saveTalk(site, n, listPage, taskId)) {
                            saved++;
                        }
                    } catch (Exception e) {
                        log.debug("{} 宣讲会单条失败: {}", site.getSchoolName(), e.getMessage());
                    }
                }
                if (obj.path("lastPage").asBoolean(false)) {
                    break;
                }
            } catch (Exception e) {
                log.warn("{} 宣讲会 JSON 解析失败 page={}: {}", site.getSchoolName(), page, e.getMessage());
                break;
            }
        }
        return saved;
    }

    private boolean saveTalk(UniversitySite site, JsonNode n, String listPage, Long taskId) {
        String title = text(n, "title");
        if (title == null || title.length() < 4) {
            return false;
        }
        String startTime = text(n, "startTime");
        String endTime = text(n, "endTime");
        LocalDate eventDate = parseDate(startTime);

        EmploymentEvent e = new EmploymentEvent();
        e.setSchoolId(site.getId());
        e.setSchoolName(site.getSchoolName());
        e.setSchoolCity(site.getSchoolCity());
        e.setEventTitle(truncate(title, 500));
        e.setOriginalTitle(truncate(title, 500));
        e.setEventType("TALK");
        e.setEventTypeOriginal("宣讲会");
        e.setOriginalCategory("宣讲会");
        e.setVenueType("ON_CAMPUS");
        e.setStandardCategory("ON_CAMPUS_TALK");
        e.setEventDate(eventDate);
        e.setStartTime(parseTime(startTime));
        e.setEndTime(parseTime(endTime));
        e.setOriginalTimeText(truncate(startTime, 256));
        String place = firstNonBlank(text(n, "place"), text(n, "campusLabel"));
        e.setVenueName(truncate(place, 256));
        e.setEventAddress(truncate(text(n, "campus"), 512));
        e.setRegistrationStatus("NOT_PUBLISHED");
        e.setEventStatus(mapStatus(text(n, "holdStatus")));
        e.setSourceListUrl(listPage);
        String rel = text(n, "url");
        e.setSourceUrl(rel == null ? null : truncate(origin(site.getSiteUrl()) + rel, 1000));
        e.setRemoteId(truncate(text(n, "id"), 128));

        // 重点企业识别（只统计可确认属于目标企业的宣讲）
        CompanyResolver.Match m = companyResolver.resolve(title, null);
        if (m != null) {
            e.setCompanyId(m.getCompanyId());
            e.setCompanyName(m.getCompanyName());
            e.setCompanyCategory(m.getCompanyCategory());
            e.setRecruitmentEntity(m.getRecruitmentEntity());
            e.setVerificationStatus("VERIFIED");
        } else {
            e.setVerificationStatus("NEED_REVIEW");
        }
        return upserter.upsert(e, taskId);
    }

    private String schoolSlug(UniversitySite site) {
        String code = site.getSchoolCode();
        return code == null ? "html" : code.toLowerCase();
    }

    private String resolveVenueType(String place) {
        if (place == null) {
            return "UNKNOWN";
        }
        if (place.contains("线上") || place.contains("网络")) {
            return "ONLINE";
        }
        return "ON_CAMPUS";
    }

    private String mapStatus(String holdStatus) {
        if (holdStatus == null) {
            return "UNKNOWN";
        }
        if (holdStatus.contains("结束")) {
            return "FINISHED";
        }
        if (holdStatus.contains("进行中")) {
            return "ONGOING";
        }
        if (holdStatus.contains("未开始") || holdStatus.contains("报名中")) {
            return "UPCOMING";
        }
        return "UNKNOWN";
    }

    private String origin(String siteUrl) {
        if (siteUrl == null || siteUrl.isBlank()) {
            return null;
        }
        try {
            URI u = URI.create(siteUrl.trim());
            if (u.getScheme() == null || u.getHost() == null) {
                return null;
            }
            String port = u.getPort() > 0 ? ":" + u.getPort() : "";
            return u.getScheme() + "://" + u.getHost() + port;
        } catch (Exception e) {
            return null;
        }
    }

    private String text(JsonNode n, String field) {
        JsonNode v = n.path(field);
        if (v.isMissingNode() || v.isNull()) {
            return null;
        }
        String s = v.asText();
        return s == null || s.isBlank() ? null : s.trim();
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.length() < 10) {
            return null;
        }
        try {
            return LocalDate.parse(s.substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }

    private String parseTime(String s) {
        if (s == null || s.length() < 16) {
            return null;
        }
        String t = s.substring(11, 16);
        return t.matches("\\d{2}:\\d{2}") ? t : null;
    }

    private LocalDateTime parseDateTime(String s, boolean endOfDay) {
        if (s == null || s.isBlank() || s.length() < 10) {
            return null;
        }
        try {
            if (s.length() >= 16) {
                return LocalDateTime.parse(s.substring(0, 16).replace(' ', 'T'));
            }
            LocalDate d = LocalDate.parse(s.substring(0, 10));
            return endOfDay ? d.atTime(23, 59) : d.atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    private String firstNonBlank(String a, String b) {
        return a != null && !a.isBlank() ? a : b;
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
