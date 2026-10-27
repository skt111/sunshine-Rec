package org.example.supplyradar.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.example.supplyradar.entity.CrawlTask;
import org.example.supplyradar.mapper.CrawlTaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 僵尸任务回收（P1-12）。
 *
 * <p>进程崩溃/卡死会使任务永久停留在 RUNNING，导致后续触发一直返回旧 taskId。</p>
 */
@Component
public class StaleTaskRecoveryRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StaleTaskRecoveryRunner.class);

    /** 超过该时长仍未结束的 RUNNING 任务视为僵尸 */
    private static final long STALE_MINUTES = 60L;

    private final CrawlTaskMapper taskMapper;

    public StaleTaskRecoveryRunner(CrawlTaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            UpdateWrapper<CrawlTask> w = new UpdateWrapper<>();
            w.eq("status", "RUNNING")
                    .lt("started_at", LocalDateTime.now().minusMinutes(STALE_MINUTES))
                    .set("status", "FAILED")
                    .set("finished_at", LocalDateTime.now())
                    .set("error_summary", "任务超时回收（进程崩溃或卡死）");
            int n = taskMapper.update(null, w);
            if (n > 0) {
                log.warn("回收僵尸采集任务 {} 条（RUNNING 超过 {} 分钟）", n, STALE_MINUTES);
            }
        } catch (Exception e) {
            log.warn("僵尸任务回收失败（不影响启动）: {}", e.getMessage());
        }
    }
}
