package com.springboot.tiku.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户成就关联实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_achievement",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "achievement_id"}),
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_achievement_id", columnList = "achievement_id")
    }
)
public class UserAchievement extends BaseEntity {
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 成就ID
     */
    @Column(name = "achievement_id", nullable = false)
    private Long achievementId;
    
    /**
     * 获得时间
     */
    @Column(nullable = false)
    private LocalDateTime achievedAt;
}




