package com.springboot.tiku.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 答题记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "answer_record", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_question_id", columnList = "question_id"),
    @Index(name = "idx_exam_id", columnList = "exam_id"),
    @Index(name = "idx_grading_status", columnList = "grading_status")
})
public class AnswerRecord extends BaseEntity {
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 题目ID
     */
    @Column(name = "question_id", nullable = false)
    private Long questionId;
    
    /**
     * 练习模式
     */
    @Column(length = 50)
    private String practiceMode;
    
    /**
     * 考试ID（如果是考试）
     */
    @Column(name = "exam_id")
    private Long examId;
    
    /**
     * 用户答案（JSON格式）
     */
    @Column(nullable = false, columnDefinition = "JSON")
    private String userAnswer;
    
    /**
     * 是否正确（客观题）
     */
    private Boolean isCorrect;
    
    /**
     * 得分（主观题）
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal score;
    
    /**
     * 判题方式
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GradingType gradingType;
    
    /**
     * 判题状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "grading_status", nullable = false, length = 20)
    private GradingStatus gradingStatus = GradingStatus.PENDING;
    
    /**
     * 答题用时（秒）
     */
    private Integer timeSpent;
    
    /**
     * 是否标记
     */
    @Column(nullable = false)
    private Boolean isMarked = false;
    
    /**
     * 笔记
     */
    @Column(columnDefinition = "TEXT")
    private String note;
    
    /**
     * 答题时间
     */
    private LocalDateTime answeredAt;
    
    /**
     * 判题完成时间
     */
    private LocalDateTime gradedAt;
    
    /**
     * 判题方式枚举
     */
    public enum GradingType {
        AUTO,    // 自动判题（客观题）
        AI,      // AI判题
        MANUAL   // 人工判题
    }
    
    /**
     * 判题状态枚举
     */
    public enum GradingStatus {
        PENDING,    // 待判题
        GRADED,     // 已判题
        REVIEWING   // 复核中
    }
}




