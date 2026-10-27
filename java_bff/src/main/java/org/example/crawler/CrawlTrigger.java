package org.example.crawler;

import java.util.List;

/**
 * 采集触发器接口（P0 契约）。
 *
 * <p>P2 的调度/任务服务依赖此接口创建并驱动采集；P1 的 {@code CrawlExecutor} 提供实现。
 * P1 未就绪时由 {@link NoopCrawlTrigger} 兜底，保证 API 层可独立开发。</p>
 */
public interface CrawlTrigger {

    /**
     * 触发一次采集。
     *
     * @param taskId     已创建的采集任务 ID（由 P2 创建，P1 用于终态更新与变更溯源）
     * @param taskType   ALL / SPECIFIED
     * @param schoolIds  指定高校 ID（taskType=SPECIFIED 时生效）
     * @return 触发结果
     */
    TaskTriggerResult trigger(Long taskId, String taskType, List<Long> schoolIds);

    /**
     * 触发结果。
     */
    class TaskTriggerResult {
        private boolean accepted;
        private String message;

        public TaskTriggerResult() {
        }

        public TaskTriggerResult(boolean accepted, String message) {
            this.accepted = accepted;
            this.message = message;
        }

        public static TaskTriggerResult ok() {
            return new TaskTriggerResult(true, "accepted");
        }

        public static TaskTriggerResult rejected(String message) {
            return new TaskTriggerResult(false, message);
        }

        public boolean isAccepted() {
            return accepted;
        }

        public void setAccepted(boolean accepted) {
            this.accepted = accepted;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
