package com.springboot.tiku.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

/**
 * AI判题记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ai_grading_record", indexes = {
    @Index(name = "idx_answer_record_id", columnList = "answer_record_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_question_id", columnList = "question_id")
})
public class AiGradingRecord extends BaseEntity {
    
    /**
     * 答题记录ID
     */
    @Column(name = "answer_record_id", nullable = false)
    private Long answerRecordId;
    
    /**
     * 题目ID
     */
    @Column(name = "question_id", nullable = false)
    private Long questionId;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 学生答案
     */
    @Column(columnDefinition = "TEXT")
    private String studentAnswer;
    
    /**
     * 使用的AI模型
     */
    @Column(length = 50)
    private String aiModel;
    
    /**
     * AI评分
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal aiScore;
    
    /**
     * 置信度（0-1之间）
     */
    @Column(precision = 3, scale = 2)
    private BigDecimal aiConfidence;
    
    /**
     * AI反馈（JSON格式）
     * {
     *   "scoreDetails": [...],
     *   "strengths": [...],
     *   "weaknesses": [...],
     *   "suggestions": "...",
     *   "comment": "..."
     * }
     */
    @Column(columnDefinition = "JSON")
    private String aiFeedback;
    
    /**
     * 是否人工复核
     */
    @Column(nullable = false)
    private Boolean manualReview = false;
    
    /**
     * 人工评分
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal manualScore;
    
    /**
     * 复核教师ID
     */
    @Column(name = "reviewer_id")
    private Long reviewerId;
    
    /**
     * 复核意见
     */
    @Column(columnDefinition = "TEXT")
    private String reviewComment;
    
    /**
     * 最终得分
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal finalScore;
    
    /**
     * 判题耗时（毫秒）
     */
    private Integer gradingTime;
}




