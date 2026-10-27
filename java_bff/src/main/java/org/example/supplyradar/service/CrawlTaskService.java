package org.example.supplyradar.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.crawler.CrawlTrigger;
import org.example.supplyradar.dto.CrawlTaskVO;
import org.example.supplyradar.dto.SchoolVO;
import org.example.supplyradar.entity.CrawlTask;
import org.example.supplyradar.enums.CrawlTaskStatus;
import org.example.supplyradar.mapper.CrawlTaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采集任务服务：创建（并发幂等）、查询、状态。
 */
@Service
public class CrawlTaskService {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskService.class);

    private final CrawlTaskMapper taskMapper;
    private final CrawlTaskLocker locker;
    private final CrawlTrigger crawlTrigger;
    private final SchoolService schoolService;

    public CrawlTaskService(CrawlTaskMapper taskMapper, CrawlTaskLocker locker,
                            CrawlTrigger crawlTrigger, SchoolService schoolService) {
        this.taskMapper = taskMapper;
        this.locker = locker;
        this.crawlTrigger = crawlTrigger;
        this.schoolService = schoolService;
    }

    /**
     * 创建并启动采集任务（并发幂等：已有 RUNNING 则返回其 ID）。
     */
    public CrawlTaskVO create(String taskType, List<Long> schoolIds, String triggeredBy) {
        Connection lock = locker.acquire();
        try {
            CrawlTask running = findRunning();
            if (running != null) {
                log.info("已存在运行中任务，返回既有 taskId={}", running.getId());
                return toVO(running);
            }

            CrawlTask task = new CrawlTask();
            task.setTaskType(taskType == null ? "ALL" : taskType);
            task.setTargetSchoolIds(joinIds(schoolIds));
            task.setStatus(CrawlTaskStatus.PENDING.getCode());
            task.setTotalCount(0);
            task.setSuccessCount(0);
            task.setFailCount(0);
            task.setTriggeredBy(triggeredBy == null ? "manual" : triggeredBy);
            task.setCreatedAt(LocalDateTime.now());
            taskMapper.insert(task);

            CrawlTrigger.TaskTriggerResult result =
                    crawlTrigger.trigger(task.getId(), task.getTaskType(), schoolIds);
            log.info("采集任务已提交: taskId={}, accepted={}, msg={}",
                    task.getId(), result.isAccepted(), result.getMessage());

            return toVO(task);
        } finally {
            locker.release(lock);
        }
    }

    /**
     * 查询任务。
     */
    public CrawlTaskVO get(Long taskId) {
        CrawlTask task = taskMapper.selectById(taskId);
        return task == null ? null : toVO(task);
    }

    /**
     * 各高校采集状态。
     */
    public List<SchoolVO> status(String lastCrawlStatus) {
        // P1-3: 仅展示启用中的高校（与头部“监测 N 所”口径一致）
        List<SchoolVO> all = schoolService.list(null, null, 1);
        if (lastCrawlStatus == null || lastCrawlStatus.trim().isEmpty()) {
            return all;
        }
        java.util.ArrayList<SchoolVO> filtered = new java.util.ArrayList<>();
        for (SchoolVO s : all) {
            if (lastCrawlStatus.equalsIgnoreCase(s.getLastCrawlStatus())) {
                filtered.add(s);
            }
        }
        return filtered;
    }

    private CrawlTask findRunning() {
        QueryWrapper<CrawlTask> w = new QueryWrapper<>();
        w.eq("status", CrawlTaskStatus.RUNNING.getCode()).orderByDesc("id").last("limit 1");
        return taskMapper.selectOne(w);
    }

    private String joinIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Long id : ids) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(id);
        }
        String s = sb.toString();
        return s.length() > 1000 ? s.substring(0, 1000) : s;
    }

    private CrawlTaskVO toVO(CrawlTask t) {
        CrawlTaskVO vo = new CrawlTaskVO();
        vo.setTaskId(t.getId());
        vo.setTaskType(t.getTaskType());
        vo.setStatus(t.getStatus());
        vo.setStartedAt(t.getStartedAt());
        vo.setFinishedAt(t.getFinishedAt());
        vo.setTotalCount(t.getTotalCount());
        vo.setSuccessCount(t.getSuccessCount());
        vo.setFailCount(t.getFailCount());
        vo.setErrorSummary(t.getErrorSummary());
        return vo;
    }
}
