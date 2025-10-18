package com.springboot.tiku.controller;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.dto.grading.GradingResult;
import com.springboot.tiku.dto.grading.SubmitAnswerRequest;
import com.springboot.tiku.service.GradingService;
import com.springboot.tiku.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 判题控制器
 */
@Tag(name = "判题管理", description = "提交答案和判题接口")
@RestController
@RequestMapping("/grading")
@RequiredArgsConstructor
public class GradingController {
    
    private final GradingService gradingService;
    private final JwtUtil jwtUtil;
    
    /**
     * 提交答案并判题
     */
    @Operation(summary = "提交答案并判题", description = "支持客观题自动判题和主观题AI判题")
    @PostMapping("/submit")
    public Result<GradingResult> submitAnswer(
            @Valid @RequestBody SubmitAnswerRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromRequest(httpRequest);
        GradingResult result = gradingService.submitAndGrade(request, userId);
        return Result.success(result);
    }
    
    /**
     * 批量提交答案（考试场景）
     */
    @Operation(summary = "批量提交答案", description = "用于考试结束时一次性提交所有答案")
    @PostMapping("/submit/batch")
    public Result<List<GradingResult>> batchSubmitAnswers(
            @Valid @RequestBody List<SubmitAnswerRequest> requests,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromRequest(httpRequest);
        List<GradingResult> results = gradingService.batchSubmitAndGrade(requests, userId);
        return Result.success(results);
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



