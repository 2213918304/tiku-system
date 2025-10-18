package com.springboot.tiku.dto.answer;

import com.springboot.tiku.entity.AnswerRecord;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 答题记录DTO（含关联数据）
 */
@Data
public class AnswerRecordDTO {
    
    private Long id;
    
    private Long userId;
    
    private String username;
    
    private Long questionId;
    
    private String practiceMode;
    
    private Long examId;
    
    private Object userAnswer;
    
    private Boolean isCorrect;
    
    private BigDecimal score;
    
    private AnswerRecord.GradingType gradingType;
    
    private AnswerRecord.GradingStatus gradingStatus;
    
    private Integer timeSpent;
    
    private Boolean isMarked;
    
    private String note;
    
    private LocalDateTime answeredAt;
    
    private LocalDateTime gradedAt;
    
    /**
     * 题目信息
     */
    private QuestionInfo question;
    
    /**
     * 题目信息内嵌类
     */
    @Data
    public static class QuestionInfo {
        private Long id;
        private String title;
        private String content;
        private String type;
        private Object answer;
        private String answerAnalysis;
        private BigDecimal score;
        private SubjectInfo subject;
        private ChapterInfo chapter;
    }
    
    /**
     * 学科信息内嵌类
     */
    @Data
    public static class SubjectInfo {
        private Long id;
        private String name;
        private String code;
    }
    
    /**
     * 章节信息内嵌类
     */
    @Data
    public static class ChapterInfo {
        private Long id;
        private String name;
    }
}




