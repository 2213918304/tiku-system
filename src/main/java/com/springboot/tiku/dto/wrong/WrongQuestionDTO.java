package com.springboot.tiku.dto.wrong;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 错题DTO - 包含题目详细信息
 */
@Data
public class WrongQuestionDTO {
    /**
     * 错题记录ID
     */
    private Long wrongQuestionId;
    
    /**
     * 题目ID
     */
    private Long questionId;
    
    /**
     * 题目类型
     */
    private String type;
    
    /**
     * 难度
     */
    private String difficulty;
    
    /**
     * 题目标题
     */
    private String title;
    
    /**
     * 题目内容
     */
    private String content;
    
    /**
     * 选项（JSON字符串）
     */
    private String options;
    
    /**
     * 正确答案
     */
    private Object correctAnswer;
    
    /**
     * 我的答案
     */
    private Object userAnswer;
    
    /**
     * 答案解析
     */
    private String explanation;
    
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
     * 错误次数
     */
    private Integer wrongCount;
    
    /**
     * 状态：WRONG, REPEATED_WRONG, MASTERED
     */
    private String status;
    
    /**
     * 最后错误时间
     */
    private LocalDateTime lastWrongAt;
    
    /**
     * 错误记录
     */
    private List<WrongRecord> wrongRecords;
    
    @Data
    public static class WrongRecord {
        private LocalDateTime wrongAt;
        private Object userAnswer;
    }
}




