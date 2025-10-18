package com.springboot.tiku.dto.favorite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收藏统计DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteStatsDTO {
    /**
     * 收藏总数
     */
    private Long totalCount;
    
    /**
     * 本周新增
     */
    private Long weekCount;
    
    /**
     * 已练习数量
     */
    private Long practicedCount;
}




