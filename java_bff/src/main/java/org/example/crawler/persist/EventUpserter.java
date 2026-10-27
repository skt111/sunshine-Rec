package org.example.crawler.persist;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.crawler.dedup.FingerprintCalculator;
import org.example.supplyradar.entity.EmploymentEvent;
import org.example.supplyradar.entity.EmploymentEventHistory;
import org.example.supplyradar.mapper.EmploymentEventHistoryMapper;
import org.example.supplyradar.mapper.EmploymentEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 活动去重更新器：命中则更新 + 写变更历史；未命中则新增 + 写 CREATED。
 *
 * <p>需求 D3/D4/D5：重复采集更新不新增；关键字段变化留痕；失败不删除原数据。</p>
 */
@Component
public class EventUpserter {

    private static final Logger log = LoggerFactory.getLogger(EventUpserter.class);

    private final EmploymentEventMapper eventMapper;
    private final EmploymentEventHistoryMapper historyMapper;
    private final FingerprintCalculator fingerprintCalculator;

    public EventUpserter(EmploymentEventMapper eventMapper,
                         EmploymentEventHistoryMapper historyMapper,
                         FingerprintCalculator fingerprintCalculator) {
        this.eventMapper = eventMapper;
        this.historyMapper = historyMapper;
        this.fingerprintCalculator = fingerprintCalculator;
    }

