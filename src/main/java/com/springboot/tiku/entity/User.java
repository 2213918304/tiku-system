package com.springboot.tiku.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email")
})
public class User extends BaseEntity {
    
    /**
     * 用户名（唯一）
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    /**
     * 密码（加密存储）
     */
    @Column(nullable = false, length = 100)
    private String password;
    
    /**
     * 邮箱
     */
    @Column(length = 100)
    private String email;
    
    /**
     * 手机号
     */
    @Column(length = 20)
    private String phone;
    
    /**
     * 真实姓名
     */
    @Column(length = 50)
    private String realName;
    
    /**
     * 头像URL
     */
    @Column(length = 500)
    private String avatar;
    
    /**
     * 角色：STUDENT-学生, TEACHER-教师, ADMIN-管理员
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.STUDENT;
    
    /**
     * 账号状态：1-正常, 0-禁用
     */
    @Column(nullable = false)
    private Integer status = 1;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录IP
     */
    @Column(length = 50)
    private String lastLoginIp;
    
    /**
     * 学习总时长（秒）
     */
    @Column(nullable = false)
    private Long totalStudyTime = 0L;
    
    /**
     * 累计答题数
     */
    @Column(nullable = false)
    private Integer totalAnswerCount = 0;
    
    /**
     * 累计正确数
     */
    @Column(nullable = false)
    private Integer totalCorrectCount = 0;
    
    /**
     * 个人简介
     */
    @Column(length = 500)
    private String bio;
    
    /**
     * 用户角色枚举
     */
    public enum UserRole {
        STUDENT,  // 学生
        TEACHER,  // 教师
        ADMIN     // 管理员
    }
}




