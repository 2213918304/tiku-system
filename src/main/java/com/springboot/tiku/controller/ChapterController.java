package com.springboot.tiku.controller;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.dto.chapter.ChapterDTO;
import com.springboot.tiku.dto.chapter.ChapterRequest;
import com.springboot.tiku.service.ChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 章节控制器
 */
@Tag(name = "章节管理", description = "章节的增删改查接口")
@RestController
@RequestMapping("/chapters")
@RequiredArgsConstructor
public class ChapterController {
    
    private final ChapterService chapterService;
    
    /**
     * 创建章节（仅管理员和教师）
     */
    @Operation(summary = "创建章节")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<ChapterDTO> createChapter(@Valid @RequestBody ChapterRequest request) {
        return Result.success(chapterService.createChapter(request));
    }
    
    /**
     * 更新章节（仅管理员和教师）
     */
    @Operation(summary = "更新章节")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<ChapterDTO> updateChapter(@PathVariable Long id, @Valid @RequestBody ChapterRequest request) {
        return Result.success(chapterService.updateChapter(id, request));
    }
    
    /**
     * 删除章节（仅管理员）
     */
    @Operation(summary = "删除章节")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteChapter(@PathVariable Long id) {
        chapterService.deleteChapter(id);
        return Result.success();
    }
    
    /**
     * 获取章节详情
     */
    @Operation(summary = "获取章节详情")
    @GetMapping("/{id}")
    public Result<ChapterDTO> getChapter(@PathVariable Long id) {
        return Result.success(chapterService.getChapterById(id));
    }
    
    /**
     * 获取学科的所有章节（平铺列表）
     */
    @Operation(summary = "获取学科的所有章节")
    @GetMapping("/subject/{subjectId}")
    public Result<List<ChapterDTO>> getChaptersBySubject(@PathVariable Long subjectId) {
        return Result.success(chapterService.getChaptersBySubjectId(subjectId));
    }
    
    /**
     * 获取学科的章节树
     */
    @Operation(summary = "获取学科的章节树")
    @GetMapping("/subject/{subjectId}/tree")
    public Result<List<ChapterDTO>> getChapterTree(@PathVariable Long subjectId) {
        return Result.success(chapterService.getChapterTree(subjectId));
    }
}



