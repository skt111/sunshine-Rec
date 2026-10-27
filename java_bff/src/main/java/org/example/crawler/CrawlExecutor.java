package org.example.crawler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.crawler.adapter.FrontpageCrawler;
import org.example.crawler.adapter.PlaywrightHtmlCrawler;
import org.example.crawler.adapter.ThuCicCrawler;
import org.example.crawler.classify.CompanyResolver;
import org.example.crawler.classify.EventClassifier;
import org.example.crawler.classify.RegistrationStatusResolver;
import org.example.crawler.config.CrawlProperties;
import org.example.crawler.discover.ColumnDiscoverer;
import org.example.crawler.fetch.PageFetcher;
import org.example.crawler.parse.EventParser;
import org.example.crawler.persist.EventUpserter;
import org.example.crawler.render.PlaywrightRenderer;
import org.example.supplyradar.entity.CrawlTask;
import org.example.supplyradar.enums.EventType;
import org.example.supplyradar.entity.EmploymentEvent;
import org.example.supplyradar.entity.UniversitySite;
import org.example.supplyradar.enums.CrawlTaskStatus;
import org.example.supplyradar.enums.SchoolCrawlStatus;
import org.example.supplyradar.mapper.CrawlTaskMapper;
import org.example.supplyradar.mapper.UniversitySiteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 采集执行器：实现 {@link CrawlTrigger}，异步执行单次采集任务。
 *
 * <p>单校失败隔离；每活动 upsert + history 同事务；任务在 finally 中收敛为终态。</p>
 */
@Component
@Primary
public class CrawlExecutor implements CrawlTrigger {

    private static final Logger log = LoggerFactory.getLogger(CrawlExecutor.class);
    private static final int MAX_DETAIL_PER_SCHOOL = 40;
    /** 单校最多扫描栏数（文档 4.1 + 栏目收敛提速） */
    private static final int MAX_COLUMNS = 5;
    private static final Pattern TEMPLATE_MARK = Pattern.compile("\\{\\{|\\}\\}|\\$\\{");
    private static final int MIN_VISIBLE_TEXT = 1200;
    /** 外部平台内部 ID（用于增量更新定位，P2-4） */
    private static final Pattern REMOTE_ID = Pattern.compile(
            "[?&](?:id|Id|ID|activityId|newsId|infoId|fairId)=([A-Za-z0-9_-]{2,64})");
    /** 活动标题词表单一来源：EventParser#EVENT_TITLE_WORDS（P2-6：本类不再重复维护） */

    private final CrawlProperties props;
    private final PageFetcher fetcher;
    private final ColumnDiscoverer discoverer;
    private final EventParser parser;
    private final EventClassifier classifier;
    private final EventUpserter upserter;
    private final PlaywrightRenderer renderer;
    private final CompanyResolver companyResolver;
    private final RegistrationStatusResolver registrationStatusResolver;
    private final FrontpageCrawler frontpageCrawler;
    private final ThuCicCrawler thuCicCrawler;
    private final PlaywrightHtmlCrawler playwrightHtmlCrawler;
    private final UniversitySiteMapper siteMapper;
    private final CrawlTaskMapper taskMapper;
    private final ThreadPoolTaskExecutor crawlTaskExecutor;

    public CrawlExecutor(CrawlProperties props, PageFetcher fetcher, ColumnDiscoverer discoverer,
                         EventParser parser, EventClassifier classifier, EventUpserter upserter,
                         PlaywrightRenderer renderer,
                         CompanyResolver companyResolver,
                         RegistrationStatusResolver registrationStatusResolver,
                         FrontpageCrawler frontpageCrawler,
                         ThuCicCrawler thuCicCrawler,
                         PlaywrightHtmlCrawler playwrightHtmlCrawler,
                         UniversitySiteMapper siteMapper, CrawlTaskMapper taskMapper,
                         ThreadPoolTaskExecutor crawlTaskExecutor) {
        this.props = props;
        this.fetcher = fetcher;
        this.discoverer = discoverer;
        this.parser = parser;
        this.classifier = classifier;
        this.upserter = upserter;
        this.renderer = renderer;
        this.companyResolver = companyResolver;
        this.registrationStatusResolver = registrationStatusResolver;
        this.frontpageCrawler = frontpageCrawler;
        this.thuCicCrawler = thuCicCrawler;
        this.playwrightHtmlCrawler = playwrightHtmlCrawler;
        this.siteMapper = siteMapper;
        this.taskMapper = taskMapper;
        this.crawlTaskExecutor = crawlTaskExecutor;
    }

    @Override
    public TaskTriggerResult trigger(Long taskId, String taskType, List<Long> schoolIds) {
        crawlTaskExecutor.execute(() -> runTask(taskId, taskType, schoolIds));
        return TaskTriggerResult.ok();
    }