    /**
     * 去重更新（单活动事务）。
     *
     * @return 是否新增或发生字段变更（false = 已存在且无变化）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean upsert(EmploymentEvent event, Long crawlTaskId) {
        if (event.getFingerprint() == null) {
            event.setFingerprint(fingerprintCalculator.calculate(
                    event.getSchoolId(), event.getSourceUrl(), event.getEventTitle(),
                    event.getEventDate(), event.getVenueName()));
        }

        EmploymentEvent existing = findByRemoteId(event) ;
        if (existing == null) {
            existing = eventMapper.selectOne(new LambdaQueryWrapper<EmploymentEvent>()
                    .eq(EmploymentEvent::getFingerprint, event.getFingerprint())
                    .last("limit 1"));
        }
        // 二级去重：同校 + 同标题 + 同日期（不同来源 URL 视为同一活动）
        if (existing == null && event.getEventDate() != null
                && event.getEventTitle() != null && !event.getEventTitle().isBlank()) {
            existing = eventMapper.selectOne(new LambdaQueryWrapper<EmploymentEvent>()
                    .eq(EmploymentEvent::getSchoolId, event.getSchoolId())
                    .eq(EmploymentEvent::getEventTitle, event.getEventTitle())
                    .eq(EmploymentEvent::getEventDate, event.getEventDate())
                    .last("limit 1"));
        }

        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            event.setCrawledAt(now);
            event.setLastSeenAt(now);
            event.setCreatedAt(now);
            event.setUpdatedAt(now);
            eventMapper.insert(event);
            writeHistory(event.getId(), crawlTaskId, "CREATED", null, null, null);
            return true;
        }

        boolean changed = false;
        changed |= diff(existing, event, "event_date", existing.getEventDate(), event.getEventDate(), crawlTaskId);
        changed |= diff(existing, event, "venue_name", existing.getVenueName(), event.getVenueName(), crawlTaskId);
        changed |= diff(existing, event, "event_title", existing.getEventTitle(), event.getEventTitle(), crawlTaskId);
        changed |= diff(existing, event, "event_status", existing.getEventStatus(), event.getEventStatus(), crawlTaskId);

        existing.setLastSeenAt(now);
        existing.setCrawledAt(now);
        existing.setUpdatedAt(now);
        // 刷新可变字段（P1-5: 可空字段仅在 incoming 非空时覆盖，防重采丢数据）
        if (event.getEventDate() != null) {
            existing.setEventDate(event.getEventDate());
        }
        if (notBlank(event.getStartTime())) {
            existing.setStartTime(event.getStartTime());
        }
        if (notBlank(event.getEndTime())) {
            existing.setEndTime(event.getEndTime());
        }
        if (notBlank(event.getVenueName())) {
            existing.setVenueName(event.getVenueName());
        }
        if (notBlank(event.getEventAddress())) {
            existing.setEventAddress(event.getEventAddress());
        }
        if (notBlank(event.getOrganizer())) {
            existing.setOrganizer(event.getOrganizer());
        }
        if (event.getCompanyNames() != null) {
            existing.setCompanyNames(event.getCompanyNames());
        }
        if (event.getRegistrationDeadline() != null) {
            existing.setRegistrationDeadline(event.getRegistrationDeadline());
        }
        if (event.getRegistrationStartAt() != null) {
            existing.setRegistrationStartAt(event.getRegistrationStartAt());
        }
        if (notBlank(event.getRegistrationMethod())) {
            existing.setRegistrationMethod(event.getRegistrationMethod());
        }
        if (notBlank(event.getRegistrationRequirements())) {
            existing.setRegistrationRequirements(event.getRegistrationRequirements());
        }
        if (notBlank(event.getRegistrationStatus())) {
            existing.setRegistrationStatus(event.getRegistrationStatus());
        }
        if (event.getCompanyId() != null) {
            existing.setCompanyId(event.getCompanyId());
        }
        if (notBlank(event.getCompanyName())) {
            existing.setCompanyName(event.getCompanyName());
        }
        if (notBlank(event.getCompanyCategory())) {
            existing.setCompanyCategory(event.getCompanyCategory());
        }
        if (notBlank(event.getRecruitmentEntity())) {
            existing.setRecruitmentEntity(event.getRecruitmentEntity());
        }
        if (notBlank(event.getOriginalTitle())) {
            existing.setOriginalTitle(event.getOriginalTitle());
        }
        if (notBlank(event.getOriginalTimeText())) {
            existing.setOriginalTimeText(event.getOriginalTimeText());
        }
        if (notBlank(event.getOriginalCategory())) {
            existing.setOriginalCategory(event.getOriginalCategory());
        }
        if (notBlank(event.getRegistrationUrl())) {
            existing.setRegistrationUrl(event.getRegistrationUrl());
        }
        if (notBlank(event.getEventDescription())) {
            existing.setEventDescription(event.getEventDescription());
        }
        if (notBlank(event.getEventStatus())) {
            existing.setEventStatus(event.getEventStatus());
        }
        if (notBlank(event.getStandardCategory())) {
            existing.setStandardCategory(event.getStandardCategory());
        }
        if (notBlank(event.getVenueType())) {
            existing.setVenueType(event.getVenueType());
        }
        if (notBlank(event.getEventType())) {
            existing.setEventType(event.getEventType());
        }
        if (notBlank(event.getVerificationStatus())) {
            existing.setVerificationStatus(event.getVerificationStatus());
        }
        if (event.getSourcePublishedAt() != null) {
            existing.setSourcePublishedAt(event.getSourcePublishedAt());
        }
        // 链接类字段回填：历史记录常缺 source_url / source_list_url，重采时补齐，
        // 否则前端卡片只能显示“入口待公布”；有变化也计入采集成果
        if (notBlank(event.getSourceUrl()) && !Objects.equals(existing.getSourceUrl(), event.getSourceUrl())) {
            existing.setSourceUrl(event.getSourceUrl());
            changed = true;
        }
        if (notBlank(event.getSourceListUrl()) && !Objects.equals(existing.getSourceListUrl(), event.getSourceListUrl())) {
            existing.setSourceListUrl(event.getSourceListUrl());
            changed = true;
        }
        if (notBlank(event.getRegistrationUrl()) && !Objects.equals(existing.getRegistrationUrl(), event.getRegistrationUrl())) {
            existing.setRegistrationUrl(event.getRegistrationUrl());
            changed = true;
        }
        eventMapper.updateById(existing);

        if (changed) {
            log.debug("活动发生变更: id={}, title={}", existing.getId(), existing.getEventTitle());
        }
        // 仅更新也要计入采集成果，否则回填历史日期会被误判为“采集 0 条”
        return changed;
    }

    private EmploymentEvent findByRemoteId(EmploymentEvent event) {
        if (event.getRemoteId() == null || event.getRemoteId().isBlank()) {
            return null;
        }
        return eventMapper.selectOne(new LambdaQueryWrapper<EmploymentEvent>()
                .eq(EmploymentEvent::getSchoolId, event.getSchoolId())
                .eq(EmploymentEvent::getRemoteId, event.getRemoteId())
                .last("limit 1"));
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private boolean diff(EmploymentEvent existing, EmploymentEvent incoming,
                         String field, Object oldValue, Object newValue, Long crawlTaskId) {
        if (Objects.equals(oldValue, newValue)) {
            return false;
        }
        writeHistory(existing.getId(), crawlTaskId, "UPDATED", field,
                oldValue == null ? null : String.valueOf(oldValue),
                newValue == null ? null : String.valueOf(newValue));
        return true;
    }

    private void writeHistory(Long eventId, Long crawlTaskId, String changeType,
                              String field, String oldValue, String newValue) {
        EmploymentEventHistory h = new EmploymentEventHistory();
        h.setEventId(eventId);
        h.setCrawlTaskId(crawlTaskId);
        h.setChangeType(changeType);
        h.setFieldName(field);
        h.setOldValue(oldValue);
        h.setNewValue(newValue);
        h.setChangedAt(LocalDateTime.now());
        historyMapper.insert(h);
    }
}
