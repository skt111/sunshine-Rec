package org.example.supplyradar.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.supplyradar.dto.SchoolVO;
import org.example.supplyradar.entity.UniversitySite;
import org.example.supplyradar.mapper.UniversitySiteMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 高校列表服务。
 */
@Service
public class SchoolService {

    private final UniversitySiteMapper siteMapper;

    public SchoolService(UniversitySiteMapper siteMapper) {
        this.siteMapper = siteMapper;
    }

    public List<SchoolVO> list(String city, String keyword, Integer enabled) {
        QueryWrapper<UniversitySite> w = new QueryWrapper<>();
        if (city != null && !city.trim().isEmpty()) {
            w.eq("school_city", city.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            w.like("school_name", keyword.trim());
        }
        if (enabled != null) {
            w.eq("enabled", enabled);
        }
        w.orderByAsc("school_city").orderByAsc("id");

        List<UniversitySite> rows = siteMapper.selectList(w);
        List<SchoolVO> result = new ArrayList<>(rows.size());
        for (UniversitySite s : rows) {
            SchoolVO vo = new SchoolVO();
            vo.setSchoolId(s.getId());
            vo.setSchoolCode(s.getSchoolCode());
            vo.setSchoolName(s.getSchoolName());
            vo.setSchoolCity(s.getSchoolCity());
            vo.setProvince(s.getProvince());
            vo.setSiteUrl(s.getSiteUrl());
            vo.setAdapterType(s.getAdapterType());
            vo.setEnabled(s.getEnabled());
            vo.setLastCrawlAt(s.getLastCrawlAt());
            vo.setLastCrawlStatus(s.getCrawlStatus());
            vo.setLastFailReason(s.getLastFailReason());
            result.add(vo);
        }
        return result;
    }
}
