package com.springboot.tiku.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 学科学习统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectStatistics {
    
    /**
     * 学科ID
     */
    private Long subjectId;
    
    /**
     * 学科名称
     */
    private String subjectName;
    
    /**
     * 已答题数
     */
    private Long answeredCount;
    
    /**
     * 正确题数
     */
    private Long correctCount;
    
    /**
     * 正确率
     */
    private BigDecimal accuracy;
    
    /**
     * 学科题目总数
     */
    private Integer totalQuestions;
    
    /**
     * 完成进度（百分比）
     */
    private BigDecimal progress;
}



