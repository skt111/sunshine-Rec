package org.example.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 大模型 Chat Completions 请求体（OpenAI 兼容格式）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatCompletionRequest {

    private String model;

    private List<Message> messages;

    /** 是否流式输出 */
    private Boolean stream;

    /** 采样温度 0~2 */
    private Double temperature;

    /** 最大输出 token 数 */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {
        private String role;
        private String content;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
