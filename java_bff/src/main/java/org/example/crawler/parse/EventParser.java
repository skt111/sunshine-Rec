package org.example.crawler.parse;

import org.example.crawler.clean.HtmlCleaner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 详情页解析器（通用）：从标题/正文中提取活动字段。
 */
@Component
public class EventParser {

    private static final Pattern DATE_CN = Pattern.compile("(20\\d{2})\\s*[年\\-/.]\\s*(\\d{1,2})\\s*[月\\-/.]\\s*(\\d{1,2})");
    private static final Pattern TIME_RANGE = Pattern.compile(
            "(\\d{1,2}:\\d{2})\\s*[-—~至]{1,2}\\s*(\\d{1,2}:\\d{2})");
    private static final Pattern TIME_SINGLE = Pattern.compile("(\\d{1,2}:\\d{2})");
    private static final Pattern PUBLISHED = Pattern.compile(
            "(20\\d{2})[-/年](\\d{1,2})[-/月](\\d{1,2})");
    private static final Pattern DEADLINE = Pattern.compile(
            "报名截止[^0-9]{0,10}(20\\d{2})[-/年](\\d{1,2})[-/月](\\d{1,2})");

    /** 前端模板占位符（未渲染） */
    private static final Pattern TEMPLATE_MARK = Pattern.compile("\\{\\{|\\}\\}|\\$\\{");

    /** 站点标题特征（非活动标题） */
    private static final List<String> SITE_TITLE_HINTS = Arrays.asList(
            "就业创业服务网", "就业信息网", "就业指导中心", "招生就业", "就业服务网", "学生就业指导");

    /** 站点标题形态（如“招聘公告 - XX大学”） */
    private static final Pattern SITE_TITLE_SHAPE =
            Pattern.compile("^.{0,16}(招聘公告|通知公告|新闻中心)\\s*[-_|].{0,20}(大学|学院|学校)");

    /** 活动时间（优先于发布时间） */
    private static final Pattern ACTIVITY_DATE = Pattern.compile(
            "(?:活动时间|举办时间|宣讲时间|双选会时间|招聘会时间|会议时间|举办日期)"
                    + "[^0-9]{0,12}(20\\d{2})\\s*[年\\-/.]\\s*(\\d{1,2})\\s*[月\\-/.]\\s*(\\d{1,2})");

    /** 报名开始（文档 4.2：需与截止时间严格区分） */
    private static final Pattern REG_START = Pattern.compile(
            "(?:报名开始|报名时间|报名日期|报名起始)[^0-9]{0,10}(20\\d{2})\\s*[年\\-/.]\\s*(\\d{1,2})\\s*[月\\-/.]\\s*(\\d{1,2})");

    /** 报名截止 */
    private static final Pattern REG_DEADLINE = Pattern.compile(
            "(?:报名截止|截止时间|报名截止时间|请于)[^0-9]{0,10}(20\\d{2})\\s*[年\\-/.]\\s*(\\d{1,2})\\s*[月\\-/.]\\s*(\\d{1,2})");

    /** 无年份日期（卡片常用 10-14 / 10/14） */
    private static final Pattern MD_DATE = Pattern.compile("(?<!\\d)(\\d{1,2})[-/](\\d{1,2})(?!\\d)");

    /** 中文卡片日期（10月28日） */
    private static final Pattern CN_MD_DATE = Pattern.compile("(\\d{1,2})\\s*月\\s*(\\d{1,2})\\s*日");

    /** 从文本中抽取含活动词的标题片段 */
    private static final Pattern TITLE_FRAGMENT = Pattern.compile(
            "[^\\s\\|>]{4,80}?(?:宣讲会|宣讲|双选会|招聘会|供需见面会|洽谈会|组团招聘)[^\\s\\|>]{0,30}");

    /** 活动标题必备词（非活动类页面不入库） */
    private static final List<String> EVENT_TITLE_WORDS = Arrays.asList(
            "宣讲", "双选", "招聘会", "供需", "洽谈", "见面会", "校招", "专场", "招聘活动", "就业活动", "校园行");

    /**
     * 列表项容器候选选择器
     */
    private static final String LIST_ITEM_SELECTOR =
            "li, tr, .item, .list-item, .news-item, .job-item, .recruitment-item, .fair-item, .info-item";

    private static final Logger log = LoggerFactory.getLogger(EventParser.class);

    private final HtmlCleaner cleaner;
    public EventParser(HtmlCleaner cleaner) {
        this.cleaner = cleaner;
    }

