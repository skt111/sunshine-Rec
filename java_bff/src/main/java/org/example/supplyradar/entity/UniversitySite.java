package org.example.supplyradar.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 高校站点表 {@code university_site}。
 */
@TableName("university_site")
public class UniversitySite {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String schoolCode;
    private String schoolName;
    private String schoolCity;
    private String province;
    private String siteUrl;
    private String adapterType;
    private String adapterKey;
    private Integer enabled;
    private String crawlStatus;
    private LocalDateTime lastCrawlAt;
    private String lastFailReason;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getSchoolCity() {
        return schoolCity;
    }

    public void setSchoolCity(String schoolCity) {
        this.schoolCity = schoolCity;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public String getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }

    public String getAdapterKey() {
        return adapterKey;
    }

    public void setAdapterKey(String adapterKey) {
        this.adapterKey = adapterKey;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public String getCrawlStatus() {
        return crawlStatus;
    }

    public void setCrawlStatus(String crawlStatus) {
        this.crawlStatus = crawlStatus;
    }

    public LocalDateTime getLastCrawlAt() {
        return lastCrawlAt;
    }

    public void setLastCrawlAt(LocalDateTime lastCrawlAt) {
        this.lastCrawlAt = lastCrawlAt;
    }

    public String getLastFailReason() {
        return lastFailReason;
    }

    public void setLastFailReason(String lastFailReason) {
        this.lastFailReason = lastFailReason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
