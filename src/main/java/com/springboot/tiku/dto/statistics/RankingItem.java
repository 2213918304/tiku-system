package com.springboot.tiku.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 排行榜项DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingItem {
    
    /**
     * 排名
     */
    private Integer rank;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 真实姓名（可能隐藏部分）
     */
    private String realName;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 排行值（根据排行榜类型不同而不同）
     */
    private Long value;
    
    /**
     * 正确率（仅正确率排行榜使用）
     */
    private BigDecimal accuracy;
    
    /**
     * 总积分（仅积分排行榜使用）
     */
    private Integer points;
    
    /**
     * 是否是当前用户
     */
    private Boolean isCurrentUser;
}



