package com.springboot.tiku.controller;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.dto.practice.PracticeMode;
import com.springboot.tiku.dto.practice.PracticeRequest;
import com.springboot.tiku.dto.practice.PracticeSession;
import com.springboot.tiku.service.PracticeService;
import com.springboot.tiku.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 刷题控制器
 * 支持9种刷题模式
 */
@Tag(name = "刷题模式", description = "9种刷题模式接口")
@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
public class PracticeController {
    
    private final PracticeService practiceService;
    private final JwtUtil jwtUtil;
    
    /**
     * 获取所有刷题模式
     */
    @Operation(summary = "获取所有刷题模式", description = "返回9种刷题模式的列表和说明")
    @GetMapping("/modes")
    public Result<List<Map<String, String>>> getPracticeModes() {
        List<Map<String, String>> modes = Arrays.stream(PracticeMode.values())
                .map(mode -> Map.of(
                        "code", mode.name(),
                        "name", mode.getName(),
                        "description", mode.getDescription()
                ))
                .collect(Collectors.toList());
        return Result.success(modes);
    }
    
    /**
     * 开始刷题（通用接口，支持所有模式）
     */
    @Operation(summary = "开始刷题", description = "根据指定模式开始刷题，返回题目列表")
    @PostMapping("/start")
    public Result<PracticeSession> startPractice(
            @Valid @RequestBody PracticeRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromRequest(httpRequest);
        PracticeSession session = practiceService.startPractice(request, userId);
        return Result.success(session);
    }
    
    /**
     * 1. 顺序刷题
     */
    @Operation(summary = "顺序刷题", description = "按题目顺序依次练习")
    @PostMapping("/sequential")
    public Result<PracticeSession> sequentialPractice(
            @RequestBody PracticeRequest request,
            HttpServletRequest httpRequest
    ) {
        request.setMode(PracticeMode.SEQUENTIAL);
        return startPractice(request, httpRequest);
    }
    
    /**
     * 2. 随机刷题
     */
    @Operation(summary = "随机刷题", description = "随机抽取题目练习")
    @PostMapping("/random")
    public Result<PracticeSession> randomPractice(
            @RequestBody PracticeRequest request,
            HttpServletRequest httpRequest
    ) {
        request.setMode(PracticeMode.RANDOM);
        return startPractice(request, httpRequest);
    }
    
    /**
     * 3. 章节练习
     */
    @Operation(summary = "章节练习", description = "针对特定章节进行练习")
    @PostMapping("/chapter")
    public Result<PracticeSession> chapterPractice(
            @RequestBody PracticeRequest request,
            HttpServletRequest httpRequest
    ) {
        request.setMode(PracticeMode.CHAPTER);
        return startPractice(request, httpRequest);
    }
    
    /**
     * 4. 考试模拟
     */
    @Operation(summary = "考试模拟", description = "模拟真实考试，限时答题")
    @PostMapping("/exam")
    public Result<PracticeSession> examPractice(
            @RequestBody PracticeRequest request,
            HttpServletRequest httpRequest
    ) {
        request.setMode(PracticeMode.EXAM);
        return startPractice(request, httpRequest);
    }
    
    /**
     * 5. 错题强化
     */
    @Operation(summary = "错题强化", description = "专门练习错题")
    @PostMapping("/wrong-questions")
    public Result<PracticeSession> wrongQuestionPractice(
            @RequestBody PracticeRequest request,
            HttpServletRequest httpRequest
    ) {
        request.setMode(PracticeMode.WRONG_QUESTION);
        return startPractice(request, httpRequest);
    }
    
    /**
     * 6. 收藏专练
     */
    @Operation(summary = "收藏专练", description = "练习收藏的题目")
    @PostMapping("/favorites")
    public Result<PracticeSession> favoritePractice(
            @RequestBody PracticeRequest request,
            HttpServletRequest httpRequest
    ) {
        request.setMode(PracticeMode.FAVORITE);
        return startPractice(request, httpRequest);
    }
    
    /**
     * 7. 闯关模式
     */
    @Operation(summary = "闯关模式", description = "游戏化闯关练习")
    @PostMapping("/challenge")
    public Result<PracticeSession> challengePractice(
            @RequestBody PracticeRequest request,
            HttpServletRequest httpRequest
    ) {
        request.setMode(PracticeMode.CHALLENGE);
        return startPractice(request, httpRequest);
    }
    
    /**
     * 8. 限时挑战
     */
    @Operation(summary = "限时挑战", description = "每题限时，快速答题")
    @PostMapping("/timed")
    public Result<PracticeSession> timedPractice(
            @RequestBody PracticeRequest request,
            HttpServletRequest httpRequest
    ) {
        request.setMode(PracticeMode.TIMED);
        return startPractice(request, httpRequest);
    }
    
    /**
     * 9. 智能推荐
     */
    @Operation(summary = "智能推荐", description = "根据学习数据智能推荐题目")
    @PostMapping("/smart-recommend")
    public Result<PracticeSession> smartRecommendPractice(
            @RequestBody PracticeRequest request,
            HttpServletRequest httpRequest
    ) {
        request.setMode(PracticeMode.SMART_RECOMMEND);
        return startPractice(request, httpRequest);
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



