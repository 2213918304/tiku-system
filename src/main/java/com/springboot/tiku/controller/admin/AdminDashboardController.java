package com.springboot.tiku.controller.admin;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员 - Dashboard统计控制器
 */
@Tag(name = "管理员-Dashboard统计", description = "管理后台仪表盘数据")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class AdminDashboardController {
    
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRecordRepository answerRecordRepository;
    private final SubjectRepository subjectRepository;
    
    /**
     * 获取Dashboard统计数据
     */
    @Operation(summary = "获取Dashboard统计数据")
    @GetMapping("/statistics")
    public Result<DashboardStatistics> getStatistics() {
        DashboardStatistics stats = new DashboardStatistics();
        
        // 用户统计
        stats.setTotalUsers(userRepository.count());
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        stats.setNewUsersToday(userRepository.countByCreatedAtAfter(todayStart));
        
        // 题目统计
        stats.setTotalQuestions(questionRepository.count());
        stats.setNewQuestionsToday(questionRepository.countByCreatedAtAfter(todayStart));
        
        // 答题统计
        stats.setTotalAnswers(answerRecordRepository.count());
        stats.setTodayAnswers(answerRecordRepository.countByAnsweredAtAfter(todayStart));
        
        // 活跃用户统计（7天内有答题记录的用户）
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        stats.setActiveUsers(answerRecordRepository.countDistinctUsersByAnsweredAtAfter(weekAgo));
        
        // 在线用户（这里简化处理，实际应该用Redis或Session管理）
        stats.setOnlineUsers((int) (stats.getActiveUsers() * 0.1)); // 估算为活跃用户的10%
        
        return Result.success(stats);
    }
    
    /**
     * 获取答题趋势数据
     */
    @Operation(summary = "获取答题趋势数据")
    @GetMapping("/trends")
    public Result<TrendData> getTrends(@RequestParam(defaultValue = "week") String type) {
        TrendData data = new TrendData();
        
        int days = "week".equals(type) ? 7 : 30;
        List<String> labels = new ArrayList<>();
        List<Integer> answerCounts = new ArrayList<>();
        List<Integer> userCounts = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            
            // 日期标签
            if ("week".equals(type)) {
                labels.add(getDayOfWeekName(date.getDayOfWeek().getValue()));
            } else {
                labels.add(date.getDayOfMonth() + "日");
            }
            
            // 答题数
            long answerCount = answerRecordRepository.countByAnsweredAtBetween(dayStart, dayEnd);
            answerCounts.add((int) answerCount);
            
            // 活跃用户数（当天答题的用户数）
            long userCount = answerRecordRepository.countDistinctUsersByAnsweredAtBetween(dayStart, dayEnd);
            userCounts.add((int) userCount);
        }
        
        data.setLabels(labels);
        data.setAnswerCounts(answerCounts);
        data.setUserCounts(userCounts);
        
        return Result.success(data);
    }
    
    /**
     * 获取学科分布数据
     */
    @Operation(summary = "获取学科分布数据")
    @GetMapping("/subject-distribution")
    public Result<List<SubjectDistribution>> getSubjectDistribution() {
        List<SubjectDistribution> distributions = subjectRepository.findAll().stream()
                .map(subject -> {
                    SubjectDistribution dist = new SubjectDistribution();
                    dist.setName(subject.getName());
                    dist.setValue((int) questionRepository.countBySubjectId(subject.getId()));
                    return dist;
                })
                .filter(dist -> dist.getValue() > 0)
                .collect(Collectors.toList());
        
        return Result.success(distributions);
    }
    
    /**
     * 获取最近活动
     */
    @Operation(summary = "获取最近活动")
    @GetMapping("/recent-activities")
    public Result<List<ActivityRecord>> getRecentActivities() {
        // 这里简化实现，实际应该有专门的活动日志表
        List<ActivityRecord> activities = new ArrayList<>();
        
        // 获取最近的答题记录
        // 由于没有专门的活动日志，这里返回空列表或简单的活动
        // 实际生产环境应该有ActivityLog表记录用户操作
        
        return Result.success(activities);
    }
    
    /**
     * 获取系统状态
     */
    @Operation(summary = "获取系统状态")
    @GetMapping("/system-status")
    public Result<SystemStatus> getSystemStatus() {
        SystemStatus status = new SystemStatus();
        
        // 这里简化处理，实际应该通过JMX或其他方式获取真实系统状态
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        status.setCpu(45); // 简化处理
        status.setMemory((int) ((usedMemory * 100) / totalMemory));
        status.setDisk(60); // 简化处理
        status.setDbConnections(10); // 简化处理
        
        return Result.success(status);
    }
    
    // 辅助方法
    private String getDayOfWeekName(int dayOfWeek) {
        String[] names = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return names[dayOfWeek];
    }
    
    // DTO类
    @Data
    public static class DashboardStatistics {
        private Long totalUsers;
        private Long newUsersToday;
        private Long totalQuestions;
        private Long newQuestionsToday;
        private Long todayAnswers;
        private Long totalAnswers;
        private Long activeUsers;
        private Integer onlineUsers;
    }
    
    @Data
    public static class TrendData {
        private List<String> labels;
        private List<Integer> answerCounts;
        private List<Integer> userCounts;
    }
    
    @Data
    public static class SubjectDistribution {
        private String name;
        private Integer value;
    }
    
    @Data
    public static class ActivityRecord {
        private Long id;
        private String user;
        private String action;
        private String detail;
        private String time;
        private String color;
    }
    
    @Data
    public static class SystemStatus {
        private Integer cpu;
        private Integer memory;
        private Integer disk;
        private Integer dbConnections;
    }
}





