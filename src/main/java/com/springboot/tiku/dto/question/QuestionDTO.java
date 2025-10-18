package com.springboot.tiku.dto.question;

import com.springboot.tiku.entity.Question;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 题目DTO
 */
@Data
public class QuestionDTO {
    
    private Long id;
    
    private Long subjectId;
    
    private String subjectName;
    
    private Long chapterId;
    
    private String chapterName;
    
    private Question.QuestionType type;
    
    private String title;
    
    private String content;
    
    private Question.Difficulty difficulty;
    
    private BigDecimal score;
    
    private Object options;
    
    private Object answer;
    
    private String answerAnalysis;
    
    private Boolean aiGradingEnabled;
    
    private Map<String, Object> aiGradingConfig;
    
    private Object scoringCriteria;
    
    private Object referenceKeywords;
    
    private Integer useCount;
    
    private Integer correctCount;
    
    private Integer wrongCount;
    
    private String tags;
    
    private String knowledgePoints;
    
    private Long creatorId;
    
    private String creatorName;
    
    private Integer status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    /**
     * 正确率（百分比）
     */
    public Double getCorrectRate() {
        if (useCount == null || useCount == 0) {
            return 0.0;
        }
        return (correctCount.doubleValue() / useCount.doubleValue()) * 100;
    }
}



