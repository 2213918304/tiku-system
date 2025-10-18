package com.springboot.tiku.service.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.springboot.tiku.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 硅基流动AI服务
 */
@Slf4j
@Service
public class SiliconFlowAIService {
    
    private final SystemConfigService systemConfigService;
    private final WebClient webClient;
    
    @Value("${ai.siliconflow.api-key:}")
    private String configApiKey;
    
    @Value("${ai.siliconflow.base-url:https://api.siliconflow.cn/v1}")
    private String configBaseUrl;
    
    @Value("${ai.siliconflow.model:Qwen/Qwen2.5-7B-Instruct}")
    private String configDefaultModel;
    
    @Value("${ai.siliconflow.temperature:0.3}")
    private Double temperature;
    
    @Value("${ai.siliconflow.max-tokens:2000}")
    private Integer maxTokens;
    
    @Value("${ai.siliconflow.timeout:30000}")
    private Integer timeout;
    
    public SiliconFlowAIService(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    
    /**
     * 获取API密钥（优先从数据库读取）
     */
    private String getApiKey() {
        String dbApiKey = systemConfigService.getConfig("ai.apiKey", "");
        if (dbApiKey != null && !dbApiKey.trim().isEmpty()) {
            return dbApiKey;
        }
        return configApiKey;
    }
    
    /**
     * 获取API URL（优先从数据库读取）
     */
    private String getBaseUrl() {
        String dbBaseUrl = systemConfigService.getConfig("ai.apiUrl", "");
        if (dbBaseUrl != null && !dbBaseUrl.trim().isEmpty()) {
            return dbBaseUrl.replace("/chat/completions", ""); // 移除可能的后缀
        }
        return configBaseUrl;
    }
    
    /**
     * 获取模型名称（优先从数据库读取）
     */
    private String getDefaultModel() {
        String dbModel = systemConfigService.getConfig("ai.modelName", "");
        if (dbModel != null && !dbModel.trim().isEmpty()) {
            return dbModel;
        }
        return configDefaultModel;
    }
    
    /**
     * 调用硅基流动AI API
     * @param messages 消息列表
     * @return AI响应
     */
    public String chat(List<Map<String, String>> messages) {
        return chat(messages, getDefaultModel(), temperature, maxTokens);
    }
    
    /**
     * 调用硅基流动AI API（自定义参数）
     * @param messages 消息列表
     * @param model 模型名称
     * @param temperature 温度参数
     * @param maxTokens 最大token数
     * @return AI响应
     */
    public String chat(List<Map<String, String>> messages, String model, Double temperature, Integer maxTokens) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("stream", false);
            
            String actualApiKey = getApiKey();
            String actualBaseUrl = getBaseUrl();
            
            log.debug("调用硅基流动API：model={}, baseUrl={}, messages={}", model, actualBaseUrl, JSON.toJSONString(messages));
            
            // 验证API密钥
            if (actualApiKey == null || actualApiKey.trim().isEmpty()) {
                throw new RuntimeException("AI API密钥未配置，请在系统设置中配置AI密钥");
            }
            
            // 发送请求（拼接完整的API端点）
            String chatCompletionsUrl = actualBaseUrl + "/chat/completions";
            String response = webClient.post()
                    .uri(chatCompletionsUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + actualApiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();
            
            // 解析响应
            JSONObject jsonResponse = JSON.parseObject(response);
            String content = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
            
            log.debug("硅基流动API响应：{}", content);
            return content;
            
        } catch (Exception e) {
            log.error("调用硅基流动API失败", e);
            throw new RuntimeException("AI服务调用失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 简化的单消息调用
     * @param systemPrompt 系统提示词
     * @param userMessage 用户消息
     * @return AI响应
     */
    public String chat(String systemPrompt, String userMessage) {
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        );
        return chat(messages);
    }
}

