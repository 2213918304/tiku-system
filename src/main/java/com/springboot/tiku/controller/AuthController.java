package com.springboot.tiku.controller;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.common.ResultCode;
import com.springboot.tiku.dto.auth.LoginRequest;
import com.springboot.tiku.dto.auth.LoginResponse;
import com.springboot.tiku.dto.auth.RegisterRequest;
import com.springboot.tiku.entity.User;
import com.springboot.tiku.exception.BusinessException;
import com.springboot.tiku.repository.UserRepository;
import com.springboot.tiku.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户注册、登录等接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final UserRepository userRepository;
    
    /**
     * 从 Authentication 中获取用户ID
     */
    private Long getUserId(org.springframework.security.core.Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
        return user.getId();
    }
    
    /**
     * 用户注册
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }
    
    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }
    
    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current")
    public Result<LoginResponse> getCurrentUser(org.springframework.security.core.Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(authService.getCurrentUser(userId));
    }
    
    /**
     * 测试接口（需要认证）
     */
    @Operation(summary = "测试接口")
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("认证成功！");
    }
}


