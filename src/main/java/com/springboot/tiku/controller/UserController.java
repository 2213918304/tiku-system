package com.springboot.tiku.controller;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.common.ResultCode;
import com.springboot.tiku.dto.user.CreateStudyPlanRequest;
import com.springboot.tiku.dto.user.StudyPlanDTO;
import com.springboot.tiku.dto.user.UpdateProfileRequest;
import com.springboot.tiku.dto.user.UserProfileResponse;
import com.springboot.tiku.entity.User;
import com.springboot.tiku.exception.BusinessException;
import com.springboot.tiku.repository.UserRepository;
import com.springboot.tiku.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    
    /**
     * 从 Authentication 中获取用户ID
     */
    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
        return user.getId();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<UserProfileResponse> getProfile(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(userService.getProfile(userId));
    }

    /**
     * 更新个人资料
     */
    @PutMapping("/profile")
    public Result<UserProfileResponse> updateProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request) {
        Long userId = getUserId(authentication);
        return Result.success(userService.updateProfile(userId, request));
    }

    /**
     * 获取学习计划列表
     */
    @GetMapping("/study-plans")
    public Result<List<StudyPlanDTO>> getStudyPlans(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(userService.getStudyPlans(userId));
    }

    /**
     * 创建学习计划
     */
    @PostMapping("/study-plans")
    public Result<StudyPlanDTO> createStudyPlan(
            Authentication authentication,
            @RequestBody CreateStudyPlanRequest request) {
        Long userId = getUserId(authentication);
        return Result.success(userService.createStudyPlan(userId, request));
    }

    /**
     * 更新学习计划
     */
    @PutMapping("/study-plans/{id}")
    public Result<StudyPlanDTO> updateStudyPlan(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody CreateStudyPlanRequest request) {
        Long userId = getUserId(authentication);
        return Result.success(userService.updateStudyPlan(userId, id, request));
    }
}