    /**
     * 解析结果。
     */
    public static class ParsedEvent {
        public String title;
        public String text;
        public LocalDate eventDate;
        public String startTime;
        public String endTime;
        public String venueName;
        public String address;
        public String organizer;
        public String companyNames;
        public LocalDateTime registrationDeadline;
        public String registrationUrl;
        public LocalDateTime sourcePublishedAt;
        public String originalType;
        /** 原始标题（未清洗） */
        public String originalTitle;
        /** 原始时间文本 */
        public String originalTimeText;
        /** 原始栏目名 */
        public String originalCategory;
        /** 企业报名开始时间（双选会） */
        public LocalDateTime registrationStartAt;
        /** 报名方式 */
        public String registrationMethod;
        /** 报名条件 */
        public String registrationRequirements;
        /** 详情页 URL（列表解析时可能为空） */
        public String sourceUrl;
    }

    /**
     * 解析详情页。
     */
    public ParsedEvent parse(String html, String listUrl) {
        if (html == null || html.isBlank()) {
            return null;
        }
        Document doc = Jsoup.parse(html);
        doc.select("script,style,noscript").remove();

        ParsedEvent e = new ParsedEvent();
        e.title = firstText(doc, "h1", ".article-title", ".news-title", ".detail-title",
                ".content-title", ".title", ".name", "h2", "h3");
        String contentHtml = pickContent(doc);
        e.text = cleaner.toText(contentHtml, 8000);
        if (e.title == null) {
            e.title = doc.title();
        }
        if (!isValidTitle(e.title)) {
            // 兜底：优先取含活动词的正文行，否则取首个有效行
            String candidate = null;
            if (e.text != null) {
                for (String line : e.text.split("\\n")) {
                    String l = line.trim();
                    if (l.length() < 6 || l.length() > 90 || !isValidTitle(l)) {
                        continue;
                    }
                    if (isEventTitle(l)) {
                        candidate = l;
                        break;
                    }
                    if (candidate == null) {
                        candidate = l;
                    }
                }
            }
            e.title = candidate;
            if (!isValidTitle(e.title)) {
                String head = e.text == null ? "" : e.text.substring(0, Math.min(160, e.text.length()));
                log.info("标题无效: docTitle={}, textHead={}", doc.title(), head.replace("\n", " "));
                return null;
            }
        }
        e.title = cleanTitle(e.title.trim());
        e.originalTitle = e.title;

        String all = (e.title + "\n" + (e.text == null ? "" : e.text));

        // 日期：优先取“活动时间”而非发布时间
        Matcher adm = ACTIVITY_DATE.matcher(all);
        if (adm.find()) {
            e.eventDate = safeDate(adm.group(1), adm.group(2), adm.group(3));
            e.originalTimeText = adm.group();
        } else {
            Matcher dm = DATE_CN.matcher(all);
            if (dm.find()) {
                e.eventDate = safeDate(dm.group(1), dm.group(2), dm.group(3));
                e.originalTimeText = dm.group();
            }
        }

        // 报名起止（双选会）
        Matcher rs = REG_START.matcher(all);
        if (rs.find()) {
            e.registrationStartAt = safeDateTime(rs.group(1), rs.group(2), rs.group(3));
        }
        Matcher rd = REG_DEADLINE.matcher(all);
        if (rd.find()) {
            e.registrationDeadline = safeDateTime(rd.group(1), rd.group(2), rd.group(3));
        }
        e.registrationMethod = extractByLabel(all, new String[]{
                "报名方式", "报名途径", "报名途径与方式"});
        e.registrationRequirements = extractByLabel(all, new String[]{
                "报名条件", "报名要求", "参会要求", "资质要求", "参会条件"});

        Matcher tr = TIME_RANGE.matcher(all);
        if (tr.find()) {
            e.startTime = pad(tr.group(1));
            e.endTime = pad(tr.group(2));
        } else {
            Matcher ts = TIME_SINGLE.matcher(all);
            if (ts.find()) {
                e.startTime = pad(ts.group(1));
            }
        }

        e.venueName = extractByLabel(all, new String[]{
                "举办地点", "活动地点", "宣讲地点", "双选会地点", "招聘会地点", "会议地点",
                "举办场馆", "场地", "地点"});
        e.address = extractByLabel(all, new String[]{
                "详细地址", "具体地址", "活动地址", "举办地址", "地址"});
        e.organizer = extractByLabel(all, new String[]{
                "主办单位", "主办方", "举办单位", "承办单位", "承办方", "组织单位", "主办"});
        e.companyNames = extractByLabel(all, new String[]{
                "参会企业", "参会单位", "招聘单位", "企业名称", "相关企业", "参展单位", "企业名单", "用人单位"});
        e.registrationUrl = extractFirstLink(doc);
        e.originalType = extractByLabel(all, new String[]{"活动类型", "类型"});

        // 兼容旧正则：仅在新正则未命中的情况下兑底
        if (e.registrationDeadline == null) {
            Matcher dead = DEADLINE.matcher(all);
            if (dead.find()) {
                LocalDate d = safeDate(dead.group(1), dead.group(2), dead.group(3));
                if (d != null) {
                    e.registrationDeadline = d.atTime(23, 59);
                }
            }
        }

        Matcher pub = PUBLISHED.matcher(all);
        if (pub.find()) {
            LocalDate d = safeDate(pub.group(1), pub.group(2), pub.group(3));
            if (d != null) {
                e.sourcePublishedAt = d.atStartOfDay();
            }
        }
        return e;
    }

