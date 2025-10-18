package com.springboot.tiku.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 系统配置实体
 */
@Data
@Entity
@Table(name = "system_config")
public class SystemConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 配置键
     */
    @Column(unique = true, nullable = false)
    private String configKey;
    
    /**
     * 配置值
     */
    @Column(columnDefinition = "TEXT")
    private String configValue;
    
    /**
     * 配置分类
     */
    private String category;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    @Column(updatable = false)
    private java.time.LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private java.time.LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
}

