package org.example.supplyradar.enums;

/**
 * 数据核验状态。
 */
public enum VerificationStatus {
    PENDING("PENDING", "待核验"),
    VERIFIED("VERIFIED", "已核验"),
    REJECTED("REJECTED", "已驳回"),
    NEED_REVIEW("NEED_REVIEW", "需人工复核");

    private final String code;
    private final String label;

    VerificationStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static VerificationStatus fromCode(String code) {
        if (code == null) {
            return PENDING;
        }
        for (VerificationStatus v : values()) {
            if (v.code.equalsIgnoreCase(code)) {
                return v;
            }
        }
        return PENDING;
    }
}
