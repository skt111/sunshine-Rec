package org.example.socialbrand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社媒数据来源登记 {@code social_data_source}。
 *
 * <p>授权状态、robots 判定与当前结论必须显式登记，避免"默认可以采"。</p>
 */
@Data
@TableName("social_data_source")
public class SocialDataSource {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** MANUAL / AUTHORIZED_API / AUTHORIZED_SERVICE / PUBLIC_PAGE */
    private String sourceType;
    private String label;
    private Integer authorized;
    private String allowedFields;
    /** ALLOW / DISALLOW / NOT_APPLICABLE */
    private String robotsDecision;
    /** ENABLED / NOT_CONFIGURED / DISABLED_BY_POLICY */
    private String status;
    private String conclusion;
    private LocalDateTime lastCheckedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
