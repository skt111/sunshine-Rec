package org.example.supplyradar.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.supplyradar.dto.ChangeVO;
import org.example.supplyradar.dto.EventDetailVO;
import org.example.supplyradar.dto.EventVO;
import org.example.supplyradar.entity.EmploymentEvent;
import org.example.supplyradar.entity.EmploymentEventHistory;
import org.example.supplyradar.mapper.EmploymentEventHistoryMapper;
import org.example.supplyradar.mapper.EmploymentEventMapper;
import org.example.supplyradar.query.EventQuery;
import org.example.supplyradar.query.EventQueryBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 活动列表与详情服务。
 */
@Service
public class EventService {

    private final EmploymentEventMapper eventMapper;
    private final EmploymentEventHistoryMapper historyMapper;
    private final EventQueryBuilder queryBuilder;

    public EventService(EmploymentEventMapper eventMapper,
                        EmploymentEventHistoryMapper historyMapper,
                        EventQueryBuilder queryBuilder) {
        this.eventMapper = eventMapper;
        this.historyMapper = historyMapper;
        this.queryBuilder = queryBuilder;
    }

    /**
     * 分页查询（列表模式）。
     */
    public Map<String, Object> page(EventQuery q) {
        queryBuilder.validatePaging(q);
        QueryWrapper<EmploymentEvent> w = queryBuilder.buildFilterWrapper(q);
        queryBuilder.applySort(w, q);

        Page<EmploymentEvent> page = new Page<>(q.getPage(), q.getSize());
        Page<EmploymentEvent> result = eventMapper.selectPage(page, w);

        List<EventVO> list = new ArrayList<>(result.getRecords().size());
        for (EmploymentEvent e : result.getRecords()) {
            list.add(toVO(e));
        }
        return toPageResult(list, result.getTotal(), q.getPage(), q.getSize());
    }

    /**
     * 活动详情（含变更记录）。
     */
    public EventDetailVO detail(Long id) {
        EmploymentEvent e = eventMapper.selectById(id);
        if (e == null) {
            return null;
        }
        EventDetailVO vo = new EventDetailVO();
        BeanUtils.copyProperties(e, vo);

        QueryWrapper<EmploymentEventHistory> hw = new QueryWrapper<>();
        hw.eq("event_id", id).orderByDesc("changed_at").last("limit 20");
        List<EmploymentEventHistory> histories = historyMapper.selectList(hw);
        List<ChangeVO> changes = new ArrayList<>(histories.size());
        for (EmploymentEventHistory h : histories) {
            ChangeVO c = new ChangeVO();
            c.setChangeType(h.getChangeType());
            c.setFieldName(h.getFieldName());
            c.setOldValue(h.getOldValue());
            c.setNewValue(h.getNewValue());
            c.setChangedAt(h.getChangedAt());
            changes.add(c);
        }
        vo.setChanges(changes);
        return vo;
    }

    /**
     * Entity → 列表 VO。
     */
    public EventVO toVO(EmploymentEvent e) {
        EventVO vo = new EventVO();
        BeanUtils.copyProperties(e, vo);
        return vo;
    }

    /**
     * 构造分页结果。
     */
    public Map<String, Object> toPageResult(List<?> list, long total, int page, int size) {
        long totalPages = size <= 0 ? 0 : (total + size - 1) / size;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", totalPages);
        return result;
    }
}
