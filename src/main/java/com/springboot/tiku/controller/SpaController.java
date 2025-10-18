package com.springboot.tiku.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA (Single Page Application) 控制器
 * 处理前端路由，所有非API请求都返回index.html
 */
@Controller
public class SpaController {
    
    /**
     * 前端路由转发
     * 除了/api开头的请求，其他都返回index.html
     */
    @GetMapping(value = {
            "/",
            "/login",
            "/register",
            "/dashboard/**",
            "/subjects/**",
            "/practice/**",
            "/wrong/**",
            "/collection/**",
            "/notes/**",
            "/statistics/**",
            "/ranking/**",
            "/profile/**",
            "/admin/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}

