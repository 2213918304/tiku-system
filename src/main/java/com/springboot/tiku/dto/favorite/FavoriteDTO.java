package com.springboot.tiku.dto.favorite;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收藏DTO - 包含题目详细信息
 */
@Data
public class FavoriteDTO {
    /**
     * 收藏ID
     */
    private Long favoriteId;
    
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
     * 答案
     */
    private Object answer;
    
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
     * 收藏分类
     */
    private String category;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 练习次数
     */
    private Integer practiceCount;
    
    /**
     * 收藏时间
     */
    private LocalDateTime favoriteAt;
}

