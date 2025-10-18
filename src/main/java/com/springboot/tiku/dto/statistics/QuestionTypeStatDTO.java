package com.springboot.tiku.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题型统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionTypeStatDTO {
    /**
     * 题型名称
     */
    private String typeName;
    
    /**
     * 答题数量
     */
    private Long count;
    
    /**
     * 正确数量
     */
    private Long correctCount;
    
    /**
     * 正确率
     */
    private Integer accuracy;
}




