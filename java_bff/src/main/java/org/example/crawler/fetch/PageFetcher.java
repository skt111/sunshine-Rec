package org.example.crawler.fetch;

import org.example.crawler.config.CrawlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 页面抓取器：HttpClient + Jsoup 预处理。
 *
 * <p>职责：robots.txt 检查、请求间隔、超时、失败重试（指数退避）、charset 判定。</p>
 */
@Component
public class PageFetcher {

    private static final Logger log = LoggerFactory.getLogger(PageFetcher.class);

    private static final Pattern META_CHARSET = Pattern.compile(
            "<meta[^>]+charset\\s*=\\s*[\"']?([A-Za-z0-9_\\-]+)", Pattern.CASE_INSENSITIVE);

    private final CrawlProperties props;
    private final HttpClient httpClient;
    private final Map<String, List<String>> robotsCache = new ConcurrentHashMap<>();
    private final Object throttleLock = new Object();
    private long lastRequestAt = 0L;

    public PageFetcher(CrawlProperties props) {
        this.props = props;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(props.getTimeoutMs()))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * 抓取页面 HTML 文本。
     *
     * @param url       目标 URL
     * @param listUrl   来源列表页（用于 robots 上下文，可空）
     * @return HTML 文本；访问受限或失败时返回 null
     */
    public String fetch(String url, String listUrl) {
        return fetch(url, listUrl, props.getUserAgent());
    }

    /**
     * 抓取（指定 UA；部分站点 WAF 仅拦截桌面 UA，如北邮）。
     */
    public String fetch(String url, String listUrl, String userAgent) {
        if (url == null || url.isBlank()) {
            return null;
        }
        if (!isFetchableUrl(url)) {
            return null;
        }
        if (!allowedByRobots(url)) {
            log.warn("robots.txt 禁止访问，跳过: {}", url);
            return null;
        }

        String ua = (userAgent == null || userAgent.isBlank()) ? props.getUserAgent() : userAgent;
        int attempt = 0;
        int maxRetry = Math.max(0, props.getMaxRetry());
        while (attempt <= maxRetry) {
            try {
                throttle();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofMillis(props.getTimeoutMs()))
                        .header("User-Agent", ua)
                        .header("Accept", "text/html,application/xhtml+xml")
                        .GET()
                        .build();

                HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
                int code = response.statusCode();

                if (code == 403 || code == 401) {
                    log.warn("访问受限（{}），停止自动尝试: {}", code, url);
                    return null;
                }
                if (code >= 200 && code < 300) {
                    return decode(response);
                }

                log.warn("抓取失败 status={}, url={}, attempt={}/{}", code, url, attempt, maxRetry);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception e) {
                log.warn("抓取异常 url={}, attempt={}/{}: {}", url, attempt, maxRetry, e.getMessage());
            }
            attempt++;
            backoff(attempt);
        }
        return null;
    }

    private boolean isFetchableUrl(String url) {
        String lower = url.toLowerCase();
        return (lower.startsWith("http://") || lower.startsWith("https://"))
                && !lower.contains("javascript:") && !lower.contains("mailto:");
    }

    private void throttle() {
        synchronized (throttleLock) {
            long interval = Math.max(0L, props.getRequestIntervalMs());
            long wait = lastRequestAt + interval - System.currentTimeMillis();
            if (wait > 0) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            lastRequestAt = System.currentTimeMillis();
        }
    }

    private void backoff(int attempt) {
        try {
            long sleep = Math.min(8000L, 1000L * (1L << Math.min(attempt, 3)));
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String decode(HttpResponse<byte[]> response) {
        byte[] body = response.body();
        String contentType = response.headers().firstValue("content-type").orElse("");
        Charset charset = null;

        Matcher m = Pattern.compile("charset=([A-Za-z0-9_\\-]+)", Pattern.CASE_INSENSITIVE).matcher(contentType);
        if (m.find()) {
            charset = safeCharset(m.group(1));
        }
        if (charset == null && body != null) {
            String head = new String(body, 0, Math.min(body.length, 4096), StandardCharsets.ISO_8859_1);
            Matcher mm = META_CHARSET.matcher(head);
            if (mm.find()) {
                charset = safeCharset(mm.group(1));
            }
        }
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }
        return new String(body == null ? new byte[0] : body, charset);
    }

    private Charset safeCharset(String name) {
        try {
            return Charset.forName(name);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 简易 robots.txt 检查：仅处理 Disallow 前缀匹配（Allow 优先）。
     */
    private boolean allowedByRobots(String url) {
        try {
            URI uri = URI.create(url);
            String origin = uri.getScheme() + "://" + uri.getHost()
                    + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
            List<String> disallows = robotsCache.computeIfAbsent(origin, this::loadRobots);
            if (disallows.isEmpty()) {
                return true;
            }
            String path = uri.getPath() == null ? "/" : uri.getPath();
            for (String rule : disallows) {
                if (!rule.isEmpty() && path.startsWith(rule)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private List<String> loadRobots(String origin) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(origin + "/robots.txt"))
                    .timeout(Duration.ofMillis(Math.min(10000L, props.getTimeoutMs())))
                    .header("User-Agent", props.getUserAgent())
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() != 200 || resp.body() == null) {
                return List.of();
            }
            List<String> disallows = new java.util.ArrayList<>();
            for (String line : resp.body().split("\\r?\\n")) {
                String s = line.trim();
                if (s.toLowerCase().startsWith("disallow:")) {
                    String p = s.substring("disallow:".length()).trim();
                    // P1-13: 保留 "/"（Disallow: / = 全站禁止），不得当成“无限制”
                    if (!p.isEmpty()) {
                        disallows.add(p);
                    }
                }
            }
            return disallows;
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 简单 GET，返回是否 200（用于连通性探测）。
     */
    public boolean probe(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(props.getTimeoutMs()))
                    .header("User-Agent", props.getUserAgent())
                    .GET()
                    .build();
            HttpResponse<Void> resp = httpClient.send(req, HttpResponse.BodyHandlers.discarding());
            return resp.statusCode() >= 200 && resp.statusCode() < 400;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
}
