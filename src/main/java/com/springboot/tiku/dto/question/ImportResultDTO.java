package com.springboot.tiku.dto.question;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果DTO
 */
@Data
public class ImportResultDTO {
    
    /**
     * 成功数量
     */
    private int successCount;
    
    /**
     * 失败数量
     */
    private int failCount;
    
    /**
     * 总数量
     */
    private int totalCount;
    
    /**
     * 创建的章节数量
     */
    private int createdChapters;
    
    /**
     * 错误消息列表
     */
    private List<String> errors = new ArrayList<>();
    
    /**
     * 章节映射（章节名称 -> 题目数量）
     */
    private java.util.Map<String, Integer> chapterStats = new java.util.HashMap<>();
    
    public void addError(String error) {
        errors.add(error);
    }
    
    public void incrementSuccess() {
        successCount++;
    }
    
    public void incrementFail() {
        failCount++;
    }
}

