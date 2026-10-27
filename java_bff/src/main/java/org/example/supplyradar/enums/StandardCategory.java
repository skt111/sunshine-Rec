package org.example.supplyradar.enums;

/**
 * 前端标准分类（6 页签口径；PENDING_REVIEW 归入「其他活动」但保留展示标签）。
 */
public enum StandardCategory {
    ON_CAMPUS_TALK("ON_CAMPUS_TALK", "校内宣讲会"),
    OFF_CAMPUS_TALK("OFF_CAMPUS_TALK", "校外宣讲会"),
    ON_CAMPUS_FAIR("ON_CAMPUS_FAIR", "校内双选会"),
    OFF_CAMPUS_FAIR("OFF_CAMPUS_FAIR", "校外双选会"),
    OTHER("OTHER", "其他就业活动"),
    PENDING_REVIEW("PENDING_REVIEW", "待人工核验");

    private final String code;
    private final String label;

    StandardCategory(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static StandardCategory fromCode(String code) {
        if (code == null) {
            return PENDING_REVIEW;
        }
        for (StandardCategory v : values()) {
            if (v.code.equalsIgnoreCase(code)) {
                return v;
            }
        }
        return PENDING_REVIEW;
    }
}
