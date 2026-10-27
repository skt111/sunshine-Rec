package org.example.supplyradar.query;

import java.time.LocalDate;
import java.util.List;

/**
 * 活动查询参数（列表 / 统计 / 分组共用）。
 */
public class EventQuery {

    private List<Long> schoolIds;
    private String city;
    private String category;
    private String eventType;
    private String venueType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean timeFilterEnabled = Boolean.TRUE;
    private String status;
    private String verificationStatus;
    private String keyword;
    private Integer page = 1;
    private Integer size = 20;
    private String sortBy = "eventDate";
    private String sortDirection = "asc";
    private String groupBy;
    /** 重点企业筛选 */
    private Long companyId;
    private String companyName;
    private String companyCategory;
    /** 报名状态筛选（双选会） */
    private String registrationStatus;
    /** 仅看有报名入口 */
    private Boolean hasRegistrationUrl;
    /** 排除的事件类型（如“其他”页签 = 非 TALK/JOB_FAIR） */
    private List<String> excludeEventTypes;

    public List<Long> getSchoolIds() {
        return schoolIds;
    }

    public void setSchoolIds(List<Long> schoolIds) {
        this.schoolIds = schoolIds;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getVenueType() {
        return venueType;
    }

    public void setVenueType(String venueType) {
        this.venueType = venueType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getTimeFilterEnabled() {
        return timeFilterEnabled;
    }

    public void setTimeFilterEnabled(Boolean timeFilterEnabled) {
        this.timeFilterEnabled = timeFilterEnabled;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyCategory() {
        return companyCategory;
    }

    public void setCompanyCategory(String companyCategory) {
        this.companyCategory = companyCategory;
    }

    public String getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(String registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    public Boolean getHasRegistrationUrl() {
        return hasRegistrationUrl;
    }

    public void setHasRegistrationUrl(Boolean hasRegistrationUrl) {
        this.hasRegistrationUrl = hasRegistrationUrl;
    }

    public List<String> getExcludeEventTypes() {
        return excludeEventTypes;
    }

    public void setExcludeEventTypes(List<String> excludeEventTypes) {
        this.excludeEventTypes = excludeEventTypes;
    }
}
