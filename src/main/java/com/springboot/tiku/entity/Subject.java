package com.springboot.tiku.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学科实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "subject", indexes = {
    @Index(name = "idx_code", columnList = "code")
})
public class Subject extends BaseEntity {
    
    /**
     * 学科名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 学科代码（唯一）
     */
    @Column(unique = true, length = 50)
    private String code;
    
    /**
     * 学科图标URL
     */
    @Column(length = 500)
    private String icon;
    
    /**
     * 学科描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 排序号
     */
    @Column(nullable = false)
    private Integer sortOrder = 0;
    
    /**
     * 状态：1-启用, 0-禁用
     */
    @Column(nullable = false)
    private Integer status = 1;
    
    /**
     * 题目总数
     */
    @Column(nullable = false)
    private Integer questionCount = 0;
    
    /**
     * 学习人数
     */
    @Column(nullable = false)
    private Integer studentCount = 0;
}




