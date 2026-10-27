package org.example.socialbrand.service;

import org.example.socialbrand.dto.ManualNoteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 批量导入（人工辅助采集）。
 *
 * <p>逐条走 {@link SocialNoteWriteService#createManual} 的完整校验，
 * 单条失败不影响其余条目；每条独立事务，避免一条脏数据回滚整批。</p>
 */
@Service
public class SocialNoteBatchService {

    private static final Logger log = LoggerFactory.getLogger(SocialNoteBatchService.class);

    private static final int MAX_BATCH = 100;

    private final SocialNoteWriteService writeService;

    public SocialNoteBatchService(SocialNoteWriteService writeService) {
        this.writeService = writeService;
    }

    /**
     * 批量导入。
     *
     * @return {successCount, failCount, ids, errors[]}
     */
    public Map<String, Object> batch(List<ManualNoteRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("请提供待导入的笔记数据");
        }
        if (requests.size() > MAX_BATCH) {
            throw new IllegalArgumentException("单次最多导入 " + MAX_BATCH + " 条，请分批提交");
        }

        List<Long> ids = new ArrayList<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            ManualNoteRequest req = requests.get(i);
            try {
                ids.add(writeService.createManual(req));
            } catch (IllegalArgumentException e) {
                errors.add(error(i, req, e.getMessage()));
            } catch (DuplicateKeyException e) {
                errors.add(error(i, req, "该链接已录入"));
            } catch (Exception e) {
                log.error("批量导入第 {} 条失败", i + 1, e);
                errors.add(error(i, req, "写入失败，请检查字段长度或稍后重试"));
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("successCount", ids.size());
        result.put("failCount", errors.size());
        result.put("ids", ids);
        result.put("errors", errors);
        log.info("批量导入完成：成功 {} 条，失败 {} 条", ids.size(), errors.size());
        return result;
    }

    private Map<String, Object> error(int index, ManualNoteRequest req, String message) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("index", index + 1);
        item.put("noteUrl", req == null ? "" : req.getNoteUrl());
        item.put("title", req == null ? "" : req.getTitle());
        item.put("message", message);
        return item;
    }
}
