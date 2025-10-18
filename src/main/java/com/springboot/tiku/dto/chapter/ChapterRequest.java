package com.springboot.tiku.dto.chapter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 章节请求DTO
 */
@Data
public class ChapterRequest {
    
    @NotNull(message = "学科ID不能为空")
    private Long subjectId;
    
    @NotBlank(message = "章节名称不能为空")
    @Size(max = 200, message = "章节名称不能超过200个字符")
    private String name;
    
    private Long parentId = 0L;
    
    private Integer level = 1;
    
    private Integer sortOrder = 0;
    
    private String description;
}



