package com.springboot.tiku.controller;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.dto.wrong.WrongQuestionDTO;
import com.springboot.tiku.dto.wrong.WrongQuestionStatsDTO;
import com.springboot.tiku.service.WrongQuestionService;
import com.springboot.tiku.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 错题本控制器
 */
@Tag(name = "错题本", description = "错题本管理功能")
@RestController
@RequestMapping("/wrong-questions")
@RequiredArgsConstructor
public class WrongQuestionController {
    
    private final WrongQuestionService wrongQuestionService;
    private final JwtUtil jwtUtil;
    
    /**
     * 获取我的错题列表（分页）
     */
    @Operation(summary = "获取我的错题列表（分页）")
    @GetMapping
    public Result<Page<WrongQuestionDTO>> getMyWrongQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return Result.success(wrongQuestionService.getUserWrongQuestions(
                userId, subjectId, chapterId, type, status, pageable
        ));
    }
    
    /**
     * 获取我的所有错题（不分页）
     */
    @Operation(summary = "获取我的所有错题（不分页）")
    @GetMapping("/all")
    public Result<List<WrongQuestionDTO>> getAllMyWrongQuestions(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return Result.success(wrongQuestionService.getAllUserWrongQuestions(userId));
    }
    
    /**
     * 获取错题统计
     */
    @Operation(summary = "获取错题统计")
    @GetMapping("/stats")
    public Result<WrongQuestionStatsDTO> getWrongQuestionStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return Result.success(wrongQuestionService.getWrongQuestionStats(userId));
    }
    
    /**
     * 标记为已掌握
     */
    @Operation(summary = "标记为已掌握")
    @PutMapping("/{questionId}/mastered")
    public Result<Void> markAsMastered(
            @PathVariable Long questionId,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        wrongQuestionService.markAsMastered(userId, questionId);
        return Result.success();
    }
    
    /**
     * 从错题本移除
     */
    @Operation(summary = "从错题本移除")
    @DeleteMapping("/{questionId}")
    public Result<Void> removeWrongQuestion(
            @PathVariable Long questionId,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        wrongQuestionService.removeFromWrongBook(userId, questionId);
        return Result.success();
    }
    
    /**
     * 批量移除
     */
    @Operation(summary = "批量移除")
    @DeleteMapping("/batch")
    public Result<Void> batchRemove(
            @RequestBody List<Long> questionIds,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        wrongQuestionService.batchRemove(userId, questionIds);
        return Result.success();
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



