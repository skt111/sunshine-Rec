package org.example.supplyradar.enums;

/**
 * 采集任务状态（任务级）。
 */
public enum CrawlTaskStatus {
    PENDING("PENDING", "排队中"),
    RUNNING("RUNNING", "运行中"),
    SUCCESS("SUCCESS", "全部成功"),
    PARTIAL("PARTIAL", "部分成功"),
    FAILED("FAILED", "全部失败");

    private final String code;
    private final String label;

    CrawlTaskStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static CrawlTaskStatus fromCode(String code) {
        if (code == null) {
            return PENDING;
        }
        for (CrawlTaskStatus v : values()) {
            if (v.code.equalsIgnoreCase(code)) {
                return v;
            }
        }
        return PENDING;
    }
}
