package org.example.socialbrand.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 企业账号视图（含核验状态，前端据此展示"已核验/待核验"徽标）。
 */
@Data
public class SocialAccountVO {

    private Long id;
    private Long companyId;
    private String companyName;
    private String companyCategory;
    private String accountName;
    private String homeUrl;
    /** OFFICIAL_ENTERPRISE / OFFICIAL_RECRUIT / EMPLOYEE_PERSONAL / PUBLIC / UNKNOWN */
    private String accountNature;
    /** VERIFIED / PENDING / FAILED */
    private String verifiedStatus;
    private String verifyBasis;
    private LocalDateTime verifiedAt;
    private BigDecimal companyConfidence;
    private Integer enabled;
}
