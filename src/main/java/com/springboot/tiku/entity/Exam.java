package com.springboot.tiku.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 考试/试卷实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "exam", indexes = {
    @Index(name = "idx_subject_id", columnList = "subject_id"),
    @Index(name = "idx_creator_id", columnList = "creator_id"),
    @Index(name = "idx_status", columnList = "status")
})
public class Exam extends BaseEntity {
    
    /**
     * 试卷名称
     */
    @Column(nullable = false, length = 200)
    private String name;
    
    /**
     * 学科ID
     */
    @Column(name = "subject_id", nullable = false)
    private Long subjectId;
    
    /**
     * 试卷描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 考试类型：PRACTICE-练习, MOCK-模拟考, FORMAL-正式考试
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamType type = ExamType.PRACTICE;
    
    /**
     * 总分
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal totalScore = BigDecimal.ZERO;
    
    /**
     * 及格分
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal passScore;
    
    /**
     * 考试时长（分钟）
     */
    @Column(nullable = false)
    private Integer duration;
    
    /**
     * 题目配置（JSON格式）
     * {
     *   "single": {"count": 30, "scorePerQuestion": 2},
     *   "multiple": {"count": 20, "scorePerQuestion": 3},
     *   ...
     * }
     */
    @Column(columnDefinition = "JSON")
    private String questionConfig;
    
    /**
     * 题目ID列表（JSON格式）
     * 固定试卷使用，存储固定的题目ID列表
     */
    @Column(columnDefinition = "JSON")
    private String questionIds;
    
    /**
     * 是否随机题目（每次考试随机抽题）
     */
    @Column(nullable = false)
    private Boolean randomQuestion = false;
    
    /**
     * 是否随机选项顺序
     */
    @Column(nullable = false)
    private Boolean randomOption = false;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 创建者ID
     */
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    
    /**
     * 参考人数
     */
    @Column(nullable = false)
    private Integer participantCount = 0;
    
    /**
     * 状态：1-启用, 0-禁用
     */
    @Column(nullable = false)
    private Integer status = 1;
    
    /**
     * 考试类型枚举
     */
    public enum ExamType {
        PRACTICE,  // 练习
        MOCK,      // 模拟考
        FORMAL     // 正式考试
    }
}