    private String pickContent(Document doc) {
        String[] selectors = {".article-content", ".news-content", ".detail-content", ".v_news_content",
                ".article", "article", ".content", "#content", ".main-content"};
        for (String s : selectors) {
            for (Element el : doc.select(s)) {
                String text = el.text();
                if (text == null || text.length() < 200) {
                    continue;
                }
                // 跳过弹窗/对话框（含“取消+提交”且体量小）
                if (text.contains("取消") && text.contains("提交") && text.length() < 800) {
                    continue;
                }
                // 跳过导航/城市筛选块
                if (text.contains("热搜城市") && text.length() < 1500) {
                    continue;
                }
                return el.html();
            }
        }
        return doc.body() == null ? doc.html() : doc.body().html();
    }

    private String firstText(Document doc, String... selectors) {
        for (String s : selectors) {
            Element el = doc.selectFirst(s);
            if (el != null && !el.text().isBlank()) {
                return el.text().trim();
            }
        }
        return null;
    }

    /**
     * 标题是否为活动性质（供外部复用）。
     */
    public boolean isEventTitle(String title) {
        if (title == null) {
            return false;
        }
        for (String w : EVENT_TITLE_WORDS) {
            if (title.contains(w)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 列表页直接解析（适用于列表项无详情链接或链接为 javascript 的站点）。
     */
    public List<ParsedEvent> parseList(String html) {
        List<ParsedEvent> result = new ArrayList<>();
        if (html == null || html.isBlank()) {
            return result;
        }
        Document doc = Jsoup.parse(html);
        doc.select("script,style,noscript").remove();
        Set<String> seen = new HashSet<>();

        for (Element el : doc.select(LIST_ITEM_SELECTOR)) {
            String text = el.text();
            if (text == null || text.length() < 8 || text.length() > 400) {
                continue;
            }
            Matcher dm = DATE_CN.matcher(text);
            LocalDate itemDate = null;
            String dateKey = null;
            if (dm.find()) {
                itemDate = safeDate(dm.group(1), dm.group(2), dm.group(3));
                dateKey = dm.group();
            } else {
                Matcher md = MD_DATE.matcher(text);
                while (md.find()) {
                    int mo = Integer.parseInt(md.group(1));
                    int da = Integer.parseInt(md.group(2));
                    if (mo >= 1 && mo <= 12 && da >= 1 && da <= 31) {
                        try {
                            itemDate = withYearRollover(mo, da);
                            dateKey = md.group();
                            break;
                        } catch (Exception ignore) {
                            // 非法日期继续找
                        }
                    }
                }
                // 中文日期（10月28日）
                if (itemDate == null) {
                    Matcher cn = CN_MD_DATE.matcher(text);
                    while (cn.find()) {
                        int mo = Integer.parseInt(cn.group(1));
                        int da = Integer.parseInt(cn.group(2));
                        if (mo >= 1 && mo <= 12 && da >= 1 && da <= 31) {
                            try {
                                itemDate = withYearRollover(mo, da);
                                dateKey = cn.group();
                                break;
                            } catch (Exception ignore) {
                                // 继续
                            }
                        }
                    }
                }
            }
            if (itemDate == null) {
                continue;
            }

            Element a = el.selectFirst("a");
            String title = null;
            if (a != null && isValidTitle(a.text())) {
                title = a.text();
            }
            if (title == null) {
                Element h = el.selectFirst(".title, .name, h1, h2, h3, h4, strong");
                if (h != null && isValidTitle(h.text())) {
                    title = h.text();
                }
            }
            if (title == null) {
                String prefix = text.substring(0, Math.max(0, text.indexOf(dateKey))).trim();
                if (prefix.length() >= 6 && prefix.length() <= 150) {
                    title = prefix;
                }
            }
            // 正则兜底：从文本中抽含活动词的片段
            if (title == null || !isEventTitle(cleanTitle(title.trim()))) {
                Matcher fm = TITLE_FRAGMENT.matcher(text);
                if (fm.find()) {
                    title = fm.group();
                }
            }
            if (title == null) {
                continue;
            }
            title = cleanTitle(title.trim());
            if (!isValidTitle(title) || !isEventTitle(title)) {
                continue;
            }
            if (!seen.add(title + "|" + dateKey)) {
                continue;
            }

            ParsedEvent e = new ParsedEvent();
            e.title = title;
            e.eventDate = itemDate;
            e.text = text;
            e.venueName = extractByLabel(text, new String[]{
                    "举办地点", "活动地点", "宣讲地点", "地点", "场馆", "场地"});
            e.address = extractByLabel(text, new String[]{"详细地址", "地址"});
            Matcher tr = TIME_RANGE.matcher(text);
            if (tr.find()) {
                e.startTime = pad(tr.group(1));
                e.endTime = pad(tr.group(2));
            }
            if (a != null) {
                String abs = a.absUrl("href");
                if (abs != null && (abs.startsWith("http://") || abs.startsWith("https://"))) {
                    e.sourceUrl = abs;
                }
            }
            result.add(e);
        }
        return result;
    }

    /**
     * 标题清洗：去掉站点后缀（如“ - XX大学就业网”）。
     */
    private String cleanTitle(String title) {
        if (title == null) {
            return null;
        }
        String t = title.trim();
        int idx = t.indexOf(" - ");
        if (idx > 5) {
            t = t.substring(0, idx).trim();
        } else {
            idx = t.indexOf(" _ ");
            if (idx > 5) {
                t = t.substring(0, idx).trim();
            } else {
                idx = t.indexOf(" | ");
                if (idx > 5) {
                    t = t.substring(0, idx).trim();
                }
            }
        }
        return t;
    }

    /**
     * 标题有效性：过滤模板占位符、站点标题、过短标题。
     */
    private boolean isValidTitle(String title) {
        if (title == null) {
            return false;
        }
        String t = title.trim();
        if (t.length() < 4) {
            return false;
        }
        if (TEMPLATE_MARK.matcher(t).find()) {
            return false;
        }
        for (String hint : SITE_TITLE_HINTS) {
            if (t.contains(hint)) {
                return false;
            }
        }
        return !SITE_TITLE_SHAPE.matcher(t).find();
    }

    private String extractByLabel(String text, String[] labels) {
        for (String label : labels) {
            int idx = text.indexOf(label);
            if (idx >= 0) {
                String rest = text.substring(idx + label.length()).replaceFirst("^[\\s:：]+", "");
                String value = rest.split("[\\n。；;]")[0].trim();
                if (!value.isEmpty() && value.length() <= 200) {
                    return value;
                }
            }
        }
        return null;
    }

    private String extractFirstLink(Document doc) {
        for (Element a : doc.select("a[href]")) {
            String href = a.attr("href");
            String txt = a.text();
            if (href != null && (href.startsWith("http") || href.startsWith("/"))
                    && (txt.contains("报名") || href.contains("sign") || href.contains("apply"))) {
                return a.absUrl("href").isEmpty() ? href : a.absUrl("href");
            }
        }
        return null;
    }

    private LocalDateTime safeDateTime(String y, String m, String d) {
        LocalDate date = safeDate(y, m, d);
        return date == null ? null : date.atTime(23, 59);
    }

    private LocalDate safeDate(String y, String m, String d) {
        try {
            return LocalDate.of(Integer.parseInt(y), Integer.parseInt(m), Integer.parseInt(d));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 无年份日期补全（P2-5）：列表只给“10月28日”时，
     * 若该日期已过去超过 6 个月，视为次年（跨年场次），避免整批标注到已结束的过去。
     */
    private LocalDate withYearRollover(int month, int day) {
        LocalDate today = LocalDate.now();
        LocalDate d = LocalDate.of(today.getYear(), month, day);
        if (d.isBefore(today.minusMonths(6))) {
            return LocalDate.of(today.getYear() + 1, month, day);
        }
        return d;
    }

    private String pad(String time) {
        String[] parts = time.split(":");
        if (parts.length == 2) {
            return String.format("%02d:%02d", Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }
        return time;
    }
}
