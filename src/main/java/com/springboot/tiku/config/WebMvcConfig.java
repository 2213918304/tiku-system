package com.springboot.tiku.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
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
        // 配置前端静态资源（只处理真实的静态文件）
        registry.addResourceHandler("/assets/**", "/vite.svg", "/favicon.ico", "/index.html")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600)
                .resourceChain(false);
        
        // 配置上传文件访问
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/")
                .setCachePeriod(3600);
    }
}

