package com.springboot.tiku.dto.question;

import com.springboot.tiku.entity.Question;
import lombok.Data;

/**
 * 题目查询请求DTO
 */
@Data
public class QuestionQueryRequest {
    
    /**
     * 学科ID
     */
    private Long subjectId;
    
    /**
     * 章节ID
     */
    private Long chapterId;
    
    /**
     * 题型
     */
    private Question.QuestionType type;
    
    /**
     * 难度
     */
    private Question.Difficulty difficulty;
    
    /**
     * 关键词搜索（搜索标题和内容）
     */
    private String keyword;
    
    /**
     * 标签
     */
    private String tag;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 创建者ID
     */
    private Long creatorId;
}



