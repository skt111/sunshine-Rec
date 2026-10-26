package org.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大模型配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LlmConfig {

    /** 大模型 Chat Completions 完整 URL */
    private String chatUrl;

    /** API Key */
    private String apiKey;

    /** 模型名称 */
    private String model;

    /** 请求超时时间（秒） */
    private Integer timeout = 60;
}
