package org.example.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 采集触发器兜底实现：当 P1 的 {@link CrawlTrigger} 实现未就绪时使用。
 *
 * <p>不做任何采集，仅返回"采集模块未就绪"，保证 API 层可独立开发与启动。</p>
 */
public class NoopCrawlTrigger implements CrawlTrigger {

    private static final Logger log = LoggerFactory.getLogger(NoopCrawlTrigger.class);

    @Override
    public TaskTriggerResult trigger(Long taskId, String taskType, List<Long> schoolIds) {
        log.warn("采集模块未就绪（NoopCrawlTrigger），忽略任务触发: taskId={}, taskType={}", taskId, taskType);
        return TaskTriggerResult.rejected("采集模块未就绪");
    }
}
