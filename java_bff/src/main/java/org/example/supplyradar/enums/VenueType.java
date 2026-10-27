package org.example.supplyradar.enums;

/**
 * 举办地点属性。
 */
public enum VenueType {
    ON_CAMPUS("ON_CAMPUS", "校内"),
    OFF_CAMPUS("OFF_CAMPUS", "校外"),
    ONLINE("ONLINE", "线上"),
    HYBRID("HYBRID", "线上线下混合"),
    UNKNOWN("UNKNOWN", "未知");

    private final String code;
    private final String label;

    VenueType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static VenueType fromCode(String code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (VenueType v : values()) {
            if (v.code.equalsIgnoreCase(code)) {
                return v;
            }
        }
        return UNKNOWN;
    }
}
