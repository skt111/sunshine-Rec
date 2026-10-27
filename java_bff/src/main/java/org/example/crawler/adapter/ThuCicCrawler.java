package org.example.crawler.adapter;

import org.example.crawler.classify.CompanyResolver;
import org.example.crawler.classify.RegistrationStatusResolver;
import org.example.crawler.fetch.PageFetcher;
import org.example.crawler.persist.EventUpserter;
import org.example.crawler.render.PlaywrightRenderer;
import org.example.supplyradar.entity.EmploymentEvent;
import org.example.supplyradar.entity.UniversitySite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 清华大学学生职业发展指导中心适配器（匿名公开列表页，静态 HTML）。
 *
 * <p>列表地址：{@code https://career.cic.tsinghua.edu.cn/xsglxt/f/jyxt/anony/xxfb}
 * 行结构：{@code <span>26-10-27</span> ... <a ahref="/xsglxt/.../showZwxx?zpxxid=xxx" fbfw="内">活动名————单位</a>}</p>
 *
 * <p>日期为两位数年份（26-10-27）；详情链接在 {@code ahref} 属性而非 {@code href}。</p>
 */
@Component
public class ThuCicCrawler {

    private static final Logger log = LoggerFactory.getLogger(ThuCicCrawler.class);

    private static final String ADAPTER_KEY = "THU_CIC";
    private static final String ORIGIN = "https://career.cic.tsinghua.edu.cn";
    private static final String LIST_URL = ORIGIN + "/xsglxt/f/jyxt/anony/xxfb";
    /** 最大翻页数（每页约 20 条） */
    private static final int MAX_PAGES = 15;

    /** 日期 26-10-27 + 后续 800 字符内的 ahref 链接 */
    private static final Pattern ROW = Pattern.compile(
            "(\\d{2})-(\\d{2})-(\\d{2})[\\s\\S]{0,900}?<a\\s+ahref=\"([^\"]+)\"[^>]*>([^<]{4,200})</a>");

    private final PageFetcher fetcher;
    private final EventUpserter upserter;
    private final CompanyResolver companyResolver;
    private final RegistrationStatusResolver registrationStatusResolver;
    private final PlaywrightRenderer renderer;

    public ThuCicCrawler(PageFetcher fetcher,
                         EventUpserter upserter,
                         CompanyResolver companyResolver,
                         RegistrationStatusResolver registrationStatusResolver,
                         PlaywrightRenderer renderer) {
        this.fetcher = fetcher;
        this.upserter = upserter;
        this.companyResolver = companyResolver;
        this.registrationStatusResolver = registrationStatusResolver;
        this.renderer = renderer;
    }

    public boolean supports(UniversitySite site) {
        return site != null && ADAPTER_KEY.equalsIgnoreCase(site.getAdapterKey());
    }

    public int crawl(UniversitySite site, Long taskId) {
        // 优先 Playwright 点击翻页（#n = 下一页）；不可用时回退静态单页
        List<String> htmlPages = new ArrayList<>();
        if (renderer.isAvailable()) {
            htmlPages = renderer.renderPages(LIST_URL, "#n", MAX_PAGES);
        }
        if (htmlPages.isEmpty()) {
            String html = fetcher.fetch(LIST_URL, LIST_URL);
            if (html != null && !html.isBlank()) {
                htmlPages.add(html);
            }
        }
        if (htmlPages.isEmpty()) {
            log.warn("清华列表页不可达");
            return 0;
        }

        int saved = 0;
        for (String html : htmlPages) {
            saved += parsePage(site, html, taskId);
        }
        log.info("清华采集完成：{} 条（{} 页）", saved, htmlPages.size());
        return saved;
    }

    private int parsePage(UniversitySite site, String html, Long taskId) {
        int saved = 0;
        Matcher m = ROW.matcher(html);
        while (m.find()) {
            try {
                int yy = Integer.parseInt(m.group(1));
                int month = Integer.parseInt(m.group(2));
                int day = Integer.parseInt(m.group(3));
                LocalDate date = LocalDate.of(2000 + yy, month, day);
                String relUrl = m.group(4).trim();
                String rawTitle = m.group(5).trim();
                if (save(site, date, relUrl, rawTitle, taskId)) {
                    saved++;
                }
            } catch (Exception e) {
                log.debug("清华单条解析失败: {}", e.getMessage());
            }
        }
        return saved;
    }

    private boolean save(UniversitySite site, LocalDate date, String relUrl, String rawTitle, Long taskId) {
        // 标题格式：活动名————单位
        String title = rawTitle;
        String unit = null;
        int sep = rawTitle.indexOf("————");
        if (sep > 0) {
            title = rawTitle.substring(0, sep).trim();
            unit = rawTitle.substring(sep + 4).trim();
        }
        if (title.length() < 4) {
            return false;
        }

        boolean isFair = title.contains("双选") || title.contains("招聘会") || title.contains("洽谈会");
        EmploymentEvent e = new EmploymentEvent();
        e.setSchoolId(site.getId());
        e.setSchoolName(site.getSchoolName());
        e.setSchoolCity(site.getSchoolCity());
        e.setEventTitle(truncate(title, 500));
        e.setOriginalTitle(truncate(rawTitle, 500));
        e.setEventType(isFair ? "JOB_FAIR" : "TALK");
        e.setEventTypeOriginal(isFair ? "招聘会" : "宣讲会");
        e.setOriginalCategory("招聘信息");
        e.setVenueType("ON_CAMPUS");
        e.setStandardCategory(isFair ? "ON_CAMPUS_FAIR" : "ON_CAMPUS_TALK");
        e.setEventDate(date);
        e.setRegistrationStatus("NOT_PUBLISHED");
        e.setEventStatus("FINISHED".equals(registrationStatusResolver.resolveEventStatus(date))
                ? "FINISHED" : registrationStatusResolver.resolveEventStatus(date));
        e.setVerificationStatus("VERIFIED");
        e.setSourceListUrl(LIST_URL);
        e.setSourceUrl(truncate(relUrl.startsWith("http") ? relUrl : ORIGIN + relUrl, 1000));
        e.setRemoteId(extractId(relUrl));

        if (unit != null && !unit.isBlank()) {
            e.setOrganizer(truncate(unit, 256));
        }
        // 企业识别（宣讲会）
        if (!isFair) {
            CompanyResolver.Match match = companyResolver.resolve(title + " " + (unit == null ? "" : unit), null);
            if (match != null) {
                e.setCompanyId(match.getCompanyId());
                e.setCompanyName(match.getCompanyName());
                e.setCompanyCategory(match.getCompanyCategory());
                e.setRecruitmentEntity(match.getRecruitmentEntity());
            } else {
                e.setVerificationStatus("NEED_REVIEW");
            }
        }
        return upserter.upsert(e, taskId);
    }

    private String extractId(String relUrl) {
        int i = relUrl.indexOf("zpxxid=");
        if (i < 0) {
            return null;
        }
        String rest = relUrl.substring(i + 7);
        int amp = rest.indexOf('&');
        return amp > 0 ? rest.substring(0, amp) : rest;
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
