package org.example.supplyradar.controller;

import org.example.common.Result;
import org.example.supplyradar.dto.CrawlTaskVO;
import org.example.supplyradar.dto.SchoolVO;
import org.example.supplyradar.service.CrawlTaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 采集任务与采集状态接口。
 */
@RestController
@RequestMapping("/api/supply-radar")
public class CrawlTaskController {

    private final CrawlTaskService crawlTaskService;

    public CrawlTaskController(CrawlTaskService crawlTaskService) {
        this.crawlTaskService = crawlTaskService;
    }

    @PostMapping("/crawl-tasks")
    public Result<CrawlTaskVO> create(@RequestBody(required = false) CrawlTaskRequest request) {
        String taskType = request == null ? "ALL" : request.getTaskType();
        List<Long> schoolIds = request == null ? null : request.getSchoolIds();
        String triggeredBy = request == null ? "manual" : request.getTriggeredBy();
        return Result.success(crawlTaskService.create(taskType, schoolIds, triggeredBy));
    }

    @GetMapping("/crawl-tasks/{taskId}")
    public Result<CrawlTaskVO> get(@PathVariable Long taskId) {
        CrawlTaskVO vo = crawlTaskService.get(taskId);
        if (vo == null) {
            return Result.fail("任务不存在");
        }
        return Result.success(vo);
    }

    @GetMapping("/crawl-status")
    public Result<List<SchoolVO>> status(@RequestParam(required = false) String status) {
        return Result.success(crawlTaskService.status(status));
    }

    /**
     * 启动采集请求体。
     */
    public static class CrawlTaskRequest {
        private String taskType = "ALL";
        private List<Long> schoolIds;
        private String triggeredBy;

        public String getTaskType() {
            return taskType;
        }

        public void setTaskType(String taskType) {
            this.taskType = taskType;
        }

        public List<Long> getSchoolIds() {
            return schoolIds;
        }

        public void setSchoolIds(List<Long> schoolIds) {
            this.schoolIds = schoolIds;
        }

        public String getTriggeredBy() {
            return triggeredBy;
        }

        public void setTriggeredBy(String triggeredBy) {
            this.triggeredBy = triggeredBy;
        }
    }
}
