package org.example.crawler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 采集配置（绑定 {@code supply-radar.crawl.*} 共 8 个键）。
 */
@Component
@ConfigurationProperties(prefix = "supply-radar.crawl")
public class CrawlProperties {

    /** 是否启用定时采集 */
    private boolean enabled = true;

    /** 定时表达式 */
    private String cron = "0 0 8,18 * * ?";

    /** 请求间隔（毫秒） */
    private long requestIntervalMs = 2000L;

    /** 单请求超时（毫秒） */
    private long timeoutMs = 30000L;

    /** 失败重试次数 */
    private int maxRetry = 2;

    /** 单任务上限高校数 */
    private int maxSchoolsPerTask = 60;

    /** 是否启用 Playwright */
    private boolean playwrightEnabled = true;

    /** 采集 UA */
    private String userAgent = "SunshineRecBot/1.0 (+contact)";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public long getRequestIntervalMs() {
        return requestIntervalMs;
    }

    public void setRequestIntervalMs(long requestIntervalMs) {
        this.requestIntervalMs = requestIntervalMs;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public int getMaxSchoolsPerTask() {
        return maxSchoolsPerTask;
    }

    public void setMaxSchoolsPerTask(int maxSchoolsPerTask) {
        this.maxSchoolsPerTask = maxSchoolsPerTask;
    }

    public boolean isPlaywrightEnabled() {
        return playwrightEnabled;
    }

    public void setPlaywrightEnabled(boolean playwrightEnabled) {
        this.playwrightEnabled = playwrightEnabled;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
