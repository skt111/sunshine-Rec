package org.example.supplyradar.service;

import org.example.crawler.config.CrawlProperties;
import org.example.supplyradar.dto.CrawlTaskVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时采集调度（P2-9）。
 *
 * <p>cron 与开关来自 {@code supply-radar.crawl.cron / enabled}。
 * 手工触发走 {@code POST /api/supply-radar/crawl}，两者共用同一任务创建与锁机制。</p>
 */
@Component
public class CrawlScheduler {

    private static final Logger log = LoggerFactory.getLogger(CrawlScheduler.class);

    private final CrawlTaskService taskService;
    private final CrawlProperties props;

    public CrawlScheduler(CrawlTaskService taskService, CrawlProperties props) {
        this.taskService = taskService;
        this.props = props;
    }

    @Scheduled(cron = "${supply-radar.crawl.cron:0 0 8,18 * * ?}")
    public void scheduledCrawl() {
        if (!props.isEnabled()) {
            log.debug("定时采集已关闭（supply-radar.crawl.enabled=false）");
            return;
        }
        try {
            CrawlTaskVO task = taskService.create("ALL", null, "SCHEDULER");
            if (task == null) {
                log.info("定时采集跳过：已有任务在运行");
                return;
            }
            log.info("定时采集已触发: taskId={} status={}", task.getTaskId(), task.getStatus());
        } catch (Exception e) {
            log.warn("定时采集触发失败: {}", e.getMessage());
        }
    }
}
