package org.example.supplyradar.enums;

/**
 * 高校采集状态（高校级）。
 */
public enum SchoolCrawlStatus {
    NEVER("NEVER", "从未采集"),
    SUCCESS("SUCCESS", "成功"),
    PARTIAL("PARTIAL", "部分成功"),
    FAILED("FAILED", "失败");

    private final String code;
    private final String label;

    SchoolCrawlStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static SchoolCrawlStatus fromCode(String code) {
        if (code == null) {
            return NEVER;
        }
        for (SchoolCrawlStatus v : values()) {
            if (v.code.equalsIgnoreCase(code)) {
                return v;
            }
        }
        return NEVER;
    }
}
