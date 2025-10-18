package com.springboot.tiku.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 学习计划实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "study_plan", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_subject_id", columnList = "subject_id")
})
public class StudyPlan extends BaseEntity {
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 学科ID
     */
    @Column(name = "subject_id")
    private Long subjectId;
    
    /**
     * 计划名称
     */
    @Column(nullable = false, length = 200)
    private String name;
    
    /**
     * 计划描述
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 每日目标题数
     */
    @Column(nullable = false)
    private Integer dailyTarget;
    
    /**
     * 已完成天数
     */
    @Column(nullable = false)
    private Integer completedDays = 0;
    
    /**
     * 开始日期
     */
    @Column(nullable = false)
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    @Column(nullable = false)
    private LocalDate endDate;
    
    /**
     * 计划状态：ACTIVE-进行中, COMPLETED-已完成, CANCELLED-已取消
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanStatus status = PlanStatus.ACTIVE;
    
    /**
     * 完成进度（百分比）
     */
    @Column(nullable = false)
    private Integer progress = 0;
    
    /**
     * 计划状态枚举
     */
    public enum PlanStatus {
        ACTIVE,     // 进行中
        COMPLETED,  // 已完成
        CANCELLED   // 已取消
    }
}


