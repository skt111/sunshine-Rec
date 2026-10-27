package org.example.crawler.classify;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import org.example.supplyradar.entity.TargetCompany;
import org.example.supplyradar.mapper.TargetCompanyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 重点企业识别（宣讲会专用）。
 *
 * <p>企业名单来自 {@code target_company} 配置表（文档 3.3：不得硬编码）。
 * 匹配顺序：标题全名 → 标题别名 → 正文全名 → 正文别名。</p>
 */
@Component
public class CompanyResolver {

    private static final Logger log = LoggerFactory.getLogger(CompanyResolver.class);
    private static final long CACHE_TTL_MS = 5 * 60 * 1000L;

    /**
     * 假阳性排除：命中公司名时若包含这些词则为其他实体（如“京东方”非京东）。
     */
    private static final Map<String, List<String>> FALSE_POSITIVES = new HashMap<>();

    static {
        FALSE_POSITIVES.put("京东", Arrays.asList("京东方"));
        FALSE_POSITIVES.put("广发证券", Arrays.asList("广发银行"));
        FALSE_POSITIVES.put("中金公司", Arrays.asList("中金岭南"));
        FALSE_POSITIVES.put("百度", Arrays.asList("百度百科"));
    }

    private final TargetCompanyMapper mapper;
    private volatile List<TargetCompany> cache = Collections.emptyList();
    private volatile long cacheAt = 0L;

    public CompanyResolver(TargetCompanyMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 从标题/正文识别重点企业。
     *
     * @return 匹配结果；未命中返回 null
     */
    public Match resolve(String title, String text) {
        List<TargetCompany> companies = load();
        if (companies.isEmpty()) {
            return null;
        }
        // 1) 标题全名
        for (TargetCompany c : companies) {
            if (title != null && contains(title, c.getCompanyName())) {
                return toMatch(c);
            }
        }
        // 2) 标题别名
        for (TargetCompany c : companies) {
            if (title != null && aliasHit(title, c)) {
                return toMatch(c);
            }
        }
        // 3) 正文全名
        for (TargetCompany c : companies) {
            if (text != null && contains(text, c.getCompanyName())) {
                return toMatch(c);
            }
        }
        // 4) 正文别名（仅取较长别名，降低误判）
        for (TargetCompany c : companies) {
            if (text != null && aliasHit(text, c)) {
                return toMatch(c);
            }
        }
        return null;
    }

    public List<TargetCompany> load() {
        long now = System.currentTimeMillis();
        if (now - cacheAt < CACHE_TTL_MS && !cache.isEmpty()) {
            return cache;
        }
        try {
            List<TargetCompany> list = mapper.selectList(
                    new LambdaQueryWrapper<TargetCompany>().eq(TargetCompany::getEnabled, 1));
            if (list != null) {
                cache = list;
                cacheAt = now;
                log.info("重点企业配置已加载: {} 家", list.size());
            }
        } catch (Exception e) {
            log.warn("重点企业配置加载失败: {}", e.getMessage());
        }
        return cache;
    }

    private boolean aliasHit(String text, TargetCompany c) {
        if (c.getCompanyAliases() == null || c.getCompanyAliases().isBlank()) {
            return false;
        }
        // P1-7: 别名命中同样要过假阳性排除（如“广发”命中“广发银行”）
        if (excludedBy(text, c.getCompanyName())) {
            return false;
        }
        for (String raw : c.getCompanyAliases().split("[,，]")) {
            String alias = raw.trim();
            if (alias.length() >= 3 && text.contains(alias)) {
                return true;
            }
        }
        return false;
    }

    private boolean excludedBy(String text, String companyName) {
        if (companyName == null) {
            return false;
        }
        List<String> excludes = FALSE_POSITIVES.get(companyName);
        if (excludes == null) {
            return false;
        }
        for (String ex : excludes) {
            if (text.contains(ex)) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(String text, String word) {
        if (word == null || word.isBlank() || !text.contains(word)) {
            return false;
        }
        List<String> excludes = FALSE_POSITIVES.get(word);
        if (excludes != null) {
            for (String ex : excludes) {
                if (text.contains(ex)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Match toMatch(TargetCompany c) {
        Match m = new Match();
        m.companyId = c.getId();
        m.companyName = c.getCompanyName();
        m.companyCategory = c.getCompanyCategory();
        m.recruitmentEntity = c.getRecruitmentEntity();
        return m;
    }

    /**
     * 匹配结果。
     */
    @Data
    public static class Match {
        private Long companyId;
        private String companyName;
        private String companyCategory;
        private String recruitmentEntity;
    }

    /** 供统计/调试：当前启用的企业名 */
    public List<String> enabledNames() {
        List<String> names = new ArrayList<>();
        for (TargetCompany c : load()) {
            names.add(c.getCompanyName());
        }
        return names;
    }
}
