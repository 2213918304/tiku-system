package com.springboot.tiku.dto.practice;

import com.springboot.tiku.dto.question.QuestionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 刷题会话DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeSession {
    
    /**
     * 会话ID（用于标识本次刷题）
     */
    private String sessionId;
    
    /**
     * 刷题模式
     */
    private PracticeMode mode;
    
    /**
     * 学科ID
     */
    private Long subjectId;
    
    /**
     * 学科名称
     */
    private String subjectName;
    
    /**
     * 章节ID
     */
    private Long chapterId;
    
    /**
     * 章节名称
     */
    private String chapterName;
    
    /**
     * 题目列表
     */
    private List<QuestionDTO> questions;
    
    /**
     * 题目总数
     */
    private Integer totalCount;
    
    /**
     * 当前进度（已答题数）
     */
    private Integer currentProgress;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间（考试模式）
     */
    private LocalDateTime endTime;
    
    /**
     * 限时秒数（限时挑战模式）
     */
    private Integer timePerQuestion;
    
    /**
     * 考试时长（分钟）
     */
    private Integer examDuration;
    
    /**
     * 闯关级别
     */
    private Integer challengeLevel;
    
    /**
     * 通关所需正确题数
     */
    private Integer passRequiredCorrect;
    
    /**
     * 提示信息
     */
    private String tip;
}



