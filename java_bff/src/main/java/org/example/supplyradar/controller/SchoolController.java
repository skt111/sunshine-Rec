package org.example.supplyradar.controller;

import org.example.common.Result;
import org.example.supplyradar.dto.SchoolVO;
import org.example.supplyradar.service.SchoolService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 高校列表接口。
 */
@RestController
@RequestMapping("/api/supply-radar")
public class SchoolController {

    private final SchoolService schoolService;

    public SchoolController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @GetMapping("/schools")
    public Result<List<SchoolVO>> schools(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer enabled) {
        return Result.success(schoolService.list(city, keyword, enabled));
    }
}
