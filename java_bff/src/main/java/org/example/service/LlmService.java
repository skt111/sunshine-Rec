package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.LlmConfig;
import org.example.dto.ChatCompletionRequest;
import org.example.dto.ChatCompletionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;

/**
 * 大模型调用服务。
 *
 * <p>基于 OpenAI 兼容的 Chat Completions 协议，通过 JDK {@link HttpClient}
 * 调用内部 LLM PaaS 平台（原 WebClient 实现已移除，避免引入 webflux）。</p>
 */
@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    private final LlmConfig llmConfig;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public LlmService(LlmConfig llmConfig, ObjectMapper objectMapper) {
        this.llmConfig = llmConfig;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(5, llmConfig.getTimeout())))
                .build();
    }

    /**
     * 同步调用大模型（非流式）。
     *
     * @param userMessage 用户消息
     * @return 模型回复内容
     */
    public String chat(String userMessage) {
        return chat(userMessage, null);
    }

    /**
     * 同步调用大模型（非流式），支持系统提示词。
     *
     * @param userMessage  用户消息
     * @param systemPrompt 系统提示词（可为 null）
     * @return 模型回复内容
     */
    public String chat(String userMessage, String systemPrompt) {
        ChatCompletionRequest request = buildRequest(userMessage, systemPrompt, false);
        try {
            String body = objectMapper.writeValueAsString(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(llmConfig.getChatUrl()))
                    .timeout(Duration.ofSeconds(llmConfig.getTimeout()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + llmConfig.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("大模型调用失败, status={}, body={}", response.statusCode(), response.body());
                throw new RuntimeException("大模型调用失败: HTTP " + response.statusCode());
            }

            ChatCompletionResponse parsed = objectMapper.readValue(response.body(), ChatCompletionResponse.class);
            if (parsed == null) {
                log.warn("大模型返回为空");
                return null;
            }
            log.info("大模型响应: id={}, model={}, tokens={}", parsed.getId(), parsed.getModel(),
                    parsed.getUsage() != null ? parsed.getUsage().getTotalTokens() : "N/A");
            return parsed.getReplyContent();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("大模型调用被中断", e);
        } catch (Exception e) {
            log.error("大模型调用异常", e);
            throw new RuntimeException("大模型调用异常: " + e.getMessage(), e);
        }
    }

    /**
     * 构建请求体。
     */
    private ChatCompletionRequest buildRequest(String userMessage, String systemPrompt, boolean stream) {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(llmConfig.getModel());
        request.setStream(stream);

        ArrayList<ChatCompletionRequest.Message> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new ChatCompletionRequest.Message("system", systemPrompt));
        }
        messages.add(new ChatCompletionRequest.Message("user", userMessage));
        request.setMessages(messages);
        return request;
    }
}
