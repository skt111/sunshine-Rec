package org.example.socialbrand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 重点企业社媒账号 {@code social_account}。
 *
 * <p>账号性质与核验状态必须显式记录：未核验（PENDING）不得当作官方账号统计。</p>
 */
@Data
@TableName("social_account")
public class SocialAccount {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long companyId;
    private String platform;
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
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
