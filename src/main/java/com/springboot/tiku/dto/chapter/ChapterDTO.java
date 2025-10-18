package com.springboot.tiku.dto.chapter;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 章节DTO
 */
@Data
public class ChapterDTO {
    
    private Long id;
    
    private Long subjectId;
    
    private String name;
    
    private Long parentId;
    
    private Integer level;
    
    private Integer sortOrder;
    
    private String description;
    
    private Integer questionCount;
    
    private LocalDateTime createdAt;
    
    /**
     * 子章节列表（树形结构）
     */
    private List<ChapterDTO> children;
}