    /**
     * 异步任务体。
     */
    void runTask(Long taskId, String taskType, List<Long> schoolIds) {
        CrawlTask task = taskId == null ? null : taskMapper.selectById(taskId);
        if (task != null) {
            task.setStatus(CrawlTaskStatus.RUNNING.getCode());
            task.setStartedAt(LocalDateTime.now());
            taskMapper.updateById(task);
        }

        int success = 0;
        int fail = 0;
        List<String> errors = new ArrayList<>();
        List<UniversitySite> schools = loadSchools(taskType, schoolIds);
        int limit = Math.min(schools.size(), props.getMaxSchoolsPerTask());

        for (int i = 0; i < limit; i++) {
            UniversitySite site = schools.get(i);
            try {
                int saved = crawlSchool(site, taskId);
                site.setLastCrawlAt(LocalDateTime.now());
                site.setUpdatedAt(LocalDateTime.now());
                if (saved > 0) {
                    site.setCrawlStatus(SchoolCrawlStatus.SUCCESS.getCode());
                    site.setLastFailReason(null);
                    success++;
                    log.info("高校采集完成: {} 新增/更新 {} 条", site.getSchoolName(), saved);
                } else {
                    // P1-1: 0 条不记成功（可能站点改版/接口受限/解析全失败）
                    site.setCrawlStatus(SchoolCrawlStatus.PARTIAL.getCode());
                    site.setLastFailReason("采集 0 条（无新活动或解析失败，需复核）");
                    fail++;
                    errors.add(site.getSchoolName() + " -> 采集 0 条");
                    log.warn("高校采集 0 条（需复核）: {}", site.getSchoolName());
                }
                siteMapper.updateById(site);
            } catch (Exception e) {
                fail++;
                String reason = e.getClass().getSimpleName() + ": " + e.getMessage();
                errors.add(site.getSchoolName() + " -> " + reason);
                log.warn("高校采集失败: {} - {}", site.getSchoolName(), reason);
                try {
                    site.setCrawlStatus(SchoolCrawlStatus.FAILED.getCode());
                    site.setLastCrawlAt(LocalDateTime.now());
                    site.setLastFailReason(truncate(reason, 1000));
                    site.setUpdatedAt(LocalDateTime.now());
                    siteMapper.updateById(site);
                } catch (Exception ignore) {
                    log.warn("回写高校采集状态失败: {}", site.getSchoolName());
                }
            }
        }

        if (task != null) {
            task.setStatus(resolveTaskStatus(success, fail));
            task.setFinishedAt(LocalDateTime.now());
            task.setTotalCount(limit);
            task.setSuccessCount(success);
            task.setFailCount(fail);
            task.setErrorSummary(errors.isEmpty() ? null : truncate(String.join("; ", errors), 4000));
            taskMapper.updateById(task);
        }
    }

    private int crawlSchool(UniversitySite site, Long taskId) {
        // 站点专用适配器优先
        if (frontpageCrawler.supports(site)) {
            int saved = frontpageCrawler.crawl(site, taskId);
            log.info("适配器采集完成 {}: {} 条", site.getSchoolName(), saved);
            return saved;
        }
        if (thuCicCrawler.supports(site)) {
            int saved = thuCicCrawler.crawl(site, taskId);
            log.info("清华适配器采集完成: {} 条", saved);
            return saved;
        }
        if (playwrightHtmlCrawler.supports(site)) {
            int saved = playwrightHtmlCrawler.crawl(site, taskId);
            log.info("Playwright 适配器采集完成 {}: {} 条", site.getSchoolName(), saved);
            return saved;
        }
        String homeUrl = site.getSiteUrl();
        if (homeUrl == null || homeUrl.isBlank()) {
            throw new IllegalStateException("siteUrl 为空");
        }
        String homeHtml = fetcher.fetch(homeUrl, homeUrl);
        if (homeHtml == null) {
            throw new IllegalStateException("首页不可达");
        }
        // JS 渲染站点（前端模板未渲染）→ 尝试 Playwright 渲染
        if (isLikelySpa(homeHtml)) {
            homeHtml = renderOrFail(homeUrl);
        }

        List<String> columns = discoverer.discoverColumns(homeHtml, homeUrl);
        if (columns.isEmpty() && renderer.isAvailable()) {
            String rendered = renderer.render(homeUrl);
            if (rendered != null && !rendered.isBlank()) {
                homeHtml = rendered;
                columns = discoverer.discoverColumns(rendered, homeUrl);
            }
        }
        if (columns.isEmpty()) {
            columns.add(homeUrl);
        } else if (columns.size() > MAX_COLUMNS) {
            // 栏目收敛：只取优先级最高的前 N 个（招聘会/宣讲会/组团招聘优先）
            columns = new ArrayList<>(columns.subList(0, MAX_COLUMNS));
        }

        int saved = 0;
        for (String column : columns) {
            String listHtml = column.equals(homeUrl) ? homeHtml : fetchPage(column, column);
            if (listHtml == null) {
                continue;
            }
            List<String> links = discoverer.discoverEventLinks(listHtml, column);
            // 结果驱动降级：静态拿不到足够条目 → 渲染重试
            if (links.size() < 3 && renderer.isAvailable()) {
                String rendered = renderer.render(column);
                if (rendered != null && !rendered.isBlank()) {
                    listHtml = rendered;
                    links = discoverer.discoverEventLinks(rendered, column);
                }
            }
            // 列表页直接解析（适用于列表项无有效详情链接的站点，如 javascript:void(0)）
            if (links.size() < 3) {
                List<EventParser.ParsedEvent> listEvents = parser.parseList(listHtml);
                for (EventParser.ParsedEvent pe : listEvents) {
                    try {
                        saved += saveParsed(site, column, pe, pe.sourceUrl, taskId);
                    } catch (Exception e) {
                        log.debug("列表解析入库失败: {}", e.getMessage());
                    }
                }
            }
            int processed = 0;
            for (String link : links) {
                if (processed++ >= MAX_DETAIL_PER_SCHOOL) {
                    break;
                }
                try {
                    saved += crawlDetail(site, column, link, taskId);
                } catch (Exception e) {
                    log.debug("详情解析失败 {}: {}", link, e.getMessage());
                }
            }
        }
        return saved;
    }

