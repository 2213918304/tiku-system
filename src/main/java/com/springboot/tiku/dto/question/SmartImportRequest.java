package com.springboot.tiku.dto.question;

import lombok.Data;

import java.util.List;

/**
 * 智能导入请求（根据Excel自动归类章节）
 */
@Data
public class SmartImportRequest {
    
    /**
     * 学科ID
     */
    private Long subjectId;
    
    /**
     * 题目列表
     */
    private List<QuestionImportItem> questions;
    
    @Data
    public static class QuestionImportItem {
        /**
         * 章节序号（如：1、2、3）
         */
        private String chapterOrder;
        
        /**
         * 章节名称
         */
        private String chapterName;
        
        /**
         * 题目内容
         */
        private String content;
        
        /**
         * 题型
         */
        private String type;
        
        /**
         * 选项（JSON数组字符串）
         */
        private List<OptionItem> options;
        
        /**
         * 正确答案
         */
        private String correctAnswer;
        
        /**
         * 解析
         */
        private String explanation;
        
        /**
         * 难度（1-5）
         */
        private Integer difficulty;
        
        /**
         * 分值
         */
        private Double score;
    }
    
    @Data
    public static class OptionItem {
        private String content;
    }
}

