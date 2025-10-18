package com.springboot.tiku.controller;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.dto.question.QuestionDTO;
import com.springboot.tiku.dto.question.QuestionQueryRequest;
import com.springboot.tiku.dto.question.QuestionRequest;
import com.springboot.tiku.entity.Question;
import com.springboot.tiku.service.QuestionService;
import com.springboot.tiku.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题目控制器
 */
@Tag(name = "题库管理", description = "题目的增删改查接口")
@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {
    
    private final QuestionService questionService;
    private final JwtUtil jwtUtil;
    
    /**
     * 创建题目（仅管理员和教师）
     */
    @Operation(summary = "创建题目")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<QuestionDTO> createQuestion(
            @Valid @RequestBody QuestionRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromRequest(httpRequest);
        return Result.success(questionService.createQuestion(request, userId));
    }
    
    /**
     * 更新题目（仅管理员和教师）
     */
    @Operation(summary = "更新题目")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<QuestionDTO> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request
    ) {
        return Result.success(questionService.updateQuestion(id, request));
    }
    
    /**
     * 删除题目（仅管理员）
     */
    @Operation(summary = "删除题目")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success();
    }
    
    /**
     * 批量删除题目（仅管理员）
     */
    @Operation(summary = "批量删除题目")
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchDeleteQuestions(@RequestBody List<Long> ids) {
        questionService.batchDeleteQuestions(ids);
        return Result.success();
    }
    
    /**
     * 获取题目详情
     */
    @Operation(summary = "获取题目详情")
    @GetMapping("/{id}")
    public Result<QuestionDTO> getQuestion(@PathVariable Long id) {
        return Result.success(questionService.getQuestionById(id));
    }
    
    /**
     * 分页查询题目
     */
    @Operation(summary = "分页查询题目")
    @PostMapping("/search")
    public Result<Page<QuestionDTO>> searchQuestions(
            @RequestBody QuestionQueryRequest queryRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "serialNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        // 创建排序规则：优先按serialNumber排序，如果serialNumber为null则按id排序
        Sort sort = Sort.by(sortDirection, sortBy).and(Sort.by(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(page, size, sort);
        return Result.success(questionService.getQuestions(queryRequest, pageable));
    }
    
    /**
     * 随机获取题目
     */
    @Operation(summary = "随机获取题目")
    @GetMapping("/random")
    public Result<List<QuestionDTO>> getRandomQuestions(
            @RequestParam Long subjectId,
            @RequestParam(required = false) Question.QuestionType type,
            @RequestParam(defaultValue = "10") Integer count
    ) {
        return Result.success(questionService.getRandomQuestions(subjectId, type, count));
    }
    
    /**
     * 更新题目状态（仅管理员）
     */
    @Operation(summary = "更新题目状态")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateQuestionStatus(@PathVariable Long id, @RequestParam Integer status) {
        questionService.updateQuestionStatus(id, status);
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
        return null;
    }
}

