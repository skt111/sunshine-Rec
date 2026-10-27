package org.example.socialbrand.adapter;

import org.example.socialbrand.dto.SourceAvailabilityVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 小红书公开页面适配器（阶段一仅做可用性探测）。
 *
 * <p>执行顺序固定为：<b>先读 robots.txt → 被禁止则立即停止</b>，不做任何页面请求。
 * robots 允许时才发起一次 GET，用于判断是否需要登录/是否被风控；
 * 不做翻页、不解析正文、不保存任何内容，也不实现 fetch* 的采集逻辑。</p>
 */
@Component
public class XiaohongshuPublicPageAdapter implements SocialSourceAdapter {

    private static final Logger log = LoggerFactory.getLogger(XiaohongshuPublicPageAdapter.class);

    private static final String USER_AGENT =
            "SunshineRec-SourceProbe/1.0 (compliance-probe; single-request; no-retry)";

    private static final String[] LOGIN_MARKERS = {
            "登录后查看", "扫码登录", "请登录", "验证码", "captcha", "滑块", "访问过于频繁", "安全验证"
    };

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @Override
    public String sourceType() {
        return "PUBLIC_PAGE";
    }

    @Override
    public boolean supports(String targetUrl) {
        try {
            String host = URI.create(targetUrl).getHost();
            if (host == null) {
                return false;
            }
            String h = host.toLowerCase(Locale.ROOT);
            return h.equals("xiaohongshu.com") || h.endsWith(".xiaohongshu.com")
                    || h.equals("xhslink.com") || h.endsWith(".xhslink.com");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public SourceAvailabilityVO checkAvailability(String targetUrl) {
        SourceAvailabilityVO vo = new SourceAvailabilityVO();
        vo.setSourceType(sourceType());
        vo.setTargetUrl(targetUrl);

        URI uri;
        try {
            uri = URI.create(targetUrl);
        } catch (Exception e) {
            vo.setConclusion("UNKNOWN");
            vo.setRemark("链接格式不合法");
            return vo;
        }

        String robotsUrl = uri.getScheme() + "://" + uri.getHost() + "/robots.txt";
        vo.setRobotsUrl(robotsUrl);

        RobotsRules rules = loadRobotsRules(robotsUrl);
        if (rules == null) {
            vo.setRobotsDecision("NOT_APPLICABLE");
        } else if (rules.disallows(uri.getPath() == null ? "/" : uri.getPath())) {
            vo.setRobotsDecision("DISALLOW");
            vo.setRobotsRule(rules.matchedRuleText);
            vo.setConclusion("DISALLOWED_BY_ROBOTS");
            vo.setRemark("robots.txt 禁止通用 UA 访问，已停止后续请求（未发起页面请求）");
            return vo;
        } else {
            vo.setRobotsDecision("ALLOW");
            vo.setRobotsRule(rules.matchedRuleText);
        }

        // robots 允许才做一次可达性探测（单次，无重试）
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(targetUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .GET()
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            int status = resp.statusCode();
            vo.setHttpStatus(status);

            String location = resp.headers().firstValue("location").orElse(null);
            if (location != null) {
                vo.setFinalUrl(location);
            }
            boolean redirectToLogin = location != null
                    && (location.contains("login") || location.contains("website-login"));

            String body = resp.body() == null ? "" : resp.body();
            String hit = findMarker(body);
            vo.setBlockMarkers(hit);

            if (status >= 300 && status < 400) {
                vo.setConclusion(redirectToLogin ? "LOGIN_REQUIRED" : "BLOCKED");
                vo.setLoginRequired(redirectToLogin);
                vo.setRemark(redirectToLogin
                        ? "未登录访问被重定向到登录页，自动读取不可用"
                        : "返回 " + status + " 跳转，来源不可用");
            } else if (status == 403 || status == 429) {
                vo.setConclusion("BLOCKED");
                vo.setRemark("来源返回 " + status + "，按合规要求停止该路径");
            } else if (status >= 500) {
                vo.setConclusion("UNKNOWN");
                vo.setRemark("来源返回 " + status + "，稍后再核验");
            } else if (hit != null) {
                vo.setConclusion("LOGIN_REQUIRED");
                vo.setLoginRequired(true);
                vo.setRemark("页面命中登录/验证特征：" + hit);
            } else if (status == 200) {
                vo.setConclusion("ALLOWED");
                vo.setRemark("robots 允许且页面可直接访问；正式采集前仍需确认平台条款与字段公开性");
            } else {
                vo.setConclusion("UNKNOWN");
                vo.setRemark("返回 " + status + "，结论待人工复核");
            }
            return vo;
        } catch (Exception e) {
            log.warn("来源探测失败 {}: {}", targetUrl, e.getMessage());
            vo.setConclusion("UNKNOWN");
            vo.setRemark("探测异常：" + e.getClass().getSimpleName());
            return vo;
        }
    }

    private String findMarker(String body) {
        if (body == null || body.isEmpty()) {
            return null;
        }
        String sample = body.length() > 200_000 ? body.substring(0, 200_000) : body;
        for (String marker : LOGIN_MARKERS) {
            if (sample.contains(marker)) {
                return marker;
            }
        }
        return null;
    }

    private RobotsRules loadRobotsRules(String robotsUrl) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(robotsUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200 || resp.body() == null) {
                return null;
            }
            return RobotsRules.parse(resp.body());
        } catch (Exception e) {
            log.warn("robots.txt 读取失败 {}: {}", robotsUrl, e.getMessage());
            return null;
        }
    }

