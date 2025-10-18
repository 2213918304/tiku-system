package com.springboot.tiku.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 学习趋势DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyTrendDTO {
    /**
     * 日期列表
     */
    private List<LocalDate> dates;
    
    /**
     * 每日答题数
     */
    private List<Long> answerCounts;
    
    /**
     * 每日正确率
     */
    private List<Integer> accuracyList;
    
    /**
     * 每日学习时长（分钟）
     */
    private List<Long> studyMinutes;
}




