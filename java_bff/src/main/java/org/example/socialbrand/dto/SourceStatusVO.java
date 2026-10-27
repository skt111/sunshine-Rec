package org.example.socialbrand.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据来源状态：由 {@code social_data_source} 登记表驱动。
 */
@Data
public class SourceStatusVO {

    /** MANUAL / AUTHORIZED_API / AUTHORIZED_SERVICE / PUBLIC_PAGE */
    private String sourceType;
    private String label;
    /** ENABLED / NOT_CONFIGURED / DISABLED_BY_POLICY */
    private String status;
    private String description;
    /** 是否已取得平台/服务方授权 */
    private Boolean authorized;
    /** ALLOW / DISALLOW / NOT_APPLICABLE */
    private String robotsDecision;
    private LocalDateTime lastCheckedAt;
}
