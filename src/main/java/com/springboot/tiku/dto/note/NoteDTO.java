package com.springboot.tiku.dto.note;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记DTO - 包含关联信息
 */
@Data
public class NoteDTO {
    /**
     * 笔记ID
     */
    private Long id;
    
    /**
     * 笔记标题
     */
    private String title;
    
    /**
     * 笔记内容
     */
    private String content;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 题目ID（可选）
     */
    private Long questionId;
    
    /**
     * 题目标题
     */
    private String questionTitle;
    
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
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}






