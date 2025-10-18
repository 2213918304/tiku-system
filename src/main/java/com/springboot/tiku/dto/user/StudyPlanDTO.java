package com.springboot.tiku.dto.user;

import com.springboot.tiku.entity.StudyPlan;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudyPlanDTO {
    private Long id;
    private Long userId;
    private Long subjectId;
    private String subjectName;
    private String name;
    private String description;
    private Integer dailyTarget;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer completedDays;
    private Integer totalDays;
    private Integer targetCount;
    private Integer completedCount;
    private Integer progress;
    private String status;
    
    public static StudyPlanDTO fromEntity(StudyPlan plan) {
        StudyPlanDTO dto = new StudyPlanDTO();
        dto.setId(plan.getId());
        dto.setUserId(plan.getUserId());
        dto.setSubjectId(plan.getSubjectId());
        dto.setName(plan.getName());
        dto.setDescription(plan.getDescription());
        dto.setDailyTarget(plan.getDailyTarget());
        dto.setStartDate(plan.getStartDate());
        dto.setEndDate(plan.getEndDate());
        dto.setCompletedDays(plan.getCompletedDays());
        dto.setStatus(plan.getStatus().name());
        
        // 计算总天数
        if (plan.getStartDate() != null && plan.getEndDate() != null) {
            dto.setTotalDays((int) java.time.temporal.ChronoUnit.DAYS.between(plan.getStartDate(), plan.getEndDate()) + 1);
        }
        
        // 计算目标题数
        if (plan.getDailyTarget() != null && dto.getTotalDays() != null) {
            dto.setTargetCount(plan.getDailyTarget() * dto.getTotalDays());
        }
        
        // 计算完成题数（这里可以后续优化，从答题记录统计）
        dto.setCompletedCount(0);
        
        // 计算进度
        if (dto.getTargetCount() != null && dto.getTargetCount() > 0) {
            dto.setProgress((dto.getCompletedCount() * 100) / dto.getTargetCount());
        }
        
        return dto;
    }
}

