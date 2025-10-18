package com.springboot.tiku.repository;

import com.springboot.tiku.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 系统配置Repository
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    
    /**
     * 根据配置键查找
     */
    Optional<SystemConfig> findByConfigKey(String configKey);
    
    /**
     * 根据分类查找
     */
    List<SystemConfig> findByCategory(String category);
    
    /**
     * 删除配置
     */
    void deleteByConfigKey(String configKey);
}

