package org.example.crawler.dedup;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;

/**
 * 去重指纹计算器（需求 D2 口径）。
 *
 * <p>fingerprint = MD5(schoolId|eventTitle|eventDate|venueName)</p>
 *
 * <p>P2-3：不再包含 sourceUrl——同一活动在列表页/详情页/翻页后的 URL 不同，
 * 旧口径会产生重复候选（同活动多条记录）。</p>
 */
@Component
public class FingerprintCalculator {

    public String calculate(Long schoolId, String sourceUrl, String eventTitle,
                            LocalDate eventDate, String venueName) {
        String raw = nz(schoolId) + "|" + nz(eventTitle)
                + "|" + (eventDate == null ? "" : eventDate.toString()) + "|" + nz(venueName);
        return md5(raw);
    }

    private String nz(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("MD5 计算失败", e);
        }
    }
}
