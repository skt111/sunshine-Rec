package org.example.supplyradar.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.common.Result;
import org.example.supplyradar.dto.CompanyVO;
import org.example.supplyradar.entity.TargetCompany;
import org.example.supplyradar.mapper.TargetCompanyMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 重点监测企业配置接口（供给侧筛选用，文档八：GET /companies）。
 */
@RestController
@RequestMapping("/api/supply-radar")
public class CompanyController {

    private final TargetCompanyMapper mapper;

    public CompanyController(TargetCompanyMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping("/companies")
    public Result<List<CompanyVO>> companies(
            @RequestParam(required = false) String category) {
        LambdaQueryWrapper<TargetCompany> qw = new LambdaQueryWrapper<>();
        qw.eq(TargetCompany::getEnabled, 1);
        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
            qw.eq(TargetCompany::getCompanyCategory, category.trim());
        }
        qw.orderByAsc(TargetCompany::getCompanyCategory).orderByAsc(TargetCompany::getId);

        List<TargetCompany> rows = mapper.selectList(qw);
        List<CompanyVO> list = new ArrayList<>(rows == null ? 0 : rows.size());
        if (rows != null) {
            for (TargetCompany c : rows) {
                CompanyVO vo = new CompanyVO();
                vo.setId(c.getId());
                vo.setCompanyName(c.getCompanyName());
                vo.setCompanyAliases(c.getCompanyAliases());
                vo.setCompanyCategory(c.getCompanyCategory());
                vo.setRecruitmentEntity(c.getRecruitmentEntity());
                list.add(vo);
            }
        }
        return Result.success(list);
    }
}
