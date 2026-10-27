package org.example.crawler.adapter;

import org.example.crawler.classify.CompanyResolver;
import org.example.crawler.classify.RegistrationStatusResolver;
import org.example.crawler.parse.EventParser;
import org.example.crawler.persist.EventUpserter;
import org.example.crawler.render.PlaywrightRenderer;
import org.example.supplyradar.entity.EmploymentEvent;
import org.example.supplyradar.entity.UniversitySite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用 Playwright HTML 采集器（WAF/SPA 站点，如北邮 412、人大 SPA）。
 *
 * <p>用真实浏览器渲染公开列表页 → 交给 {@link EventParser#parseList(String)} 做通用卡片解析。
 * 不使用截图 OCR（易错、不可追溯）；仅访问公开页面，等同正常浏览。</p>
 */
@Component
public class PlaywrightHtmlCrawler {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightHtmlCrawler.class);

    private static final String ADAPTER_KEY = "PLAYWRIGHT_HTML";

    /** 各校公开列表页（双选会 / 宣讲会） */
    private static final Map<String, List<String>> PAGES = new HashMap<>();

    /** 人大“近期活动”文本块：类型 标题 时间|日期 举办地点|地点 */
    private static final Pattern RUC_ROW = Pattern.compile(
            "(宣讲会|双选会|职业指导活动)\\s*(.{4,100}?)\\s*时间\\s*[|｜]\\s*(20\\d{2}-\\d{2}-\\d{2})"
                    + "[\\s\\S]{0,80}?举办地点\\s*[|｜]\\s*([^\\s|｜]{2,60})");

    /**
     * 日历控件噪声：整页文本解析会把日历格里的活动类型词拼成一条标题
     * （如“宣讲会 职业指导活动 31 职业指导活动 1 2 宣讲会…”），需剔除。
     */
    private static final Pattern TYPE_WORD = Pattern.compile(
            "宣讲会|双选会|职业指导活动|招聘会|招聘活动");

    static {
        PAGES.put("BUPT", Arrays.asList(
                "https://job.bupt.edu.cn/frontpage/bupt/html/bilateralchosefairList.html",
                "https://job.bupt.edu.cn/frontpage/bupt/html/recruitmentFairList.html?type=3"));
        PAGES.put("RUC", Arrays.asList(
                "https://career.ruc.edu.cn/"));
    }

    private final PlaywrightRenderer renderer;
    private final EventParser parser;
    private final EventUpserter upserter;
    private final CompanyResolver companyResolver;
    private final RegistrationStatusResolver registrationStatusResolver;

    public PlaywrightHtmlCrawler(PlaywrightRenderer renderer,
                                 EventParser parser,
                                 EventUpserter upserter,
                                 CompanyResolver companyResolver,
                                 RegistrationStatusResolver registrationStatusResolver) {
        this.renderer = renderer;
        this.parser = parser;
        this.upserter = upserter;
        this.companyResolver = companyResolver;
        this.registrationStatusResolver = registrationStatusResolver;
    }

    public boolean supports(UniversitySite site) {
        return site != null && ADAPTER_KEY.equalsIgnoreCase(site.getAdapterKey());
    }

    public int crawl(UniversitySite site, Long taskId) {
        List<String> urls = PAGES.get(site.getSchoolCode());
        if (urls == null || urls.isEmpty()) {
            log.warn("{} 未配置 Playwright 列表页", site.getSchoolName());
            return 0;
        }
        if (!renderer.isAvailable()) {
            log.warn("Playwright 不可用：{}", renderer.getUnavailableReason());
            return 0;
        }
        int saved = 0;
        for (String url : urls) {
            boolean isRuc = "RUC".equalsIgnoreCase(site.getSchoolCode());
            // 人大：先渲染首页（近期活动首屏），再滚动加载补充（合并去重靠 upsert）
            String html = renderer.render(url);
            if (html == null || html.isBlank()) {
                log.warn("{} 渲染失败: {}", site.getSchoolName(), url);
                continue;
            }
            saved += parseHtml(site, html, url, taskId, isRuc);
            if (isRuc) {
                // 浏览器上下文内直接 POST 拉全量（真实参数：current/size）
                String fullBody = renderer.postJsonInPage(url,
                        "/open/api/activity/guiding/list", "{current:1,size:500}");
                if (fullBody != null && !fullBody.isBlank()) {
                    saved += parseRucJson(site, fullBody,
                            "/open/api/activity/guiding/list", taskId);
                }
                // 捕获 SPA 真实接口（/open/api/activity/*），用于后续结构化解析
                for (String item : renderer.captureJson(url, "/open/api/activity", 3000)) {
                    int sep = item.indexOf("||");
                    String apiUrl = sep > 0 ? item.substring(0, sep) : "";
                    String body = sep > 0 ? item.substring(sep + 2) : item;
                    log.info("RUC-API {} :: {}", apiUrl,
                            body.length() > 500 ? body.substring(0, 500) : body);
                    saved += parseRucJson(site, body, apiUrl, taskId);
                }
                String scrolled = renderer.renderScrolled(url, 8);
                if (scrolled != null && !scrolled.isBlank()) {
                    saved += parseHtml(site, scrolled, url, taskId, true);
                }
            }
        }
        return saved;
    }

    private int parseHtml(UniversitySite site, String html, String url, Long taskId, boolean rucFallback) {
        int saved = 0;
        List<EventParser.ParsedEvent> list = parser.parseList(html);
        log.info("{} 渲染解析: {} 候选（{}）", site.getSchoolName(), list.size(), url);
        for (EventParser.ParsedEvent pe : list) {
            try {
                if (save(site, pe, url, taskId)) {
                    saved++;
                }
            } catch (Exception e) {
                log.debug("{} 单条入库失败: {}", site.getSchoolName(), e.getMessage());
            }
        }
        if (rucFallback) {
            saved += parseByTextBlock(site, html, url, taskId);
        }
        return saved;
    }

    /**
     * 文本框解析（人大“近期活动”列表）。
     */
    private int parseByTextBlock(UniversitySite site, String html, String listUrl, Long taskId) {
        String text = org.jsoup.Jsoup.parse(html).text();
        Matcher m = RUC_ROW.matcher(text);
        int saved = 0;
        java.util.Set<String> seen = new java.util.HashSet<>();
        while (m.find()) {
            try {
                String type = m.group(1);
                String title = m.group(2).trim();
                String dateStr = m.group(3);
                String place = m.group(4).trim();
                if (title.length() < 4 || !seen.add(title + "|" + dateStr)) {
                    continue;
                }
                if (looksLikeCalendarNoise(title)) {
                    log.info("{} 跳过日历拼接标题: {}", site.getSchoolName(), truncate(title, 60));
                    continue;
                }
                boolean isFair = "双选会".equals(type);
                EmploymentEvent e = new EmploymentEvent();
                e.setSchoolId(site.getId());
                e.setSchoolName(site.getSchoolName());
                e.setSchoolCity(site.getSchoolCity());
                e.setEventTitle(truncate(title, 500));
                e.setOriginalTitle(truncate(title, 500));
                e.setEventType(isFair ? "JOB_FAIR" : ("职业指导活动".equals(type) ? "EMPLOYMENT_SERVICE" : "TALK"));
                e.setEventTypeOriginal(type);
                e.setOriginalCategory("近期活动");
                e.setVenueType(place.contains("线上") ? "ONLINE" : "ON_CAMPUS");
                e.setStandardCategory(isFair ? "ON_CAMPUS_FAIR" : "ON_CAMPUS_TALK");
                e.setEventDate(java.time.LocalDate.parse(dateStr));
                e.setVenueName(truncate(place, 256));
                e.setRegistrationStatus("NOT_PUBLISHED");
                e.setEventStatus(registrationStatusResolver.resolveEventStatus(java.time.LocalDate.parse(dateStr)));
                e.setSourceListUrl(truncate(listUrl, 500));
                if (!isFair) {
                    CompanyResolver.Match match = companyResolver.resolve(title, null);
                    if (match != null) {
                        e.setCompanyId(match.getCompanyId());
                        e.setCompanyName(match.getCompanyName());
                        e.setCompanyCategory(match.getCompanyCategory());
                        e.setRecruitmentEntity(match.getRecruitmentEntity());
                        e.setVerificationStatus("VERIFIED");
                    } else {
                        e.setVerificationStatus("NEED_REVIEW");
                    }
                } else {
                    e.setVerificationStatus("PENDING");
                }
                if (upserter.upsert(e, taskId)) {
                    saved++;
                }
            } catch (Exception ex) {
                log.debug("人大文本块解析失败: {}", ex.getMessage());
            }
        }
        log.info("人大文本块解析: {} 条", saved);
        return saved;
    }

    /**
     * 日历拼接噪声判定：同一标题出现 ≥3 个活动类型词，视为日历格文字被拼接，
     * 正常活动名最多包含 1 个类型词。
     */
    private boolean looksLikeCalendarNoise(String title) {
        if (title == null) {
            return false;
        }
        Matcher m = TYPE_WORD.matcher(title);
        int hits = 0;
        while (m.find()) {
            hits++;
            if (hits >= 3) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析人大 SPA 接口 JSON（结构：data[] 或 data.list[]）。
     */
    private int parseRucJson(UniversitySite site, String body, String apiUrl, Long taskId) {
        int saved = 0;
        int dup = 0;
        int skipTitle = 0;
        int skipDate = 0;
        int skipErr = 0;
        int total = 0;
        try {
            com.fasterxml.jackson.databind.JsonNode root =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
            com.fasterxml.jackson.databind.JsonNode arr = root.path("data");
            if (arr.isObject()) {
                // 人大：data.records（guiding/list）/ data.list；兼容其他结构
                if (arr.has("records")) {
                    arr = arr.path("records");
                } else if (arr.has("list")) {
                    arr = arr.path("list");
                } else {
                    arr = arr.path("records");
                }
            }
            if (!arr.isArray()) {
                log.info("RUC-PARSE {}: data not array, bodyLen={}", apiUrl, body.length());
                return 0;
            }
            total = arr.size();
            for (com.fasterxml.jackson.databind.JsonNode n : arr) {
                try {
                    String title = firstText(n, "title", "name", "activityName");
                    String dateStr = firstText(n, "startTime", "startDate", "date", "activityDate", "endTime");
                    if (title == null || title.length() < 4) {
                        skipTitle++;
                        continue;
                    }
                    if (dateStr == null || dateStr.length() < 8) {
                        skipDate++;
                        continue;
                    }
                    String dateOnly = normalizeDate(dateStr);
                    if (dateOnly == null) {
                        skipDate++;
                        continue;
                    }
                    java.time.LocalDate date = java.time.LocalDate.parse(dateOnly);
                    String place = firstText(n, "location", "place", "address", "venue");
                    String type = firstText(n, "type", "typeName", "activityType");
                    String state = firstText(n, "state", "status");
                    boolean isFair = type != null && (type.contains("双选") || type.contains("mutual"));

                    EmploymentEvent e = new EmploymentEvent();
                    e.setSchoolId(site.getId());
                    e.setSchoolName(site.getSchoolName());
                    e.setSchoolCity(site.getSchoolCity());
                    e.setEventTitle(truncate(title, 500));
                    e.setOriginalTitle(truncate(title, 500));
                    e.setEventType(isFair ? "JOB_FAIR" : "TALK");
                    e.setEventTypeOriginal(type);
                    e.setOriginalCategory("近期活动");
                    e.setVenueType(place != null && place.contains("线上") ? "ONLINE" : "ON_CAMPUS");
                    e.setStandardCategory(isFair ? "ON_CAMPUS_FAIR" : "ON_CAMPUS_TALK");
                    e.setEventDate(date);
                    e.setVenueName(truncate(place, 256));
                    e.setRegistrationStatus(mapRucState(state));
                    e.setEventStatus(registrationStatusResolver.resolveEventStatus(date));
                    e.setSourceListUrl(truncate(apiUrl, 500));
                    String id = firstText(n, "id", "activityId");
                    e.setRemoteId(truncate(id, 128));
                    if (!isFair) {
                        CompanyResolver.Match match = companyResolver.resolve(title, null);
                        if (match != null) {
                            e.setCompanyId(match.getCompanyId());
                            e.setCompanyName(match.getCompanyName());
                            e.setCompanyCategory(match.getCompanyCategory());
                            e.setRecruitmentEntity(match.getRecruitmentEntity());
                            e.setVerificationStatus("VERIFIED");
                        } else {
                            e.setVerificationStatus("NEED_REVIEW");
                        }
                    } else {
                        e.setVerificationStatus("PENDING");
                    }
                    if (upserter.upsert(e, taskId)) {
                        saved++;
                    } else {
                        dup++;
                    }
                } catch (Exception ex) {
                    skipErr++;
                    log.info("RUC-ROW-ERR: {}", ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("RUC-JSON-FAIL api={} bodyLen={}: {}", apiUrl, body.length(), e.getMessage());
        }
        if (total > 0) {
            log.info("RUC-PARSE {}: total={} saved={} dup={} skipTitle={} skipDate={} skipErr={}",
                    apiUrl, total, saved, dup, skipTitle, skipDate, skipErr);
        } else {
            log.info("RUC-PARSE {}: arr not array, bodyLen={}", apiUrl, body.length());
        }
        return saved;
    }

    /**
     * 日期归一化：兼容 2026-11-15 / 2026-11-15 12:45:00 / 2026/11/15。
     */
    private String normalizeDate(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim().replace('/', '-');
        if (s.length() >= 10) {
            String head = s.substring(0, 10);
            if (head.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return head;
            }
        }
        return null;
    }

    /**
     * 人大 state 映射为报名状态（文档 4.4）。
     */
    private String mapRucState(String state) {
        if (state == null) {
            return "NOT_PUBLISHED";
        }
        String s = state.toLowerCase();
        if (s.contains("not_open")) {
            return "NOT_OPEN";
        }
        if (s.contains("open") || s.contains("enroll")) {
            return "OPEN";
        }
        if (s.contains("close") || s.contains("end")) {
            return "CLOSED";
        }
        if (s.contains("cancel")) {
            return "CANCELLED";
        }
        return "NOT_PUBLISHED";
    }

    private String firstText(com.fasterxml.jackson.databind.JsonNode n, String... fields) {
        for (String f : fields) {
            com.fasterxml.jackson.databind.JsonNode v = n.path(f);
            if (!v.isMissingNode() && !v.isNull()) {
                String s = v.asText();
                if (s != null && !s.isBlank()) {
                    return s.trim();
                }
            }
        }
        return null;
    }

    private boolean save(UniversitySite site, EventParser.ParsedEvent pe, String listUrl, Long taskId) {
        if (pe.title == null || pe.title.length() < 4) {
            return false;
        }
        boolean isFair = pe.title.contains("双选") || pe.title.contains("招聘会");
        EmploymentEvent e = new EmploymentEvent();
        e.setSchoolId(site.getId());
        e.setSchoolName(site.getSchoolName());
        e.setSchoolCity(site.getSchoolCity());
        e.setEventTitle(truncate(pe.title, 500));
        e.setOriginalTitle(truncate(pe.originalTitle != null ? pe.originalTitle : pe.title, 500));
        e.setEventType(isFair ? "JOB_FAIR" : "TALK");
        e.setEventTypeOriginal(isFair ? "招聘会" : "宣讲会");
        e.setVenueType(pe.venueName != null && pe.venueName.contains("线上") ? "ONLINE" : "ON_CAMPUS");
        e.setStandardCategory(isFair ? "ON_CAMPUS_FAIR" : "ON_CAMPUS_TALK");
        e.setEventDate(pe.eventDate);
        e.setStartTime(pe.startTime);
        e.setEndTime(pe.endTime);
        e.setVenueName(truncate(pe.venueName, 256));
        e.setEventAddress(truncate(pe.address, 512));
        // 报名链路完整字段（防重复采集丢字段 / 前端“暂未公布”误显示）
        e.setRegistrationDeadline(pe.registrationDeadline);
        e.setRegistrationStartAt(pe.registrationStartAt);
        e.setRegistrationMethod(truncate(pe.registrationMethod, 256));
        e.setRegistrationRequirements(truncate(pe.registrationRequirements, 1000));
        e.setRegistrationStatus(registrationStatusResolver.resolve(
                pe.registrationStartAt, pe.registrationDeadline, null));
        e.setEventStatus(registrationStatusResolver.resolveEventStatus(pe.eventDate));
        e.setSourceListUrl(truncate(listUrl, 500));
        e.setSourceUrl(truncate(pe.sourceUrl, 1000));
        e.setEventDescription(truncate(pe.text, 60000));

        if (!isFair) {
            CompanyResolver.Match m = companyResolver.resolve(pe.title, pe.text);
            if (m != null) {
                e.setCompanyId(m.getCompanyId());
                e.setCompanyName(m.getCompanyName());
                e.setCompanyCategory(m.getCompanyCategory());
                e.setRecruitmentEntity(m.getRecruitmentEntity());
                e.setVerificationStatus("VERIFIED");
            } else {
                e.setVerificationStatus("NEED_REVIEW");
            }
        } else {
            e.setVerificationStatus("PENDING");
        }
        return upserter.upsert(e, taskId);
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
