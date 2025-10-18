package com.springboot.tiku.dto.answer;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI判题记录DTO（含关联数据）
 */
@Data
public class AIGradingRecordDTO {
    
    private Long id;
    
    private Long answerRecordId;
    
    private Long questionId;
    
    private Long userId;
    
    private String username;
    
    /**
     * 题目内容（从Question表获取）
     */
    private String questionContent;
    
    /**
     * 用户答案
     */
    private String userAnswer;
    
    /**
     * AI评分
     */
    private BigDecimal score;
    
    /**
     * 置信度（0-1）
     */
    private BigDecimal confidence;
    
    /**
     * AI评语（从aiFeedback中提取）
     */
    private String feedback;
    
    /**
     * AI模型
     */
    private String aiModel;
    
    /**
     * AI反馈详情（JSON）
     */
    private Map<String, Object> aiFeedback;
    
    /**
     * 是否人工复核
     */
    private Boolean manualReview;
    
    /**
     * 人工评分
     */
    private BigDecimal manualScore;
    
    /**
     * 复核教师ID
     */
    private Long reviewerId;
    
    /**
     * 复核意见
     */
    private String reviewComment;
    
    /**
     * 最终得分
     */
    private BigDecimal finalScore;
    
    /**
     * 判题耗时（毫秒）
     */
    private Integer gradingTime;
    
    /**
     * 判题时间
     */
    private LocalDateTime gradedAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

