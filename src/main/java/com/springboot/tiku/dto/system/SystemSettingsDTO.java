package com.springboot.tiku.dto.system;

import lombok.Data;

import java.util.List;

/**
 * 系统设置DTO
 */
@Data
public class SystemSettingsDTO {
    
    /**
     * 基本设置
     */
    private BasicSettings basic;
    
    /**
     * 答题设置
     */
    private PracticeSettings practice;
    
    /**
     * AI设置
     */
    private AISettings ai;
    
    /**
     * 安全设置
     */
    private SecuritySettings security;
    
    @Data
    public static class BasicSettings {
        private String systemName;
        private String systemDesc;
        private Boolean allowRegister;
        private String defaultRole;
    }
    
    @Data
    public static class PracticeSettings {
        private Integer defaultQuestionCount;
        private Integer examDuration;
        private Integer timedChallengeDuration;
        private Boolean autoGrading;
        private Boolean showExplanation;
    }
    
    @Data
    public static class AISettings {
        private Boolean enableAI;
        private String apiKey;
        private String apiUrl;
        private String modelName;
    }
    
    @Data
    public static class SecuritySettings {
        private Integer minPasswordLength;
        private List<String> passwordComplexity;
        private Integer sessionTimeout;
        private Integer maxLoginAttempts;
    }
}

