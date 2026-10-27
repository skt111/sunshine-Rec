package org.example.socialbrand.dto;

import lombok.Data;

/**
 * 来源可用性探测结果（单次，不采集内容）。
 */
@Data
public class SourceAvailabilityVO {

    private String sourceType;
    private String targetUrl;
    private String robotsUrl;
    /** ALLOW / DISALLOW / NOT_APPLICABLE */
    private String robotsDecision;
    private String robotsRule;
    /** 未发起页面请求时为空（robots 已禁止的情况） */
    private Integer httpStatus;
    private String finalUrl;
    private Boolean loginRequired;
    private String blockMarkers;
    /** ALLOWED / DISALLOWED_BY_ROBOTS / LOGIN_REQUIRED / BLOCKED / UNKNOWN */
    private String conclusion;
    private String remark;
}
