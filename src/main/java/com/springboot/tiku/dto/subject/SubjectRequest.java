package com.springboot.tiku.dto.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 学科请求DTO
 */
@Data
public class SubjectRequest {
    
    @NotBlank(message = "学科名称不能为空")
    @Size(max = 100, message = "学科名称不能超过100个字符")
    private String name;
    
    @Size(max = 50, message = "学科代码不能超过50个字符")
    private String code;
    
    private String icon;
    
    private String description;
    
    private Integer sortOrder = 0;
    
    private Integer status = 1;
}



