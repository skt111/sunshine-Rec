package org.example.crawler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 采集触发器装配：仅在无其他 {@link CrawlTrigger} 实现时注册 Noop 兜底。
 */
@Configuration
public class CrawlTriggerConfig {

    @Bean
    @ConditionalOnMissingBean(CrawlTrigger.class)
    public CrawlTrigger noopCrawlTrigger() {
        return new NoopCrawlTrigger();
    }
}
