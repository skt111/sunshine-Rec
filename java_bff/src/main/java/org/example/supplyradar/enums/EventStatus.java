package org.example.supplyradar.enums;

/**
 * 活动状态。
 */
public enum EventStatus {
    UPCOMING("UPCOMING", "未开始"),
    ONGOING("ONGOING", "进行中"),
    FINISHED("FINISHED", "已结束"),
    CANCELLED("CANCELLED", "已取消"),
    POSTPONED("POSTPONED", "已延期"),
    UNKNOWN("UNKNOWN", "未知");

    private final String code;
    private final String label;

    EventStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static EventStatus fromCode(String code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (EventStatus v : values()) {
            if (v.code.equalsIgnoreCase(code)) {
                return v;
            }
        }
        return UNKNOWN;
    }
}
