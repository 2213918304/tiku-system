package com.springboot.tiku.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "exam_record", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_exam_id", columnList = "exam_id"),
    @Index(name = "idx_status", columnList = "status")
})
public class ExamRecord extends BaseEntity {
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 考试ID
     */
    @Column(name = "exam_id", nullable = false)
    private Long examId;
    
    /**
     * 得分
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal score;
    
    /**
     * 正确题数
     */
    private Integer correctCount;
    
    /**
     * 错误题数
     */
    private Integer wrongCount;
    
    /**
     * 总题数
     */
    @Column(nullable = false)
    private Integer totalCount;
    
    /**
     * 用时（秒）
     */
    private Integer timeSpent;
    
    /**
     * 是否及格
     */
    private Boolean passed;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 提交时间
     */
    private LocalDateTime submitTime;
    
    /**
     * 考试状态：ONGOING-进行中, SUBMITTED-已提交, TIMEOUT-超时
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamStatus status = ExamStatus.ONGOING;
    
    /**
     * 考试状态枚举
     */
    public enum ExamStatus {
        ONGOING,    // 进行中
        SUBMITTED,  // 已提交
        TIMEOUT     // 超时
    }
}




