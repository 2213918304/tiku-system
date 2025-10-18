package com.springboot.tiku.service;

import com.alibaba.fastjson2.JSON;
import com.springboot.tiku.dto.system.SystemInfoDTO;
import com.springboot.tiku.dto.system.SystemSettingsDTO;
import com.springboot.tiku.entity.SystemConfig;
import com.springboot.tiku.repository.AnswerRecordRepository;
import com.springboot.tiku.repository.QuestionRepository;
import com.springboot.tiku.repository.SystemConfigRepository;
import com.springboot.tiku.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {
    
    private final SystemConfigRepository systemConfigRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRecordRepository answerRecordRepository;
    
    /**
     * 获取配置值
     */
    public String getConfig(String key, String defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse(defaultValue);
    }
    
    /**
     * 设置配置值
     */
    @Transactional
    public void setConfig(String key, String value, String category) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElse(new SystemConfig());
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setCategory(category);
        systemConfigRepository.save(config);
    }
    
    /**
     * 获取所有设置
     */
    public SystemSettingsDTO getAllSettings() {
        SystemSettingsDTO settings = new SystemSettingsDTO();
        
        // 基本设置
        SystemSettingsDTO.BasicSettings basic = new SystemSettingsDTO.BasicSettings();
        basic.setSystemName(getConfig("system.name", "题库系统"));
        basic.setSystemDesc(getConfig("system.desc", "一个功能完善的在线刷题系统"));
        basic.setAllowRegister(Boolean.parseBoolean(getConfig("system.allowRegister", "true")));
        basic.setDefaultRole(getConfig("system.defaultRole", "STUDENT"));
        settings.setBasic(basic);
        
        // 答题设置
        SystemSettingsDTO.PracticeSettings practice = new SystemSettingsDTO.PracticeSettings();
        practice.setDefaultQuestionCount(Integer.parseInt(getConfig("practice.defaultQuestionCount", "20")));
        practice.setExamDuration(Integer.parseInt(getConfig("practice.examDuration", "120")));
        practice.setTimedChallengeDuration(Integer.parseInt(getConfig("practice.timedChallengeDuration", "30")));
        practice.setAutoGrading(Boolean.parseBoolean(getConfig("practice.autoGrading", "true")));
        practice.setShowExplanation(Boolean.parseBoolean(getConfig("practice.showExplanation", "true")));
        settings.setPractice(practice);
        
        // AI设置
        SystemSettingsDTO.AISettings ai = new SystemSettingsDTO.AISettings();
        ai.setEnableAI(Boolean.parseBoolean(getConfig("ai.enable", "false")));
        ai.setApiKey(getConfig("ai.apiKey", ""));
        ai.setApiUrl(getConfig("ai.apiUrl", "https://api.siliconflow.cn/v1/chat/completions"));
        ai.setModelName(getConfig("ai.modelName", "qwen/qwen-plus"));
        settings.setAi(ai);
        
        // 安全设置
        SystemSettingsDTO.SecuritySettings security = new SystemSettingsDTO.SecuritySettings();
        security.setMinPasswordLength(Integer.parseInt(getConfig("security.minPasswordLength", "6")));
        String complexityJson = getConfig("security.passwordComplexity", "[\"数字\",\"小写字母\"]");
        security.setPasswordComplexity(JSON.parseArray(complexityJson, String.class));
        security.setSessionTimeout(Integer.parseInt(getConfig("security.sessionTimeout", "120")));
        security.setMaxLoginAttempts(Integer.parseInt(getConfig("security.maxLoginAttempts", "5")));
        settings.setSecurity(security);
        
        return settings;
    }
    
    /**
     * 保存基本设置
     */
    @Transactional
    public void saveBasicSettings(SystemSettingsDTO.BasicSettings settings) {
        setConfig("system.name", settings.getSystemName(), "basic");
        setConfig("system.desc", settings.getSystemDesc(), "basic");
        setConfig("system.allowRegister", settings.getAllowRegister().toString(), "basic");
        setConfig("system.defaultRole", settings.getDefaultRole(), "basic");
        log.info("保存基本设置成功");
    }
    
    /**
     * 保存答题设置
     */
    @Transactional
    public void savePracticeSettings(SystemSettingsDTO.PracticeSettings settings) {
        setConfig("practice.defaultQuestionCount", settings.getDefaultQuestionCount().toString(), "practice");
        setConfig("practice.examDuration", settings.getExamDuration().toString(), "practice");
        setConfig("practice.timedChallengeDuration", settings.getTimedChallengeDuration().toString(), "practice");
        setConfig("practice.autoGrading", settings.getAutoGrading().toString(), "practice");
        setConfig("practice.showExplanation", settings.getShowExplanation().toString(), "practice");
        log.info("保存答题设置成功");
    }
    
    /**
     * 保存AI设置
     */
    @Transactional
    public void saveAISettings(SystemSettingsDTO.AISettings settings) {
        setConfig("ai.enable", settings.getEnableAI().toString(), "ai");
        setConfig("ai.apiKey", settings.getApiKey(), "ai");
        setConfig("ai.apiUrl", settings.getApiUrl(), "ai");
        setConfig("ai.modelName", settings.getModelName(), "ai");
        log.info("保存AI设置成功");
    }
    
    /**
     * 保存安全设置
     */
    @Transactional
    public void saveSecuritySettings(SystemSettingsDTO.SecuritySettings settings) {
        setConfig("security.minPasswordLength", settings.getMinPasswordLength().toString(), "security");
        setConfig("security.passwordComplexity", JSON.toJSONString(settings.getPasswordComplexity()), "security");
        setConfig("security.sessionTimeout", settings.getSessionTimeout().toString(), "security");
        setConfig("security.maxLoginAttempts", settings.getMaxLoginAttempts().toString(), "security");
        log.info("保存安全设置成功");
    }
    
    /**
     * 获取系统信息
     */
    public SystemInfoDTO getSystemInfo() {
        SystemInfoDTO info = new SystemInfoDTO();
        
        // 系统版本
        info.setVersion("v1.0.0");
        
        // 数据库类型
        info.setDatabaseType("MySQL 8.0");
        
        // Java版本
        info.setJavaVersion(System.getProperty("java.version"));
        
        // 系统运行时间
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        Duration duration = Duration.ofMillis(uptime);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        info.setRuntime(String.format("%d天%d小时%d分钟", days, hours, minutes));
        
        // 数据库大小（模拟）
        info.setDatabaseSize(256.0);
        info.setDatabaseTotal(1024.0);
        
        // 文件存储（模拟）
        info.setFileStorageSize(450.0);
        info.setFileStorageTotal(10240.0);
        
        // 统计数据
        info.setUserCount(userRepository.count());
        info.setQuestionCount(questionRepository.count());
        info.setAnswerRecordCount(answerRecordRepository.count());
        
        return info;
    }
    
    /**
     * 清空答题记录
     */
    @Transactional
    public Map<String, Object> clearAnswerRecords() {
        long count = answerRecordRepository.count();
        answerRecordRepository.deleteAll();
        
        Map<String, Object> result = new HashMap<>();
        result.put("deletedCount", count);
        
        log.warn("清空了{}条答题记录", count);
        return result;
    }
    
    /**
     * 备份数据库（模拟）
     */
    public Map<String, Object> backupDatabase() {
        Map<String, Object> result = new HashMap<>();
        result.put("backupFile", "backup_" + System.currentTimeMillis() + ".sql");
        result.put("backupTime", java.time.LocalDateTime.now());
        result.put("success", true);
        
        log.info("创建数据库备份");
        return result;
    }
    
    /**
     * 重建索引（模拟）
     */
    public Map<String, Object> rebuildIndex() {
        Map<String, Object> result = new HashMap<>();
        result.put("rebuiltTables", new String[]{"question", "answer_record", "user"});
        result.put("rebuildTime", java.time.LocalDateTime.now());
        result.put("success", true);
        
        log.info("重建数据库索引");
        return result;
    }
}

