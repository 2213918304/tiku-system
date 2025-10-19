package com.springboot.tiku.controller.admin;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.dto.answer.AIGradingRecordDTO;
import com.springboot.tiku.service.AIGradingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 管理员 - AI判题审核控制器
 */
@Tag(name = "管理员-AI判题审核", description = "审核AI判题结果")
@RestController
@RequestMapping("/api/admin/ai-grading")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class AdminAIGradingController {
    
    private final AIGradingService aiGradingService;
    
    /**
     * 分页查询AI判题记录
     */
    @Operation(summary = "分页查询AI判题记录")
    @GetMapping
    public Result<Page<AIGradingRecordDTO>> listAIGradingRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AIGradingRecordDTO> records = aiGradingService.getAIGradingRecords(status, pageable);
        return Result.success(records);
    }
    
    /**
     * 获取AI判题统计
     */
    @Operation(summary = "获取AI判题统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getAIGradingStatistics() {
        Map<String, Object> stats = aiGradingService.getStatistics();
        return Result.success(stats);
    }
    
    /**
     * 审核AI判题结果（修改分数）
     */
    @Operation(summary = "审核AI判题结果")
    @PutMapping("/{id}/review")
    public Result<Void> reviewAIGrading(
            @PathVariable Long id,
            @RequestBody ReviewRequest request
    ) {
        aiGradingService.reviewAIGrading(id, BigDecimal.valueOf(request.getScore()), request.getComment());
        return Result.success();
    }
    
    /**
     * 批量审核（通过）
     */
    @Operation(summary = "批量审核通过")
    @PostMapping("/batch-approve")
    public Result<Void> batchApprove(@RequestBody List<Long> ids) {
        aiGradingService.batchApprove(ids);
        return Result.success();
    }
    
    /**
     * 删除AI判题记录
     */
    @Operation(summary = "删除AI判题记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteAIGradingRecord(@PathVariable Long id) {
        aiGradingService.deleteRecord(id);
        return Result.success();
    }
    
    @Data
    public static class ReviewRequest {
        private Integer score;
        private String comment;
    }
}

