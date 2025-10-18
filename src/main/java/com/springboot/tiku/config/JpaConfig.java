package com.springboot.tiku.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA配置
 * 启用审计功能（自动填充创建时间、更新时间）
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}




