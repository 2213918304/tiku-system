package com.springboot.tiku.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 * 配置静态资源访问和前端路由支持
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    /**
     * 配置静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 不设置缓存，避免开发时缓存问题
        // Spring Boot会自动处理classpath:/static/下的静态资源
        
        // 配置上传文件访问
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}

