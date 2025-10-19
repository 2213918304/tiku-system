package com.springboot.tiku.controller.admin;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.entity.User;
import com.springboot.tiku.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员 - 用户管理控制器
 */
@Tag(name = "管理员-用户管理", description = "管理员管理所有用户")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class AdminUserController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 分页查询用户列表
     */
    @Operation(summary = "分页查询用户列表")
    @GetMapping
    public Result<Page<User>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 构建查询条件
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (username != null && !username.trim().isEmpty()) {
                predicates.add(cb.like(root.get("username"), "%" + username + "%"));
            }
            
            if (role != null && !role.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("role"), User.UserRole.valueOf(role)));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<User> users = userRepository.findAll(spec, pageable);
        return Result.success(users);
    }
    
    /**
     * 获取用户详情
     */
    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return Result.success(user);
    }
    
    /**
     * 创建用户
     */
    @Operation(summary = "创建用户")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> createUser(@RequestBody CreateUserRequest request) {
        // 检查用户名是否存在
        if (userRepository.existsByUsername(request.getUsername())) {
            return Result.error(400, "用户名已存在");
        }
        
        // 检查邮箱是否存在
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                return Result.error(400, "邮箱已被使用");
            }
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole(User.UserRole.valueOf(request.getRole()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1); // 启用状态
        
        User savedUser = userRepository.save(user);
        return Result.success(savedUser);
    }
    
    /**
     * 更新用户
     */
    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查邮箱是否被其他用户使用
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!request.getEmail().equals(user.getEmail()) && 
                userRepository.existsByEmail(request.getEmail())) {
                return Result.error(400, "邮箱已被使用");
            }
        }
        
        user.setRealName(request.getRealName());
        user.setRole(User.UserRole.valueOf(request.getRole()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        
        User updatedUser = userRepository.save(user);
        return Result.success(updatedUser);
    }
    
    /**
     * 删除用户
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 不允许删除管理员
        if (user.getRole() == User.UserRole.ADMIN) {
            return Result.error(400, "不能删除管理员账号");
        }
        
        userRepository.deleteById(id);
        return Result.success();
    }
    
    /**
     * 重置用户密码
     */
    @Operation(summary = "重置用户密码")
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> resetPassword(
            @PathVariable Long id,
            @RequestBody ResetPasswordRequest request
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        String newPassword = request.getNewPassword() != null ? 
                request.getNewPassword() : "123456";
        user.setPassword(passwordEncoder.encode(newPassword));
        
        userRepository.save(user);
        return Result.success();
    }
    
    /**
     * 批量删除用户
     */
    @Operation(summary = "批量删除用户")
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchDeleteUsers(@RequestBody List<Long> ids) {
        // 过滤掉管理员
        List<User> users = userRepository.findAllById(ids);
        List<Long> canDeleteIds = users.stream()
                .filter(u -> u.getRole() != User.UserRole.ADMIN)
                .map(User::getId)
                .toList();
        
        userRepository.deleteAllById(canDeleteIds);
        return Result.success();
    }
    
    // DTO类
    @Data
    public static class CreateUserRequest {
        private String username;
        private String password;
        private String realName;
        private String role;
        private String email;
        private String phone;
    }
    
    @Data
    public static class UpdateUserRequest {
        private String realName;
        private String role;
        private String email;
        private String phone;
    }
    
    @Data
    public static class ResetPasswordRequest {
        private String newPassword;
    }
}

