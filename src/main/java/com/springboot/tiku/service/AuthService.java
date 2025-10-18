package com.springboot.tiku.service;

import com.springboot.tiku.common.ResultCode;
import com.springboot.tiku.dto.auth.LoginRequest;
import com.springboot.tiku.dto.auth.LoginResponse;
import com.springboot.tiku.dto.auth.RegisterRequest;
import com.springboot.tiku.entity.User;
import com.springboot.tiku.exception.BusinessException;
import com.springboot.tiku.repository.UserRepository;
import com.springboot.tiku.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    
    /**
     * 获取当前用户信息
     */
    public LoginResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
        
        return new LoginResponse(null, user.getId(), user.getUsername(), 
                user.getRole().name(), user.getRealName(), user.getAvatar());
    }
    
    /**
     * 用户注册
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }
        
        // 检查邮箱是否已注册
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS);
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRealName(request.getRealName());
        user.setRole(User.UserRole.STUDENT); // 默认学生角色
        user.setStatus(1);
        
        user = userRepository.save(user);
        log.info("用户注册成功：{}", user.getUsername());
        
        // 生成Token并返回
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        return new LoginResponse(token, user.getId(), user.getUsername(), 
                user.getRole().name(), user.getRealName(), user.getAvatar());
    }
    
    /**
     * 用户登录
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            // 认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            
            // 查询用户
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
            
            // 检查账号状态
            if (user.getStatus() == 0) {
                throw new BusinessException(ResultCode.USER_DISABLED);
            }
            
            // 更新登录信息
            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);
            
            // 生成Token
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
            
            log.info("用户登录成功：{}", user.getUsername());
            return new LoginResponse(token, user.getId(), user.getUsername(), 
                    user.getRole().name(), user.getRealName(), user.getAvatar());
                    
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            log.error("登录失败：用户名或密码错误 - {}", request.getUsername());
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        } catch (org.springframework.security.authentication.DisabledException e) {
            log.error("登录失败：账号已被禁用 - {}", request.getUsername());
            throw new BusinessException(ResultCode.USER_DISABLED);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("登录失败：", e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "登录失败，请稍后重试");
        }
    }
}


