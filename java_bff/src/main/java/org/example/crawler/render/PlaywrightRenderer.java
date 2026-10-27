package org.example.crawler.render;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import jakarta.annotation.PreDestroy;
import org.example.crawler.config.CrawlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Playwright 渲染器（JS 渲染页面的降级方案）。
 *
 * <p>懒加载单例 Chromium；未安装内核时返回 null 并记录原因，由上层降级为静态解析。
 * 安装命令：{@code mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"}</p>
 */
@Component
public class PlaywrightRenderer {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightRenderer.class);

    private static final String BROWSER_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /** 正文最小可见字符数（低于此判定渲染失败，防 WAF 空页） */
    private static final int MIN_VISIBLE_CHARS = 200;

    private final CrawlProperties props;
    private volatile Playwright playwright;
    private volatile Browser browser;
    private volatile BrowserContext context;
    private volatile boolean unavailable = false;
    private volatile String unavailableReason;

    public PlaywrightRenderer(CrawlProperties props) {
        this.props = props;
    }

    /**
     * 渲染页面并返回渲染后的 HTML。
     *
     * @return HTML；不可用或失败返回 null
     */
    public String render(String url) {
        if (!props.isPlaywrightEnabled()) {
            return null;
        }
        if (unavailable) {
            return null;
        }
        try {
            Page page = acquirePage();
            try {
                page.setDefaultTimeout(props.getTimeoutMs());
                page.navigate(url, new Page.NavigateOptions().setTimeout(props.getTimeoutMs()));
                try {
                    page.waitForLoadState(LoadState.NETWORKIDLE);
                } catch (Exception ignore) {
                    // 网络空闲等待超时不视为失败，取当前 DOM
                }
                // 显式等待正文有内容（防 SPA 渲染滞后）
                try {
                    page.waitForFunction("document.body && document.body.innerText.length > 500",
                            null, new Page.WaitForFunctionOptions().setTimeout(8000));
                } catch (Exception ignore) {
                    // 超时则取当前 DOM
                }
                String html = page.content();
                // P1-2: 短空页（WAF 拦截/渲染失败）不得当作有效 HTML
                int visibleLen = 0;
                try {
                    Object len = page.evaluate(
                            "document.body ? document.body.innerText.length : 0");
                    if (len instanceof Number) {
                        visibleLen = ((Number) len).intValue();
                    }
                } catch (Exception ignore) {
                    // ignore
                }
                if (visibleLen < MIN_VISIBLE_CHARS) {
                    log.warn("渲染正文过短（{} 字符 < {}），判定失败: {}",
                            visibleLen, MIN_VISIBLE_CHARS, url);
                    return null;
                }
                String preview = "";
                try {
                    preview = String.valueOf(page.evaluate(
                            "document.body ? document.body.innerText.substring(0, Math.min(200, document.body.innerText.length)) : ''"));
                } catch (Exception ignore) {
                    // ignore
                }
                log.info("Playwright 渲染完成: {} ({} chars) 预览: {}", url,
                        html == null ? 0 : html.length(), preview.replace("\n", " "));
                return html;
            } finally {
                page.close();
            }
        } catch (Exception e) {
            String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.warn("Playwright 渲染失败 url={} - {}", url, msg);
            if (msg != null && (msg.contains("Executable doesn't exist") || msg.contains("install"))) {
                unavailable = true;
                unavailableReason = "Chromium 内核未安装";
                log.error("Playwright 不可用：Chromium 内核未安装，后续全部降级为静态解析");
            }
            return null;
        }
    }

    public boolean isAvailable() {
        return props.isPlaywrightEnabled() && !unavailable;
    }

    /**
     * 渲染并点击翻页，返回多页 HTML（适用于 JS 分页站点，如清华 #n / 人大）。
     *
     * @param url          列表页
     * @param nextSelector “下一页”选择器（为空则只取一页）
     * @param maxPages     最大页数
     */
    public java.util.List<String> renderPages(String url, String nextSelector, int maxPages) {
        java.util.List<String> pages = new java.util.ArrayList<>();
        if (!props.isPlaywrightEnabled() || unavailable) {
            return pages;
        }
        Page page = null;
        try {
            page = acquirePage();
            page.setDefaultTimeout(props.getTimeoutMs());
            page.navigate(url, new Page.NavigateOptions().setTimeout(props.getTimeoutMs()));
            try {
                page.waitForLoadState(LoadState.NETWORKIDLE);
            } catch (Exception ignore) {
                // 取当前 DOM
            }
            String lastMark = null;
            for (int i = 0; i < maxPages; i++) {
                String html = page.content();
                if (html == null || html.isBlank()) {
                    break;
                }
                // 防重复页（同一页内容不重复计数）
                String mark = html.length() + "#" + Integer.toHexString(html.hashCode());
                if (mark.equals(lastMark)) {
                    break;
                }
                lastMark = mark;
                pages.add(html);

                if (nextSelector == null || nextSelector.isBlank()) {
                    break;
                }
                com.microsoft.playwright.ElementHandle next = page.querySelector(nextSelector);
                if (next == null || !next.isVisible()) {
                    break;
                }
                next.click();
                page.waitForTimeout(1800);
                try {
                    page.waitForLoadState(LoadState.NETWORKIDLE);
                } catch (Exception ignore) {
                    // ignore
                }
            }
            log.info("Playwright 分页渲染: {} 页（{}）", pages.size(), url);
        } catch (Exception e) {
            String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.warn("Playwright 分页渲染失败 url={} - {}", url, msg);
        } finally {
            if (page != null) {
                try {
                    page.close();
                } catch (Exception ignore) {
                    // ignore
                }
            }
        }
        return pages;
    }

    /** 滚动脚本：滚动页面与所有可滚动容器，触发懒加载 */
    private static final String SCROLL_JS =
            "(() => { const els = [document.scrollingElement, ...document.querySelectorAll('div,ul,section')];"
                    + " let n = 0; els.forEach(el => { if (el && el.scrollHeight > el.clientHeight + 40) {"
                    + " el.scrollTop = el.scrollHeight; n++; } }); return n; })()";

    /**
     * 渲染并滚动加载（适用于滚动懒加载列表，如人大“近期活动”）。
     */
    public String renderScrolled(String url, int maxScrolls) {
        if (!props.isPlaywrightEnabled() || unavailable) {
            return null;
        }
        Page page = null;
        try {
            page = acquirePage();
            page.setDefaultTimeout(props.getTimeoutMs());
            page.navigate(url, new Page.NavigateOptions().setTimeout(props.getTimeoutMs()));
            try {
                page.waitForLoadState(LoadState.NETWORKIDLE);
            } catch (Exception ignore) {
                // ignore
            }
            try {
                page.waitForFunction("document.body && document.body.innerText.length > 500",
                        null, new Page.WaitForFunctionOptions().setTimeout(8000));
            } catch (Exception ignore) {
                // ignore
            }
            int lastLen = -1;
            for (int i = 0; i < maxScrolls; i++) {
                try {
                    page.evaluate(SCROLL_JS);
                } catch (Exception ignore) {
                    // ignore
                }
                page.waitForTimeout(1200);
                String html = page.content();
                int len = html == null ? 0 : html.length();
                if (len == lastLen) {
                    break;
                }
                lastLen = len;
            }
            String html = page.content();
            log.info("Playwright 滚动加载完成: {} ({} chars, {} 次滚动)", url,
                    html == null ? 0 : html.length(), maxScrolls);
            return html;
        } catch (Exception e) {
            log.warn("Playwright 滚动加载失败 url={} - {}", url, e.getMessage());
            return null;
        } finally {
            if (page != null) {
                try {
                    page.close();
                } catch (Exception ignore) {
                    // ignore
                }
            }
        }
    }

    /**
     * 渲染页面并捕获匹配关键字的 JSON 响应（用于 SPA 接口探测）。
     *
     * @return 每条为 {@code url + "||" + body}
     */
    public java.util.List<String> captureJson(String url, String keyword, int waitMs) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (!props.isPlaywrightEnabled() || unavailable) {
            return out;
        }
        Page page = null;
        try {
            page = acquirePage();
            // 记录请求体（用于复现接口参数）
            page.onRequest(req -> {
                try {
                    String u = req.url();
                    if (keyword != null && !keyword.isBlank() && !u.contains(keyword)) {
                        return;
                    }
                    String pd = req.postData();
                    if (pd != null && !pd.isBlank()) {
                        log.info("RUC-REQ {} body={}", u, pd.length() > 300 ? pd.substring(0, 300) : pd);
                    }
                } catch (Exception ignore) {
                    // ignore
                }
            });
            page.onResponse(resp -> {
                try {
                    String u = resp.url();
                    if (keyword != null && !keyword.isBlank() && !u.contains(keyword)) {
                        return;
                    }
                    if (resp.status() != 200) {
                        return;
                    }
                    String ct = resp.headerValue("content-type");
                    if (ct != null && !ct.toLowerCase().contains("json") && !ct.toLowerCase().contains("text")) {
                        return;
                    }
                    String body = resp.text();
                    if (body != null && body.length() > 20) {
                        log.info("RUC-RESP {} -> {} chars", u, body.length());
                        out.add(u + "||" + body);
                    }
                } catch (Exception ignore) {
                    // ignore
                }
            });
            page.setDefaultTimeout(props.getTimeoutMs());
            page.navigate(url, new Page.NavigateOptions().setTimeout(props.getTimeoutMs()));
            page.waitForTimeout(Math.max(2000, waitMs));
            for (int i = 0; i < 3; i++) {
                try {
                    page.evaluate(SCROLL_JS);
                } catch (Exception ignore) {
                    // ignore
                }
                page.waitForTimeout(900);
            }
            log.info("Playwright 捕获 JSON: {} 条（{}）", out.size(), url);
        } catch (Exception e) {
            log.warn("Playwright 捕获失败 url={} - {}", url, e.getMessage());
        } finally {
            if (page != null) {
                try {
                    page.close();
                } catch (Exception ignore) {
                    // ignore
                }
            }
        }
        return out;
    }

    /**
     * 在浏览器上下文内直接 POST 接口并返回响应文本（同源 + Cookie，绕过 WAF 与鉴权）。
     *
     * @param pageUrl  同源页面（用于建立上下文）
     * @param apiPath  接口路径（如 /open/api/activity/guiding/list）
     * @param postBody JSON 请求体
     */
    public String postJsonInPage(String pageUrl, String apiPath, String postBody) {
        if (!props.isPlaywrightEnabled() || unavailable) {
            return null;
        }
        Page page = null;
        try {
            page = acquirePage();
            page.setDefaultTimeout(props.getTimeoutMs());
            page.navigate(pageUrl, new Page.NavigateOptions().setTimeout(props.getTimeoutMs()));
            page.waitForTimeout(2500);
            String js = "async () => { const res = await fetch('" + apiPath + "', {method:'POST',"
                    + " headers:{'Content-Type':'application/json;charset=UTF-8'},"
                    + " body: JSON.stringify(" + postBody + ")}); return await res.text(); }";
            Object result = page.evaluate(js);
            if (result == null) {
                return null;
            }
            String body = result.toString();
            log.info("Playwright 页面内 POST {} -> {} chars", apiPath, body.length());
            return body;
        } catch (Exception e) {
            log.warn("Playwright 页面内 POST 失败 {}: {}", apiPath, e.getMessage());
            return null;
        } finally {
            if (page != null) {
                try {
                    page.close();
                } catch (Exception ignore) {
                    // ignore
                }
            }
        }
    }

    public String getUnavailableReason() {
        return unavailableReason;
    }

    private synchronized Browser acquireBrowser() {
        if (browser != null && browser.isConnected()) {
            return browser;
        }
        if (playwright == null) {
            playwright = Playwright.create();
        }
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setTimeout(props.getTimeoutMs())
                .setArgs(java.util.Arrays.asList(
                        "--disable-blink-features=AutomationControlled",
                        "--no-sandbox",
                        "--disable-dev-shm-usage")));
        log.info("Playwright Chromium 已启动（headless）");
        return browser;
    }

    /**
     * 专用上下文：真实 UA / viewport / 隐藏 webdriver（应对 WAF 反自动化识别）。
     */
    private Page acquirePage() {
        Browser b = acquireBrowser();
        if (context == null) {
            context = b.newContext(new Browser.NewContextOptions()
                    .setUserAgent(BROWSER_UA)
                    .setViewportSize(1920, 1080)
                    .setLocale("zh-CN")
                    .setTimezoneId("Asia/Shanghai"));
            context.addInitScript("Object.defineProperty(navigator,'webdriver',{get:()=>undefined});");
        }
        return context.newPage();
    }

    @PreDestroy
    public void close() {
        try {
            if (context != null) {
                context.close();
            }
        } catch (Exception ignore) {
            // ignore
        }
        try {
            if (browser != null) {
                browser.close();
            }
        } catch (Exception ignore) {
            // ignore
        }
        try {
            if (playwright != null) {
                playwright.close();
            }
        } catch (Exception ignore) {
            // ignore
        }
        browser = null;
        playwright = null;
    }
}
