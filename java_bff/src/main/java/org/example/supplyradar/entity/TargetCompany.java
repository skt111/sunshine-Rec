package org.example.supplyradar.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 重点监测企业 {@code target_company}（名单配置化，不硬编码）。
 */
@Data
@TableName("target_company")
public class TargetCompany {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String companyName;
    /** 别名，逗号分隔 */
    private String companyAliases;
    /** INSURANCE / FINANCE / INTERNET */
    private String companyCategory;
    private String recruitmentEntity;
    private Integer enabled;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
