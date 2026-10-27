package org.example.crawler.discover;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 栏目与活动入口发现。
 *
 * <p>不能仅抓"宣讲会/双选会"标题：需按栏目关键词白名单扫描候选栏目，
 * 再进入列表/详情判断活动性质。</p>
 */
@Component
public class ColumnDiscoverer {

    /**
     * 栏目关键词白名单（文档 6.2）：招聘会类优先，信息/公告类次之。
     */
    private static final List<String> PRIORITY_KEYWORDS = Arrays.asList(
            "双选会", "宣讲会", "组团招聘", "招聘会", "就业活动", "招聘活动",
            "招聘日历", "就业日历", "就业市场", "校园宣讲");

    /** 次级线索（优先栏目为空时补充） */
    private static final List<String> SECONDARY_KEYWORDS = Arrays.asList(
            "校园招聘", "招聘信息", "企业进校", "就业信息", "通知公告");

    /** 活动候选关键词 */
    private static final List<String> EVENT_KEYWORDS = Arrays.asList(
            "宣讲", "双选", "招聘", "就业", "洽谈", "供需", "见面会", "校招");

    /**
     * 从导航/首页发现候选栏目链接（优先级排序，供调用方截断）。
     */
    public List<String> discoverColumns(String html, String baseUrl) {
        Set<String> priority = new LinkedHashSet<>();
        Set<String> secondary = new LinkedHashSet<>();
        if (html == null || html.isBlank()) {
            return new ArrayList<>(priority);
        }
        Document doc = Jsoup.parse(html);
        for (Element a : doc.select("a[href]")) {
            String text = a.text();
            if (text == null || text.isBlank()) {
                continue;
            }
            String abs = absolute(a, baseUrl);
            if (abs == null) {
                continue;
            }
            if (containsAny(text, PRIORITY_KEYWORDS)) {
                priority.add(abs);
            } else if (containsAny(text, SECONDARY_KEYWORDS)) {
                secondary.add(abs);
            }
        }
        List<String> result = new ArrayList<>(priority);
        result.addAll(secondary);
        return result;
    }

    /**
     * 从列表页提取候选活动详情链接。
     */
    public List<String> discoverEventLinks(String listHtml, String baseUrl) {
        Set<String> result = new LinkedHashSet<>();
        if (listHtml == null || listHtml.isBlank()) {
            return new ArrayList<>(result);
        }
        Document doc = Jsoup.parse(listHtml);
        for (Element a : doc.select("a[href]")) {
            String text = a.text();
            if (text == null || text.length() < 4) {
                continue;
            }
            if (!containsAny(text, EVENT_KEYWORDS)) {
                continue;
            }
            String abs = absolute(a, baseUrl);
            if (abs != null && !abs.equals(baseUrl)) {
                result.add(abs);
            }
        }
        return new ArrayList<>(result);
    }

    private String absolute(Element a, String baseUrl) {
        String abs = a.absUrl("href");
        if (abs == null || abs.isBlank()) {
            String href = a.attr("href");
            if (href == null || href.isBlank() || href.startsWith("#") || href.startsWith("javascript")) {
                return null;
            }
            try {
                abs = URI.create(baseUrl).resolve(href).toString();
            } catch (Exception e) {
                return null;
            }
        }
        // 排除登录 / 注册 / 企业端入口与伪协议（需求 B6：不采集访问受限页面）
        String lower = abs.toLowerCase();
        if (!(lower.startsWith("http://") || lower.startsWith("https://"))) {
            return null;
        }
        if (lower.contains("login") || lower.contains("register") || lower.contains("signin")
                || lower.contains("/vip/") || lower.contains("/system/company")) {
            return null;
        }
        return abs;
    }

    private boolean containsAny(String text, List<String> words) {
        for (String w : words) {
            if (text.contains(w)) {
                return true;
            }
        }
        return false;
    }
}
