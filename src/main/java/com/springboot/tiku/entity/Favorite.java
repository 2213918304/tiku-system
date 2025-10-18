package com.springboot.tiku.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收藏实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "favorite", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "question_id"}),
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_question_id", columnList = "question_id")
    }
)
public class Favorite extends BaseEntity {
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 题目ID
     */
    @Column(name = "question_id", nullable = false)
    private Long questionId;
    
    /**
     * 收藏分类（重点题、易错题、考前必看、自定义）
     */
    @Column(length = 50)
    private String category;
    
    /**
     * 备注
     */
    @Column(length = 500)
    private String remark;
}




