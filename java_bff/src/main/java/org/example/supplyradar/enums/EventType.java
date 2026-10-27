package org.example.supplyradar.enums;

/**
 * 活动类型。
 */
public enum EventType {
    TALK("TALK", "宣讲会"),
    JOB_FAIR("JOB_FAIR", "双选会/招聘会"),
    RECRUITMENT_EVENT("RECRUITMENT_EVENT", "其他招聘活动"),
    EMPLOYMENT_SERVICE("EMPLOYMENT_SERVICE", "就业服务活动"),
    OTHER_EMPLOYMENT_ACTIVITY("OTHER_EMPLOYMENT_ACTIVITY", "其他就业相关活动"),
    UNKNOWN("UNKNOWN", "待判断");

    private final String code;
    private final String label;

    EventType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static EventType fromCode(String code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (EventType v : values()) {
            if (v.code.equalsIgnoreCase(code)) {
                return v;
            }
        }
        return UNKNOWN;
    }
}
