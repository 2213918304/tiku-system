package com.springboot.tiku.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 成就徽章实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "achievement")
public class Achievement extends BaseEntity {
    
    /**
     * 成就名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 成就代码（唯一）
     */
    @Column(unique = true, length = 50)
    private String code;
    
    /**
     * 成就描述
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 成就图标
     */
    @Column(length = 500)
    private String icon;
    
    /**
     * 成就类型：ANSWER-答题类, ACCURACY-正确率类, STREAK-连续类, SPECIAL-特殊类
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AchievementType type;
    
    /**
     * 达成条件（JSON格式或描述）
     */
    @Column(name = "achieve_condition", columnDefinition = "TEXT")
    private String condition;
    
    /**
     * 排序
     */
    @Column(nullable = false)
    private Integer sortOrder = 0;
    
    /**
     * 成就类型枚举
     */
    public enum AchievementType {
        ANSWER,    // 答题类（如：答题100题）
        ACCURACY,  // 正确率类（如：正确率90%）
        STREAK,    // 连续类（如：连续7天学习）
        SPECIAL    // 特殊类（如：全勤、满分）
    }
}