    private int crawlDetail(UniversitySite site, String listUrl, String detailUrl, Long taskId) {
        String html = fetchPage(detailUrl, listUrl);
        if (html == null) {
            return 0;
        }
        // 详情页模板未渲染则尝试渲染（已在 fetchPage 内部处理）；仍为模板则跳过
        if (isLikelySpa(html)) {
            log.debug("详情页疑似 JS 渲染，跳过: {}", detailUrl);
            return 0;
        }
        EventParser.ParsedEvent parsed = parser.parse(html, listUrl);
        return saveParsed(site, listUrl, parsed, detailUrl, taskId);
    }

    /**
     * 从 URL 提取外部平台内部 ID（P2-4）。
     */
    private String extractRemoteId(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        Matcher m = REMOTE_ID.matcher(url);
        if (m.find()) {
            return m.group(1);
        }
        String clean = url.replaceAll("[?#].*$", "");
        int idx = clean.lastIndexOf('/');
        if (idx >= 0 && idx < clean.length() - 1) {
            String seg = clean.substring(idx + 1);
            if (seg.matches("[A-Za-z0-9_-]{4,64}") && !seg.contains(".")) {
                return seg;
            }
        }
        return null;
    }

    /**
     * 统一组装并入库（详情解析与列表解析共用，保证字段与表结构一致）。
     */
    private int saveParsed(UniversitySite site, String listUrl,
                          EventParser.ParsedEvent parsed, String detailUrl, Long taskId) {
        if (parsed == null || parsed.title == null || parsed.title.isBlank()) {
            return 0;
        }
        // 模板占位符不得入库
        if (TEMPLATE_MARK.matcher(parsed.title).find()) {
            log.debug("标题含模板占位符，跳过: {}", parsed.title);
            return 0;
        }
        // 活动性校验
        if (!parser.isEventTitle(parsed.title)) {
            log.info("标题非活动性质，跳过: {}", parsed.title);
            return 0;
        }

        EventClassifier.Classification c = classifier.classify(
                parsed.title, parsed.text, parsed.originalType,
                parsed.venueName, parsed.address, parsed.eventDate, parsed.startTime, parsed.endTime);

        EmploymentEvent event = new EmploymentEvent();
        event.setSchoolId(site.getId());
        event.setSchoolName(site.getSchoolName());
        event.setSchoolCity(site.getSchoolCity());
        event.setEventTitle(truncate(parsed.title, 500));
        event.setEventType(c.eventType.getCode());
        event.setEventTypeOriginal(truncate(parsed.originalType, 120));
        event.setVenueType(c.venueType.getCode());
        event.setStandardCategory(c.standardCategory.getCode());
        event.setEventDate(parsed.eventDate);
        event.setStartTime(parsed.startTime);
        event.setEndTime(parsed.endTime);
        event.setVenueName(truncate(parsed.venueName, 250));
        event.setEventAddress(truncate(parsed.address, 500));
        event.setOrganizer(truncate(parsed.organizer, 250));
        event.setCompanyNames(truncate(parsed.companyNames, 1000));
        event.setRegistrationDeadline(parsed.registrationDeadline);
        event.setRegistrationUrl(truncate(parsed.registrationUrl, 500));
        event.setEventDescription(truncate(parsed.text, 60000));
        event.setSourceListUrl(truncate(listUrl, 500));
        String finalDetailUrl = detailUrl != null ? detailUrl : parsed.sourceUrl;
        event.setSourceUrl(truncate(finalDetailUrl, 1000));
        // P2-4：写入外部平台 ID，供下次采集命中同一记录
        event.setRemoteId(extractRemoteId(finalDetailUrl));
        event.setSourcePublishedAt(parsed.sourcePublishedAt);
        event.setEventStatus(c.eventStatus.getCode());
        event.setVerificationStatus(c.verificationStatus.getCode());

        // 原始数据留存（文档 9：原始与标准化分离）
        event.setOriginalTitle(truncate(parsed.originalTitle != null ? parsed.originalTitle : parsed.title, 500));
        event.setOriginalTimeText(truncate(parsed.originalTimeText, 256));

        // 宣讲会：识别重点企业（文档 3.4）；双选会：不采集企业名单（文档 4.3）
        if (c.eventType == EventType.TALK) {
            CompanyResolver.Match m = companyResolver.resolve(parsed.title, parsed.text);
            if (m != null) {
                event.setCompanyId(m.getCompanyId());
                event.setCompanyName(m.getCompanyName());
                event.setCompanyCategory(m.getCompanyCategory());
                event.setRecruitmentEntity(m.getRecruitmentEntity());
            } else {
                // 未识别重点企业 → 待核验（不计入已确认）
                event.setVerificationStatus("NEED_REVIEW");
            }
        } else if (c.eventType == EventType.JOB_FAIR) {
            event.setCompanyNames(null);
            event.setRegistrationStartAt(parsed.registrationStartAt);
            event.setRegistrationMethod(truncate(parsed.registrationMethod, 256));
            event.setRegistrationRequirements(truncate(parsed.registrationRequirements, 1000));
            event.setRegistrationDeadline(parsed.registrationDeadline);
            event.setRegistrationStatus(registrationStatusResolver.resolve(
                    parsed.registrationStartAt, parsed.registrationDeadline, c.eventStatus.getCode()));
        } else {
            event.setRegistrationStatus("NOT_PUBLISHED");
        }

        return upserter.upsert(event, taskId) ? 1 : 0;
    }

