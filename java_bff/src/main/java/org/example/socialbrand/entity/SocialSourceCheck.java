package org.example.socialbrand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 来源核验记录 {@code social_source_check}：每次探测留痕，结论不可回填猜测。
 */
@Data
@TableName("social_source_check")
public class SocialSourceCheck {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String sourceType;
    private String targetUrl;
    private String robotsUrl;
    private String robotsDecision;
    private String robotsRule;
    private Integer httpStatus;
    private String finalUrl;
    private Integer loginRequired;
    private String blockMarkers;
    /** ALLOWED / DISALLOWED_BY_ROBOTS / LOGIN_REQUIRED / BLOCKED / UNKNOWN */
    private String conclusion;
    private String remark;
    private LocalDateTime checkedAt;
}
