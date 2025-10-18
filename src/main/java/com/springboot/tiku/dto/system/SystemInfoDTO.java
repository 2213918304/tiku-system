package com.springboot.tiku.dto.system;

import lombok.Data;

/**
 * 系统信息DTO
 */
@Data
public class SystemInfoDTO {
    
    /**
     * 系统版本
     */
    private String version;
    
    /**
     * 数据库类型
     */
    private String databaseType;
    
    /**
     * Java版本
     */
    private String javaVersion;
    
    /**
     * 系统运行时间
     */
    private String runtime;
    
    /**
     * 数据库大小（MB）
     */
    private Double databaseSize;
    
    /**
     * 数据库总容量（MB）
     */
    private Double databaseTotal;
    
    /**
     * 文件存储大小（MB）
     */
    private Double fileStorageSize;
    
    /**
     * 文件存储总容量（MB）
     */
    private Double fileStorageTotal;
    
    /**
     * 用户总数
     */
    private Long userCount;
    
    /**
     * 题目总数
     */
    private Long questionCount;
    
    /**
     * 答题记录总数
     */
    private Long answerRecordCount;
}