    /**
     * 极简 robots 解析：只关心 {@code User-agent: *} 分组的最长匹配规则。
     */
    static final class RobotsRules {

        private final List<String[]> starRules = new ArrayList<>();
        String matchedRuleText;

        static RobotsRules parse(String text) {
            RobotsRules rules = new RobotsRules();
            List<String> agents = new ArrayList<>();
            Map<String, List<String[]>> byAgent = new LinkedHashMap<>();
            for (String raw : text.split("\\r?\\n")) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    agents.clear();
                    continue;
                }
                int idx = line.indexOf(':');
                if (idx <= 0) {
                    continue;
                }
                String key = line.substring(0, idx).trim().toLowerCase(Locale.ROOT);
                String val = line.substring(idx + 1).trim();
                if ("user-agent".equals(key)) {
                    agents.add(val.toLowerCase(Locale.ROOT));
                } else if ("allow".equals(key) || "disallow".equals(key)) {
                    String kind = "allow".equals(key) ? "A" : "D";
                    for (String a : agents) {
                        byAgent.computeIfAbsent(a, k -> new ArrayList<>()).add(new String[]{kind, val});
                    }
                }
            }
            List<String[]> star = byAgent.get("*");
            if (star != null) {
                rules.starRules.addAll(star);
            }
            return rules;
        }

        boolean disallows(String path) {
            String best = null;
            String bestKind = null;
            for (String[] rule : starRules) {
                String kind = rule[0];
                String pattern = rule[1];
                if (pattern.isEmpty()) {
                    continue;
                }
                if (!matches(pattern, path)) {
                    continue;
                }
                if (best == null || pattern.length() > best.length()
                        || (pattern.length() == best.length() && "A".equals(kind))) {
                    best = pattern;
                    bestKind = kind;
                }
            }
            if (best == null) {
                return false;
            }
            matchedRuleText = "User-agent: * → " + ("D".equals(bestKind) ? "Disallow: " : "Allow: ") + best;
            return "D".equals(bestKind);
        }

        private boolean matches(String pattern, String path) {
            String p = pattern;
            if (p.endsWith("*")) {
                p = p.substring(0, p.length() - 1);
            }
            if (p.endsWith("$")) {
                p = p.substring(0, p.length() - 1);
                return path.equals(p);
            }
            return path.startsWith(p);
        }
    }
}
