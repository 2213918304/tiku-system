package com.springboot.tiku.dto.subject;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学科DTO
 */
@Data
public class SubjectDTO {
    
    private Long id;
    
    private String name;
    
    private String code;
    
    private String icon;
    
    private String description;
    
    private Integer sortOrder;
    
    private Integer status;
    
    private Integer questionCount;
    
    private Integer studentCount;
    
    /**
     * 章节数量
     */
    private Integer chapterCount;
    
    /**
     * 当前用户已答题数
     */
    private Integer answeredCount;
    
    private LocalDateTime createdAt;
}



