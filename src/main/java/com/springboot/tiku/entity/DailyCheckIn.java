package com.springboot.tiku.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 每日打卡实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "daily_checkin",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "check_date"}),
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_check_date", columnList = "check_date")
    }
)
public class DailyCheckIn extends BaseEntity {
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 打卡日期
     */
    @Column(name = "check_date", nullable = false)
    private LocalDate checkDate;
    
    /**
     * 当日答题数
     */
    @Column(nullable = false)
    private Integer answerCount = 0;
    
    /**
     * 当日正确数
     */
    @Column(nullable = false)
    private Integer correctCount = 0;
    
    /**
     * 当日学习时长（秒）
     */
    @Column(nullable = false)
    private Integer studyTime = 0;
    
    /**
     * 连续打卡天数
     */
    @Column(nullable = false)
    private Integer streakDays = 1;
}




