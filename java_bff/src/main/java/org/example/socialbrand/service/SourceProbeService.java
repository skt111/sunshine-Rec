package org.example.socialbrand.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.socialbrand.adapter.SocialSourceAdapter;
import org.example.socialbrand.dto.SourceAvailabilityVO;
import org.example.socialbrand.dto.SourceStatusVO;
import org.example.socialbrand.entity.SocialDataSource;
import org.example.socialbrand.entity.SocialSourceCheck;
import org.example.socialbrand.mapper.SocialDataSourceMapper;
import org.example.socialbrand.mapper.SocialSourceCheckMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据来源核验服务：登记表查询 + 单次可用性探测。
 *
 * <p>探测只回答"能不能读"，不采集内容；结论落库留痕，供审计与后续授权决策使用。</p>
 */
@Service
public class SourceProbeService {

    private static final Logger log = LoggerFactory.getLogger(SourceProbeService.class);

    private final List<SocialSourceAdapter> adapters;
    private final SocialDataSourceMapper sourceMapper;
    private final SocialSourceCheckMapper checkMapper;

    public SourceProbeService(List<SocialSourceAdapter> adapters,
                              SocialDataSourceMapper sourceMapper,
                              SocialSourceCheckMapper checkMapper) {
        this.adapters = adapters;
        this.sourceMapper = sourceMapper;
        this.checkMapper = checkMapper;
    }

    /**
     * 来源登记表（无登记时返回内置兜底，避免前端空白）。
     */
    public List<SourceStatusVO> listSourceStatus() {
        QueryWrapper<SocialDataSource> w = new QueryWrapper<>();
        w.orderByAsc("id");
        List<SocialDataSource> rows = sourceMapper.selectList(w);
        if (rows == null || rows.isEmpty()) {
            return fallback();
        }
        List<SourceStatusVO> list = new ArrayList<>(rows.size());
        for (SocialDataSource s : rows) {
            SourceStatusVO vo = new SourceStatusVO();
            vo.setSourceType(s.getSourceType());
            vo.setLabel(s.getLabel());
            vo.setStatus(s.getStatus());
            vo.setDescription(s.getConclusion());
            vo.setAuthorized(s.getAuthorized() != null && s.getAuthorized() == 1);
            vo.setRobotsDecision(s.getRobotsDecision());
            vo.setLastCheckedAt(s.getLastCheckedAt());
            list.add(vo);
        }
        return list;
    }

    /**
     * 核验记录（默认最近 50 条）。
     */
    public List<SocialSourceCheck> checks(String sourceType, Integer limit) {
        QueryWrapper<SocialSourceCheck> w = new QueryWrapper<>();
        if (sourceType != null && !sourceType.trim().isEmpty()) {
            w.eq("source_type", sourceType.trim());
        }
        w.orderByDesc("checked_at").orderByDesc("id");
        int max = limit == null ? 50 : Math.max(1, Math.min(200, limit));
        w.last("limit " + max);
        List<SocialSourceCheck> rows = checkMapper.selectList(w);
        return rows == null ? new ArrayList<>() : rows;
    }

    /**
     * 单次可用性探测：先 robots，被禁止则不再请求页面。
     */
    public SourceAvailabilityVO probe(String targetUrl) {
        String url = targetUrl == null ? "" : targetUrl.trim();
        if (url.isEmpty()) {
            throw new IllegalArgumentException("请填写待核验的链接");
        }
        URI uri;
        try {
            uri = URI.create(url);
        } catch (Exception e) {
            throw new IllegalArgumentException("链接格式不合法");
        }
        String scheme = uri.getScheme();
        if (scheme == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
            throw new IllegalArgumentException("链接必须是 http/https 完整地址");
        }

        SocialSourceAdapter adapter = null;
        for (SocialSourceAdapter a : adapters) {
            if (a.supports(url)) {
                adapter = a;
                break;
            }
        }
        if (adapter == null) {
            throw new IllegalArgumentException("暂不支持该域名，当前仅登记小红书（xiaohongshu.com / xhslink.com）");
        }

        SourceAvailabilityVO vo = adapter.checkAvailability(url);
        persist(vo);
        log.info("来源核验完成 url={} robots={} conclusion={}", url, vo.getRobotsDecision(), vo.getConclusion());
        return vo;
    }

    private void persist(SourceAvailabilityVO vo) {
        LocalDateTime now = LocalDateTime.now();
        SocialSourceCheck c = new SocialSourceCheck();
        c.setSourceType(vo.getSourceType());
        c.setTargetUrl(truncate(vo.getTargetUrl(), 512));
        c.setRobotsUrl(truncate(vo.getRobotsUrl(), 512));
        c.setRobotsDecision(vo.getRobotsDecision());
        c.setRobotsRule(truncate(vo.getRobotsRule(), 256));
        c.setHttpStatus(vo.getHttpStatus());
        c.setFinalUrl(truncate(vo.getFinalUrl(), 512));
        c.setLoginRequired(Boolean.TRUE.equals(vo.getLoginRequired()) ? 1 : 0);
        c.setBlockMarkers(truncate(vo.getBlockMarkers(), 256));
        c.setConclusion(vo.getConclusion());
        c.setRemark(truncate(vo.getRemark(), 512));
        c.setCheckedAt(now);
        checkMapper.insert(c);

        QueryWrapper<SocialDataSource> w = new QueryWrapper<>();
        w.eq("source_type", vo.getSourceType());
        SocialDataSource source = sourceMapper.selectOne(w);
        if (source == null) {
            return;
        }
        source.setRobotsDecision(vo.getRobotsDecision());
        source.setLastCheckedAt(now);
        source.setUpdatedAt(now);
        if ("DISALLOWED_BY_ROBOTS".equals(vo.getConclusion()) || "BLOCKED".equals(vo.getConclusion())) {
            source.setStatus("DISABLED_BY_POLICY");
        }
        String note = vo.getConclusion() + (vo.getRemark() == null ? "" : "：" + vo.getRemark());
        source.setConclusion(truncate(note, 512));
        sourceMapper.updateById(source);
    }

    private String truncate(String v, int max) {
        if (v == null) {
            return null;
        }
        return v.length() <= max ? v : v.substring(0, max);
    }

    private List<SourceStatusVO> fallback() {
        List<SourceStatusVO> list = new ArrayList<>();
        list.add(status("MANUAL", "人工辅助录入", "ENABLED", "业务人工查看后录入，不涉及自动访问"));
        list.add(status("AUTHORIZED_API", "平台授权接口", "NOT_CONFIGURED", "尚未取得授权"));
        list.add(status("AUTHORIZED_SERVICE", "授权第三方数据服务", "NOT_CONFIGURED", "尚未签约"));
        list.add(status("PUBLIC_PAGE", "公开页面自动读取", "DISABLED_BY_POLICY", "robots 禁止自动访问"));
        return list;
    }

    private SourceStatusVO status(String type, String label, String status, String desc) {
        SourceStatusVO vo = new SourceStatusVO();
        vo.setSourceType(type);
        vo.setLabel(label);
        vo.setStatus(status);
        vo.setDescription(desc);
        vo.setAuthorized(false);
        return vo;
    }
}
