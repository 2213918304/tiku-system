package com.springboot.tiku.controller;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.dto.statistics.RankingItem;
import com.springboot.tiku.service.RankingService;
import com.springboot.tiku.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 排行榜控制器
 */
@Tag(name = "排行榜", description = "各类排行榜功能")
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {
    
    private final RankingService rankingService;
    private final JwtUtil jwtUtil;
    
    /**
     * 答题数排行榜
     */
    @Operation(summary = "答题数排行榜", description = "根据答题总数排名")
    @GetMapping("/answer-count")
    public Result<List<RankingItem>> getAnswerCountRanking(
            @RequestParam(defaultValue = "100") Integer limit,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        List<RankingItem> ranking = rankingService.getAnswerCountRanking(limit, userId);
        return Result.success(ranking);
    }
    
    /**
     * 正确率排行榜
     */
    @Operation(summary = "正确率排行榜", description = "根据答题正确率排名（至少答题10道）")
    @GetMapping("/accuracy")
    public Result<List<RankingItem>> getAccuracyRanking(
            @RequestParam(defaultValue = "100") Integer limit,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        List<RankingItem> ranking = rankingService.getAccuracyRanking(limit, userId);
        return Result.success(ranking);
    }
    
    /**
     * 积分排行榜
     */
    @Operation(summary = "积分排行榜", description = "根据学习积分排名")
    @GetMapping("/points")
    public Result<List<RankingItem>> getPointsRanking(
            @RequestParam(defaultValue = "100") Integer limit,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        List<RankingItem> ranking = rankingService.getPointsRanking(limit, userId);
        return Result.success(ranking);
    }
    
    /**
     * 学科排行榜
     */
    @Operation(summary = "学科排行榜", description = "指定学科的答题数排名")
    @GetMapping("/subject/{subjectId}")
    public Result<List<RankingItem>> getSubjectRanking(
            @PathVariable Long subjectId,
            @RequestParam(defaultValue = "100") Integer limit,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        List<RankingItem> ranking = rankingService.getSubjectRanking(subjectId, limit, userId);
        return Result.success(ranking);
    }
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("未找到用户信息");
    }
}



