package org.example.crawler.classify;

import org.example.supplyradar.enums.VenueType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 地点属性识别（仅依据公开文本，禁止用高校所在地推断校内）。
 */
@Component
public class VenueTypeResolver {

    private static final List<String> ONLINE_WORDS = Arrays.asList(
            "线上", "网络", "直播", "云宣讲", "腾讯会议", "钉钉", "ZOOM", "视频面试", "空中");
    private static final List<String> CAMPUS_WORDS = Arrays.asList(
            "校内", "本校", "校区", "教学楼", "大学生活动中心", "体育馆", "报告厅", "学生中心", "就业中心");
    private static final List<String> OFF_CAMPUS_WORDS = Arrays.asList(
            "校外", "会展中心", "酒店", "会议中心", "人才市场", "产业园", "科技园", "会展");

    /**
     * 识别地点属性。
     */
    public VenueType resolve(String venueName, String address, String text) {
        String t = (venueName == null ? "" : venueName) + " "
                + (address == null ? "" : address) + " "
                + (text == null ? "" : text);
        boolean online = containsAny(t, ONLINE_WORDS);
        boolean campus = containsAny(t, CAMPUS_WORDS);
        boolean off = containsAny(t, OFF_CAMPUS_WORDS);

        if (online && (campus || off)) {
            return VenueType.HYBRID;
        }
        if (online) {
            return VenueType.ONLINE;
        }
        if (campus && off) {
            return VenueType.UNKNOWN;
        }
        if (campus) {
            return VenueType.ON_CAMPUS;
        }
        if (off) {
            return VenueType.OFF_CAMPUS;
        }
        return VenueType.UNKNOWN;
    }

    private boolean containsAny(String text, List<String> words) {
        for (String w : words) {
            if (text.toUpperCase().contains(w.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}
