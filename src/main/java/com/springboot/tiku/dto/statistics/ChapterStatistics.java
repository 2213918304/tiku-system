package com.springboot.tiku.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 章节学习统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterStatistics {
    
    /**
     * 章节ID
     */
    private Long chapterId;
    
    /**
     * 章节名称
     */
    private String chapterName;
    
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
     * 章节题目总数
     */
    private Integer totalQuestions;
    
    /**
     * 掌握程度（0-100）
     */
    private Integer masteryLevel;
}



