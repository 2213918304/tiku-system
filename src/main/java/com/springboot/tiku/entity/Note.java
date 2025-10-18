package com.springboot.tiku.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 笔记实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "note", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_question_id", columnList = "question_id")
})
public class Note extends BaseEntity {
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 题目ID（可选，null表示独立笔记）
     */
    @Column(name = "question_id")
    private Long questionId;
    
    /**
     * 学科ID（可选）
     */
    @Column(name = "subject_id")
    private Long subjectId;
    
    /**
     * 笔记内容
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    /**
     * 笔记标题
     */
    @Column(length = 200)
    private String title;
}




