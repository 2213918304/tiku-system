package com.springboot.tiku.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 章节实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "chapter", indexes = {
    @Index(name = "idx_subject_id", columnList = "subject_id"),
    @Index(name = "idx_parent_id", columnList = "parent_id")
})
public class Chapter extends BaseEntity {
    
    /**
     * 学科ID
     */
    @Column(name = "subject_id", nullable = false)
    private Long subjectId;
    
    /**
     * 章节名称
     */
    @Column(nullable = false, length = 200)
    private String name;
    
    /**
     * 父章节ID（0表示顶级章节）
     */
    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;
    
    /**
     * 章节层级（1-一级章节，2-二级章节，以此类推）
     */
    @Column(nullable = false)
    private Integer level = 1;
    
    /**
     * 排序号
     */
    @Column(nullable = false)
    private Integer sortOrder = 0;
    
    /**
     * 章节描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 题目数量
     */
    @Column(nullable = false)
    private Integer questionCount = 0;
}




