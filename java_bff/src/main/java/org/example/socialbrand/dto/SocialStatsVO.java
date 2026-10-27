package org.example.socialbrand.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统计卡数据。
 *
 * <p>全部来自库内真实数据：无数据即 0，不回填任何模拟值。</p>
 */
@Data
public class SocialStatsVO {

    /** 已核验企业：enabled 且存在 VERIFIED 账号 */
    private int verifiedCompanyCount;
    /** 启用中的账号数（含待核验） */
    private int enabledAccountCount;
    /** 已核验账号数（VERIFIED） */
    private int verifiedAccountCount;
    /** 当前时间范围内公开笔记数 */
    private int noteCount;
    /** 高互动笔记数（样本不足时为 0，且 highInteractionInsufficient=true） */
    private int highInteractionCount;
    private boolean highInteractionInsufficient;
    /** 待核验笔记数 */
    private int pendingNoteCount;
    /** 待核验账号数 */
    private int pendingAccountCount;
    private String rangeStart;
    private String rangeEnd;
    private LocalDateTime lastObservedAt;
    /** 数据来源说明（固定文案，前端展示"非实时"） */
    private String sourceNote;
}
