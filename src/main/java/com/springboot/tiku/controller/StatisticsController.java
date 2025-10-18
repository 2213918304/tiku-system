package com.springboot.tiku.controller;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.common.ResultCode;
import com.springboot.tiku.dto.statistics.*;
import com.springboot.tiku.entity.User;
import com.springboot.tiku.exception.BusinessException;
import com.springboot.tiku.repository.UserRepository;
import com.springboot.tiku.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统计分析控制器
 */
@Tag(name = "统计分析", description = "用户学习数据统计分析")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    
    private final StatisticsService statisticsService;
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
     * 获取我的学习统计
     */
    @Operation(summary = "获取我的学习统计", description = "获取当前用户的整体学习数据统计")
    @GetMapping("/my")
    public Result<UserStatistics> getMyStatistics(Authentication authentication) {
        Long userId = getUserId(authentication);
        UserStatistics statistics = statisticsService.getUserStatistics(userId);
        return Result.success(statistics);
    }
    
    /**
     * 获取学科学习统计
     */
    @Operation(summary = "获取学科学习统计", description = "获取各学科的学习情况统计")
    @GetMapping("/subjects")
    public Result<List<SubjectStatistics>> getSubjectStatistics(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<SubjectStatistics> statistics = statisticsService.getSubjectStatistics(userId);
        return Result.success(statistics);
    }
    
    /**
     * 获取章节学习统计
     */
    @Operation(summary = "获取章节学习统计", description = "获取指定学科各章节的学习情况")
    @GetMapping("/chapters/{subjectId}")
    public Result<List<ChapterStatistics>> getChapterStatistics(
            @PathVariable Long subjectId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        List<ChapterStatistics> statistics = statisticsService.getChapterStatistics(userId, subjectId);
        return Result.success(statistics);
    }
    
    /**
     * 获取学习日历
     */
    @Operation(summary = "获取学习日历", description = "获取指定月份的学习日历和打卡数据")
    @GetMapping("/calendar")
    public Result<StudyCalendar> getStudyCalendar(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        StudyCalendar calendar = statisticsService.getStudyCalendar(userId, year, month);
        return Result.success(calendar);
    }
    
    /**
     * 每日打卡
     */
    @Operation(summary = "每日打卡", description = "完成今日学习打卡")
    @PostMapping("/check-in")
    public Result<String> checkIn(Authentication authentication) {
        Long userId = getUserId(authentication);
        statisticsService.checkIn(userId);
        return Result.success("打卡成功！");
    }
    
    /**
     * 获取学习趋势
     */
    @Operation(summary = "获取学习趋势", description = "获取最近N天的学习趋势数据")
    @GetMapping("/trend")
    public Result<StudyTrendDTO> getStudyTrend(
            @RequestParam(defaultValue = "30") int days,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        StudyTrendDTO trend = statisticsService.getStudyTrend(userId, days);
        return Result.success(trend);
    }
    
    /**
     * 获取题型统计
     */
    @Operation(summary = "获取题型统计", description = "获取各题型的答题统计")
    @GetMapping("/question-types")
    public Result<List<QuestionTypeStatDTO>> getQuestionTypeStatistics(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<QuestionTypeStatDTO> statistics = statisticsService.getQuestionTypeStatistics(userId);
        return Result.success(statistics);
    }
}

