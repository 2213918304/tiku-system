package com.springboot.tiku.service;

import com.springboot.tiku.dto.user.CreateStudyPlanRequest;
import com.springboot.tiku.dto.user.StudyPlanDTO;
import com.springboot.tiku.dto.user.UpdateProfileRequest;
import com.springboot.tiku.dto.user.UserProfileResponse;
import com.springboot.tiku.entity.StudyPlan;
import com.springboot.tiku.entity.User;
import com.springboot.tiku.exception.BusinessException;
import com.springboot.tiku.repository.StudyPlanRepository;
import com.springboot.tiku.repository.UserRepository;
import com.springboot.tiku.common.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StudyPlanRepository studyPlanRepository;

    /**
     * 获取用户个人资料
     */
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
        return UserProfileResponse.fromEntity(user);
    }

    /**
     * 更新用户个人资料
     */
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 更新允许修改的字段
        if (request.getRealName() != null && !request.getRealName().trim().isEmpty()) {
            user.setRealName(request.getRealName().trim());
        }
        
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            // 检查邮箱是否已被使用（排除自己）
            userRepository.findByEmail(request.getEmail())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(userId)) {
                            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "该邮箱已被使用");
                        }
                    });
            user.setEmail(request.getEmail().trim());
        }
        
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        userRepository.save(user);
        log.info("用户 {} 更新了个人资料", userId);
        
        return UserProfileResponse.fromEntity(user);
    }

    /**
     * 获取学习计划列表
     */
    public List<StudyPlanDTO> getStudyPlans(Long userId) {
        List<StudyPlan> plans = studyPlanRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return plans.stream()
                .map(StudyPlanDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 创建学习计划
     */
    @Transactional
    public StudyPlanDTO createStudyPlan(Long userId, CreateStudyPlanRequest request) {
        StudyPlan plan = new StudyPlan();
        plan.setUserId(userId);
        plan.setSubjectId(request.getSubjectId());
        plan.setName(request.getDescription());
        plan.setDescription(request.getDescription());
        
        // 计算每日目标
        if (request.getTargetCount() != null && request.getTotalDays() != null && request.getTotalDays() > 0) {
            plan.setDailyTarget(request.getTargetCount() / request.getTotalDays());
        } else {
            plan.setDailyTarget(10); // 默认每天10题
        }
        
        plan.setStartDate(LocalDate.now());
        
        // 设置结束日期
        if (request.getTotalDays() != null) {
            plan.setEndDate(LocalDate.now().plusDays(request.getTotalDays() - 1));
        } else {
            plan.setEndDate(LocalDate.now().plusDays(6)); // 默认7天
        }
        
        plan.setCompletedDays(0);
        plan.setStatus(StudyPlan.PlanStatus.ACTIVE);
        
        plan = studyPlanRepository.save(plan);
        log.info("用户 {} 创建了学习计划：{}", userId, plan.getName());
        
        return StudyPlanDTO.fromEntity(plan);
    }

    /**
     * 更新学习计划
     */
    @Transactional
    public StudyPlanDTO updateStudyPlan(Long userId, Long planId, CreateStudyPlanRequest request) {
        StudyPlan plan = studyPlanRepository.findById(planId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "学习计划不存在"));
        
        // 验证权限
        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限操作此学习计划");
        }
        
        if (request.getDescription() != null) {
            plan.setName(request.getDescription());
            plan.setDescription(request.getDescription());
        }
        
        plan = studyPlanRepository.save(plan);
        log.info("用户 {} 更新了学习计划 {}", userId, planId);
        
        return StudyPlanDTO.fromEntity(plan);
    }
}

