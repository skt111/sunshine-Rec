package org.example.socialbrand.controller;

import org.example.common.Result;
import org.example.socialbrand.dto.ManualNoteRequest;
import org.example.socialbrand.dto.RankingVO;
import org.example.socialbrand.dto.SocialAccountVO;
import org.example.socialbrand.dto.SocialNoteDetailVO;
import org.example.socialbrand.dto.SocialStatsVO;
import org.example.socialbrand.dto.SourceStatusVO;
import org.example.socialbrand.dto.SourceAvailabilityVO;
import org.example.socialbrand.dto.CleanNotesRequest;
import org.example.socialbrand.query.SocialNoteQuery;
import org.example.socialbrand.service.SocialBrandService;
import org.example.socialbrand.service.SocialNoteBatchService;
import org.example.socialbrand.service.SocialNoteWriteService;
import org.example.socialbrand.service.SourceProbeService;
import org.example.socialbrand.service.NoteCleanService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 雇主品牌 · 重点企业公开笔记（小红书）接口。
 *
 * <p>阶段一只提供人工录入与查询，不含任何平台抓取逻辑。</p>
 */
@RestController
@RequestMapping("/api/supply-radar/social")
public class SocialBrandController {

    private final SocialBrandService brandService;
    private final SocialNoteWriteService writeService;
    private final SocialNoteBatchService batchService;
    private final SourceProbeService probeService;
    private final NoteCleanService cleanService;

    public SocialBrandController(SocialBrandService brandService,
                                 SocialNoteWriteService writeService,
                                 SocialNoteBatchService batchService,
                                 SourceProbeService probeService,
                                 NoteCleanService cleanService) {
        this.brandService = brandService;
        this.writeService = writeService;
        this.batchService = batchService;
        this.probeService = probeService;
        this.cleanService = cleanService;
    }

    @GetMapping("/stats")
    public Result<SocialStatsVO> stats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(brandService.stats(startDate, endDate));
    }

    @GetMapping("/accounts")
    public Result<List<SocialAccountVO>> accounts(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String companyCategory,
            @RequestParam(required = false) String nature,
            @RequestParam(required = false) String verifiedStatus) {
        return Result.success(brandService.accounts(companyId, companyCategory, nature, verifiedStatus));
    }

    @GetMapping("/notes")
    public Result<Map<String, Object>> notes(
            @RequestParam(required = false) String companyIds,
            @RequestParam(required = false) String companyCategory,
            @RequestParam(required = false) String accountNature,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String metricType,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String verificationStatus,
            @RequestParam(required = false) String ownerType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "publishedAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

        SocialNoteQuery q = new SocialNoteQuery();
        q.setCompanyIds(parseIds(companyIds));
        q.setCompanyCategory(companyCategory);
        q.setAccountNature(accountNature);
        q.setTopic(topic);
        q.setStartDate(startDate);
        q.setEndDate(endDate);
        q.setMetricType(metricType);
        q.setSourceType(sourceType);
        q.setVerificationStatus(verificationStatus);
        q.setOwnerType(ownerType);
        q.setKeyword(keyword);
        q.setPage(page == null ? 1 : page);
        q.setSize(size == null ? 20 : size);
        q.setSortBy(sortBy == null ? "publishedAt" : sortBy);
        q.setSortDirection(sortDirection == null ? "desc" : sortDirection);
        return Result.success(brandService.notes(q));
    }

    @GetMapping("/notes/{id}")
    public Result<SocialNoteDetailVO> detail(@PathVariable Long id) {
        SocialNoteDetailVO vo = brandService.detail(id);
        if (vo == null) {
            return Result.fail("笔记不存在");
        }
        return Result.success(vo);
    }

    @GetMapping("/rankings")
    public Result<RankingVO> rankings(
            @RequestParam(required = false, defaultValue = "composite") String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return Result.success(brandService.rankings(type, startDate, endDate, companyId, limit));
    }

    @GetMapping("/source-status")
    public Result<List<SourceStatusVO>> sourceStatus() {
        return Result.success(probeService.listSourceStatus());
    }

    /**
     * 来源核验记录（默认最近 50 条）。
     */
    @GetMapping("/sources/checks")
    public Result<List<org.example.socialbrand.entity.SocialSourceCheck>> sourceChecks(
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false, defaultValue = "50") Integer limit) {
        return Result.success(probeService.checks(sourceType, limit));
    }

    /**
     * 单次可用性探测：先 robots.txt，被禁止则不再请求页面；不采集内容。
     */
    @PostMapping("/sources/probe")
    public Result<SourceAvailabilityVO> probeSource(@RequestBody Map<String, String> body) {
        String url = body == null ? null : body.get("url");
        return Result.success(probeService.probe(url));
    }

    @PostMapping("/notes/manual")
    public Result<Long> createManual(@RequestBody ManualNoteRequest request) {
        return Result.success(writeService.createManual(request));
    }

    /**
     * 批量导入（人工辅助采集）：单次最多 100 条，逐条校验、单条失败不影响其余。
     */
    @PostMapping("/notes/batch")
    public Result<Map<String, Object>> createBatch(@RequestBody List<ManualNoteRequest> requests) {
        return Result.success(batchService.batch(requests));
    }

    /**
     * 大模型数据清洗：标题去噪 + 归属类型 + 主题标签；失败自动降级规则清洗。
     */
    @PostMapping("/notes/clean")
    public Result<Map<String, Object>> cleanNotes(@RequestBody(required = false) CleanNotesRequest request) {
        return Result.success(cleanService.clean(request));
    }

    private List<Long> parseIds(String csv) {
        List<Long> ids = new ArrayList<>();
        if (csv == null || csv.trim().isEmpty()) {
            return ids;
        }
        for (String part : csv.split(",")) {
            String s = part.trim();
            if (s.isEmpty()) {
                continue;
            }
            try {
                ids.add(Long.parseLong(s));
            } catch (NumberFormatException ignore) {
                // 忽略非法 ID
            }
        }
        return ids;
    }
}
