package org.example.supplyradar.dto;

import lombok.Data;

/**
 * 重点监测企业 VO（筛选下拉用）。
 */
@Data
public class CompanyVO {

    private Long id;
    private String companyName;
    private String companyAliases;
    /** INSURANCE / FINANCE / INTERNET */
    private String companyCategory;
    private String recruitmentEntity;
}
