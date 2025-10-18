package com.springboot.tiku.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户学习统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 总答题数
     */
    private Long totalAnswered;
    
    /**
     * 正确题数
     */
    private Long correctCount;
    
    /**
     * 错误题数
     */
    private Long wrongCount;
    
    /**
     * 正确率（百分比）
     */
    private BigDecimal accuracy;
    
    /**
     * 累计学习时长（分钟）
     */
    private Long totalStudyMinutes;
    
    /**
     * 连续打卡天数
     */
    private Integer continuousDays;
    
    /**
     * 累计打卡天数
     */
    private Integer totalCheckInDays;
    
    /**
     * 收藏题目数
     */
    private Long favoriteCount;
    
    /**
     * 笔记数量
     */
    private Long noteCount;
    
    /**
     * 错题本题数
     */
    private Long wrongQuestionCount;
    
    /**
     * 获得的成就数
     */
    private Integer achievementCount;
    
    /**
     * 总积分
     */
    private Integer totalPoints;
    
    /**
     * 当前排名
     */
    private Integer ranking;
    
    /**
     * 最近学习时间
     */
    private LocalDateTime lastStudyTime;
}



