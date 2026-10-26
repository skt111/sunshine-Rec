package org.example.service;

import org.example.config.LlmConfig;
import org.example.dto.ChatCompletionRequest;
import org.example.dto.ChatCompletionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collections;

/**
 * 大模型调用服务
 * <p>
 * 基于 OpenAI 兼容的 Chat Completions 协议，
 * 通过 WebClient 调用内部 LLM PaaS 平台。
 * </p>
 */
@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    private final LlmConfig llmConfig;
    private final WebClient webClient;

    public LlmService(LlmConfig llmConfig) {
        this.llmConfig = llmConfig;
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Bearer " + llmConfig.getApiKey())
                .codecs(config -> config.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }

    /**
     * 同步调用大模型（非流式）
     *
     * @param userMessage 用户消息
     * @return 模型回复内容
     */
    public String chat(String userMessage) {
        return chat(userMessage, null);
    }

    /**
     * 同步调用大模型（非流式），支持系统提示词
     *
     * @param userMessage  用户消息
     * @param systemPrompt 系统提示词（可为 null）
     * @return 模型回复内容
     */
    public String chat(String userMessage, String systemPrompt) {
        ChatCompletionRequest request = buildRequest(userMessage, systemPrompt, false);

        try {
            ChatCompletionResponse response = webClient.post()
                    .uri(llmConfig.getChatUrl())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChatCompletionResponse.class)
                    .timeout(Duration.ofSeconds(llmConfig.getTimeout()))
                    .block();

            if (response == null) {
                log.warn("大模型返回为空");
                return null;
            }

            log.info("大模型响应: id={}, model={}, tokens={}",
                    response.getId(), response.getModel(),
                    response.getUsage() != null ? response.getUsage().getTotalTokens() : "N/A");

            return response.getReplyContent();

        } catch (WebClientResponseException e) {
            log.error("大模型调用失败, status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("大模型调用失败: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("大模型调用异常", e);
            throw new RuntimeException("大模型调用异常: " + e.getMessage(), e);
        }
    }

    /**
     * 流式调用大模型
     *
     * @param userMessage  用户消息
     * @param systemPrompt 系统提示词（可为 null）
     * @return 流式响应片段
     */
    public Flux<String> chatStream(String userMessage, String systemPrompt) {
        ChatCompletionRequest request = buildRequest(userMessage, systemPrompt, true);

        return webClient.post()
                .uri(llmConfig.getChatUrl())
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(ChatCompletionResponse.class)
                .timeout(Duration.ofSeconds(llmConfig.getTimeout()))
                .filter(resp -> resp.getReplyContent() != null)
                .map(ChatCompletionResponse::getReplyContent)
                .doOnError(e -> log.error("大模型流式调用异常", e));
    }

    /**
     * 构建请求体
     */
    private ChatCompletionRequest buildRequest(String userMessage, String systemPrompt, boolean stream) {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(llmConfig.getModel());
        request.setStream(stream);

        var messages = new java.util.ArrayList<ChatCompletionRequest.Message>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new ChatCompletionRequest.Message("system", systemPrompt));
        }
        messages.add(new ChatCompletionRequest.Message("user", userMessage));
        request.setMessages(messages);

        return request;
    }
}
