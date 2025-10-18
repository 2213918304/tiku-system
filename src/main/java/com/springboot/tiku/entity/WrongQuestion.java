package com.springboot.tiku.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 错题本实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "wrong_question",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "question_id"}),
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_question_id", columnList = "question_id"),
        @Index(name = "idx_status", columnList = "status")
    }
)
public class WrongQuestion extends BaseEntity {
    
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
     * 错误次数
     */
    @Column(nullable = false)
    private Integer wrongCount = 1;
    
    /**
     * 最后一次错误的答题记录ID
     */
    @Column(name = "last_answer_record_id")
    private Long lastAnswerRecordId;
    
    /**
     * 状态：易错、反复错、已掌握
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WrongStatus status = WrongStatus.WRONG;
    
    /**
     * 是否已移除
     */
    @Column(nullable = false)
    private Boolean removed = false;
    
    /**
     * 错题状态枚举
     */
    public enum WrongStatus {
        WRONG,          // 易错
        REPEATED_WRONG, // 反复错（3次以上）
        MASTERED        // 已掌握
    }
}




