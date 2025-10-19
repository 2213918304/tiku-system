package com.springboot.tiku.controller.admin;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.dto.answer.AnswerRecordDTO;
import com.springboot.tiku.repository.AnswerRecordRepository;
import com.springboot.tiku.service.AnswerRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员 - 答题记录管理控制器
 */
@Tag(name = "管理员-答题记录管理", description = "管理员查看所有答题记录")
@RestController
@RequestMapping("/api/admin/answer-records")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class AdminAnswerRecordController {
    
    private final AnswerRecordRepository answerRecordRepository;
    private final AnswerRecordService answerRecordService;
    
    /**
     * 分页查询答题记录
     */
    @Operation(summary = "分页查询答题记录")
    @GetMapping
    public Result<Page<AnswerRecordDTO>> listAnswerRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Boolean isCorrect
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "answeredAt"));
        Page<AnswerRecordDTO> records = answerRecordService.getAnswerRecords(userId, subjectId, isCorrect, pageable);
        return Result.success(records);
    }
    
    /**
     * 获取答题统计
     */
    @Operation(summary = "获取答题统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getAnswerStatistics(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总答题数
        long totalCount = answerRecordRepository.count();
        stats.put("totalCount", totalCount);
        
        // 正确数和正确率
        long correctCount = answerRecordRepository.countByIsCorrect(true);
        stats.put("correctCount", correctCount);
        stats.put("correctRate", totalCount > 0 ? (double) correctCount / totalCount * 100 : 0);
        
        // 今日答题数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayCount = answerRecordRepository.countByAnsweredAtAfter(todayStart);
        stats.put("todayCount", todayCount);
        
        // 本周答题数
        LocalDateTime weekStart = LocalDate.now().minusDays(6).atStartOfDay();
        long weekCount = answerRecordRepository.countByAnsweredAtAfter(weekStart);
        stats.put("weekCount", weekCount);
        
        return Result.success(stats);
    }
    
    /**
     * 删除答题记录
     */
    @Operation(summary = "删除答题记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteAnswerRecord(@PathVariable Long id) {
        answerRecordRepository.deleteById(id);
        return Result.success();
    }
    
    /**
     * 批量删除答题记录
     */
    @Operation(summary = "批量删除答题记录")
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchDeleteAnswerRecords(@RequestBody List<Long> ids) {
        answerRecordRepository.deleteAllById(ids);
        return Result.success();
    }
}

