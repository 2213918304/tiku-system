package com.springboot.tiku.dto.practice;

import com.springboot.tiku.entity.Question;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 刷题请求DTO
 */
@Data
public class PracticeRequest {
    
    /**
     * 刷题模式
     */
    @NotNull(message = "刷题模式不能为空")
    private PracticeMode mode;
    
    /**
     * 学科ID（必填）
     */
    @NotNull(message = "学科ID不能为空")
    private Long subjectId;
    
    /**
     * 章节ID（章节练习模式必填）
     */
    private Long chapterId;
    
    /**
     * 题型（可选，不填则全部题型）
     */
    private Question.QuestionType questionType;
    
    /**
     * 难度（可选）
     */
    private Question.Difficulty difficulty;
    
    /**
     * 题目数量
     */
    private Integer count = 20;
    
    /**
     * 是否继续上次进度（顺序刷题模式）
     */
    private Boolean continueProgress = false;
    
    /**
     * 每题限时秒数（限时挑战模式）
     */
    private Integer timePerQuestion = 30;
    
    /**
     * 考试时长（分钟）- 考试模拟模式
     */
    private Integer examDuration = 90;
    
    /**
     * 关卡ID（闯关模式）
     */
    private Integer challengeLevel;
}