    /**
     * 静态抓取 + SPA 降级渲染。
     */
    private String fetchPage(String url, String listUrl) {
        String html = fetcher.fetch(url, listUrl);
        if (html == null) {
            return null;
        }
        if (!isLikelySpa(html)) {
            return html;
        }
        if (renderer.isAvailable()) {
            String rendered = renderer.render(url);
            if (rendered != null && !rendered.isBlank()) {
                return rendered;
            }
        }
        return html;
    }

    private String renderOrFail(String url) {
        if (renderer.isAvailable()) {
            String rendered = renderer.render(url);
            if (rendered != null && !rendered.isBlank()) {
                return rendered;
            }
        }
        throw new IllegalStateException("疑似 JS 渲染站点且 Playwright 不可用（"
                + (renderer.getUnavailableReason() == null ? "渲染失败" : renderer.getUnavailableReason()) + "）");
    }

    private List<UniversitySite> loadSchools(String taskType, List<Long> schoolIds) {
        LambdaQueryWrapper<UniversitySite> qw = new LambdaQueryWrapper<>();
        if ("SPECIFIED".equalsIgnoreCase(taskType) && schoolIds != null && !schoolIds.isEmpty()) {
            qw.in(UniversitySite::getId, schoolIds);
        } else {
            qw.eq(UniversitySite::getEnabled, 1);
        }
        return siteMapper.selectList(qw);
    }

    private String resolveTaskStatus(int success, int fail) {
        if (fail == 0) {
            return CrawlTaskStatus.SUCCESS.getCode();
        }
        if (success == 0) {
            return CrawlTaskStatus.FAILED.getCode();
        }
        return CrawlTaskStatus.PARTIAL.getCode();
    }

    /**
     * 标题活动性：统一走 EventParser 的词表（P2-6：词表单源）。
     */
    private boolean isEventTitle(String title) {
        return parser.isEventTitle(title);
    }

    /**
     * 启发式判定：HTML 含模板占位符且可见文本极少 → 需 JS 渲染。
     */
    private boolean isLikelySpa(String html) {
        if (html == null || html.isEmpty()) {
            return false;
        }
        String visible = html.replaceAll("<script[\\s\\S]*?</script>", " ")
                .replaceAll("<style[\\s\\S]*?</style>", " ")
                .replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();
        boolean hasTemplate = TEMPLATE_MARK.matcher(visible).find();
        return hasTemplate && visible.length() < MIN_VISIBLE_TEXT;
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() > max ? value.substring(0, max) : value;
    }
}
