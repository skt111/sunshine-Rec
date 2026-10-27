package org.example.crawler.classify;

import org.example.supplyradar.enums.EventType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 活动类型识别。
 *
 * <p><b>标题优先</b>：标题含明确活动词时直接定类型，避免被正文导航词（如"招聘会"菜单）污染；
 * 标题为岗位类（老师/工程师/校招-xxx）时归 RECRUITMENT_EVENT，不误判为双选会。</p>
 */
@Component
public class EventTypeResolver {

    private static final List<String> TALK_WORDS = Arrays.asList(
            "宣讲会", "宣讲", "专场招聘", "校园宣讲", "空中宣讲", "线上宣讲");

    private static final List<String> FAIR_WORDS = Arrays.asList(
            "双选会", "招聘会", "供需见面会", "洽谈会", "人才交流会", "大型招聘");

    /** 标题中明确的双选会信号（优先级最高） */
    private static final List<String> FAIR_TITLE_WORDS = Arrays.asList(
            "双选会", "招聘会", "供需见面会", "洽谈会", "人才交流会");

    private static final List<String> SERVICE_WORDS = Arrays.asList(
            "就业指导", "职业规划", "简历", "面试技巧", "就业讲座", "政策解读", "就业服务");

    private static final List<String> RECRUIT_WORDS = Arrays.asList(
            "招聘公告", "招聘信息", "校园招聘", "补录", "春季招聘", "秋季招聘", "秋招", "春招");

    /** 岗位类信号（招聘职位，不是活动） */
    private static final List<String> POST_WORDS = Arrays.asList(
            "岗位", "老师", "工程师", "专员", "经理", "顾问", "设计师", "算法",
            "实习生", "管培生", "培训生", "校招-", "招聘-", "职位");

    /**
     * 识别活动类型（标题优先）。
     */
    public EventType resolve(String title, String text, String originalType) {
        String t = title == null ? "" : title;

        // 1) 标题明确双选会
        if (containsAny(t, FAIR_TITLE_WORDS)) {
            return EventType.JOB_FAIR;
        }
        // 1.5) 标题明确就业服务（职业指导/简历/面试技巧）——P2-2：需先于宣讲判定，
        // 否则正文含“宣讲”时会被误归 TALK
        if (containsAny(t, SERVICE_WORDS)) {
            return EventType.EMPLOYMENT_SERVICE;
        }
        // 2) 标题明确宣讲会
        if (containsAny(t, TALK_WORDS)) {
            return EventType.TALK;
        }
        // 3) 标题为岗位类（非活动实体）
        if (containsAny(t, POST_WORDS)) {
            return EventType.RECRUITMENT_EVENT;
        }

        // 4) 标题无明确信号 → 结合原始类型与正文
        String all = (originalType == null ? "" : originalType) + " " + t + " "
                + (text == null ? "" : text);
        if (containsAny(all, FAIR_WORDS)) {
            return EventType.JOB_FAIR;
        }
        if (containsAny(all, TALK_WORDS)) {
            return EventType.TALK;
        }
        if (containsAny(all, SERVICE_WORDS)) {
            return EventType.EMPLOYMENT_SERVICE;
        }
        if (containsAny(all, RECRUIT_WORDS)) {
            return EventType.RECRUITMENT_EVENT;
        }
        return EventType.UNKNOWN;
    }

    private boolean containsAny(String text, List<String> words) {
        for (String w : words) {
            if (text.contains(w)) {
                return true;
            }
        }
        return false;
    }
}
