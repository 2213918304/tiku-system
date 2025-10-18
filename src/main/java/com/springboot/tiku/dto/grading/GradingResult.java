package com.springboot.tiku.dto.grading;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 判题结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingResult {
    
    /**
     * 答题记录ID
     */
    private Long answerRecordId;
    
    /**
     * 是否正确（客观题）
     */
    private Boolean isCorrect;
    
    /**
     * 得分
     */
    private BigDecimal score;
    
    /**
     * 满分
     */
    private BigDecimal totalScore;
    
    /**
     * 判题类型
     */
    private String gradingType;
    
    /**
     * 正确答案
     */
    private Object correctAnswer;
    
    /**
     * 答案解析
     */
    private String answerAnalysis;
    
    /**
     * AI判题详细反馈（仅AI判题）
     */
    private AIFeedback aiFeedback;
    
    /**
     * 是否需要人工复核
     */
    private Boolean needManualReview;
    
    /**
     * AI判题反馈
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIFeedback {
        /**
         * AI模型
         */
        private String model;
        
        /**
         * 置信度
         */
        private BigDecimal confidence;
        
        /**
         * 分项得分
         */
        private List<ScoreDetail> scoreDetails;
        
        /**
         * 优点
         */
        private List<String> strengths;
        
        /**
         * 不足
         */
        private List<String> weaknesses;
        
        /**
         * 改进建议
         */
        private String suggestions;
        
        /**
         * 总体评语
         */
        private String comment;
    }
    
    /**
     * 分项得分详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreDetail {
        /**
         * 维度名称
         */
        private String dimension;
        
        /**
         * 得分
         */
        private BigDecimal score;
        
        /**
         * 满分
         */
        private BigDecimal maxScore;
        
        /**
         * 评分理由
         */
        private String reason;
    }
}



