package com.springboot.tiku.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    /**
     * Token
     */
    private String token;
    
    /**
     * Token类型
     */
    private String tokenType = "Bearer";
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 角色
     */
    private String role;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 头像
     */
    private String avatar;
    
    public LoginResponse(String token, Long userId, String username, String role, String realName, String avatar) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.realName = realName;
        this.avatar = avatar;
    }
}




