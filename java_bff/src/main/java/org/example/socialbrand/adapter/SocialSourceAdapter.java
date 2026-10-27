package org.example.socialbrand.adapter;

import org.example.socialbrand.dto.SourceAvailabilityVO;

/**
 * 数据来源适配器契约。
 *
 * <p><b>实现纪律</b>：</p>
 * <ul>
 *   <li>{@link #checkAvailability(String)} 只做 robots.txt 判定与单次可达性探测，不采集内容；</li>
 *   <li>未取得授权或 robots 禁止时，各 fetch* 方法必须直接熔断抛错，不得绕过登录/验证码/风控；</li>
 *   <li>请求频率、重试策略由调用方控制，禁止并发轰炸与 UA/IP 轮换。</li>
 * </ul>
 */
public interface SocialSourceAdapter {

    /** 来源类型：MANUAL / AUTHORIZED_API / AUTHORIZED_SERVICE / PUBLIC_PAGE */
    String sourceType();

    /** 该适配器是否支持此 URL。 */
    boolean supports(String targetUrl);

    /**
     * 可用性探测：先查 robots.txt，被禁止则不再请求页面。
     */
    SourceAvailabilityVO checkAvailability(String targetUrl);

    /**
     * 获取候选笔记：阶段一未实现，未授权时直接熔断。
     */
    default Object fetchCandidates(String keywordOrAccount) {
        throw new IllegalStateException("未取得授权：阶段一不支持自动获取候选笔记");
    }

    /**
     * 获取笔记详情：阶段一未实现，未授权时直接熔断。
     */
    default Object fetchNoteDetail(String noteUrl) {
        throw new IllegalStateException("未取得授权：阶段一不支持自动读取笔记详情");
    }

    /**
     * 记录互动指标：阶段一未实现，未授权时直接熔断。
     */
    default void recordMetrics(Object payload) {
        throw new IllegalStateException("未取得授权：阶段一不支持自动记录互动指标");
    }
}
