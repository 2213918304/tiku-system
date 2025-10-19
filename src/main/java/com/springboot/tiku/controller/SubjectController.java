package com.springboot.tiku.controller;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.dto.subject.SubjectDTO;
import com.springboot.tiku.dto.subject.SubjectRequest;
import com.springboot.tiku.service.SubjectService;
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

import java.util.List;

/**
 * 学科控制器
 */
@Tag(name = "学科管理", description = "学科的增删改查接口")
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {
    
    private final SubjectService subjectService;
    
    /**
     * 创建学科（仅管理员）
     */
    @Operation(summary = "创建学科")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SubjectDTO> createSubject(@Valid @RequestBody SubjectRequest request) {
        return Result.success(subjectService.createSubject(request));
    }
    
    /**
     * 更新学科（仅管理员）
     */
    @Operation(summary = "更新学科")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SubjectDTO> updateSubject(@PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        return Result.success(subjectService.updateSubject(id, request));
    }
    
    /**
     * 删除学科（仅管理员）
     */
    @Operation(summary = "删除学科")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return Result.success();
    }
    
    /**
     * 获取学科详情
     */
    @Operation(summary = "获取学科详情")
    @GetMapping("/{id}")
    public Result<SubjectDTO> getSubject(@PathVariable Long id) {
        return Result.success(subjectService.getSubjectById(id));
    }
    
    /**
     * 获取所有启用的学科
     */
    @Operation(summary = "获取所有启用的学科")
    @GetMapping("/enabled")
    public Result<List<SubjectDTO>> getEnabledSubjects() {
        return Result.success(subjectService.getAllEnabledSubjects());
    }
    
    /**
     * 分页查询学科
     */
    @Operation(summary = "分页查询学科")
    @GetMapping
    public Result<Page<SubjectDTO>> getSubjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        return Result.success(subjectService.getSubjects(pageable));
    }
    
    /**
     * 更新学科状态（仅管理员）
     */
    @Operation(summary = "更新学科状态")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateSubjectStatus(@PathVariable Long id, @RequestParam Integer status) {
        subjectService.updateSubjectStatus(id, status);
        return Result.success();
    }
}



