package com.springboot.tiku.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * 学习日历DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyCalendar {
    
    /**
     * 年份
     */
    private Integer year;
    
    /**
     * 月份
     */
    private Integer month;
    
    /**
     * 日期学习数据 Map<日期, 学习数据>
     * key: "2024-10-17"
     * value: DayStudyData
     */
    private Map<String, DayStudyData> studyData;
    
    /**
     * 本月连续打卡天数
     */
    private Integer continuousDays;
    
    /**
     * 本月累计打卡天数
     */
    private Integer totalDays;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayStudyData {
        /**
         * 日期
         */
        private LocalDate date;
        
        /**
         * 是否打卡
         */
        private Boolean checked;
        
        /**
         * 答题数
         */
        private Long answeredCount;
        
        /**
         * 正确率
         */
        private Integer accuracy;
        
        /**
         * 学习时长（分钟）
         */
        private Long studyMinutes;
    }
}



