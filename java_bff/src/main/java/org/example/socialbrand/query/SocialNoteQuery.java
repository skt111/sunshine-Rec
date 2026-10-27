package org.example.socialbrand.query;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 公开笔记列表查询条件。
 */
@Data
public class SocialNoteQuery {

    private List<Long> companyIds = new ArrayList<>();
    private String companyCategory;
    /** OFFICIAL_ENTERPRISE / OFFICIAL_RECRUIT / EMPLOYEE_PERSONAL / PUBLIC / UNKNOWN */
    private String accountNature;
    private String topic;
    private LocalDate startDate;
    private LocalDate endDate;
    /** like / favorite / comment / score；仅影响排序 */
    private String metricType;
    private String sourceType;
    private String verificationStatus;
    private String ownerType;
    private String keyword;
    private int page = 1;
    private int size = 20;
    /** publishedAt / like / favorite / comment / score */
    private String sortBy = "publishedAt";
    private String sortDirection = "desc";
}
