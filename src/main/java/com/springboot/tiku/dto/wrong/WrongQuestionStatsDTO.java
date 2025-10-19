package com.springboot.tiku.dto.wrong;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 错题统计DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WrongQuestionStatsDTO {
    /**
     * 错题总数
     */
    private Long totalWrong;
    
    /**
     * 待复习（错误+多次错误）
     */
    private Long needReview;
    
    /**
     * 已掌握
     */
    private Long mastered;
}






