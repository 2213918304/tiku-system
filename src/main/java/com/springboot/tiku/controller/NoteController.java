package com.springboot.tiku.controller;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.dto.note.NoteDTO;
import com.springboot.tiku.entity.Note;
import com.springboot.tiku.service.NoteService;
import com.springboot.tiku.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记控制器
 */
@Tag(name = "笔记管理", description = "题目笔记功能")
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {
    
    private final NoteService noteService;
    private final JwtUtil jwtUtil;
    
    /**
     * 添加笔记
     */
    @Operation(summary = "添加笔记")
    @PostMapping
    public Result<Note> addNote(
            @RequestBody NoteRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromRequest(httpRequest);
        Note note = noteService.addNote(
            userId, 
            request.getQuestionId(), 
            request.getSubjectId(),
            request.getTitle(), 
            request.getContent()
        );
        return Result.success(note);
    }
    
    /**
     * 更新笔记
     */
    @Operation(summary = "更新笔记")
    @PutMapping("/{id}")
    public Result<Note> updateNote(
            @PathVariable Long id,
            @RequestBody NoteRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromRequest(httpRequest);
        Note note = noteService.updateNote(
            id, 
            userId, 
            request.getSubjectId(),
            request.getTitle(), 
            request.getContent()
        );
        return Result.success(note);
    }
    
    /**
     * 删除笔记
     */
    @Operation(summary = "删除笔记")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNote(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        noteService.deleteNote(id, userId);
        return Result.success();
    }
    
    /**
     * 获取我的笔记列表（分页）
     */
    @Operation(summary = "获取我的笔记列表（分页）")
    @GetMapping
    public Result<Page<NoteDTO>> getMyNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(noteService.getUserNotes(userId, subjectId, keyword, pageable));
    }
    
    /**
     * 获取我的所有笔记（不分页）
     */
    @Operation(summary = "获取我的所有笔记（不分页）")
    @GetMapping("/all")
    public Result<List<NoteDTO>> getAllMyNotes(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return Result.success(noteService.getAllUserNotes(userId));
    }
    
    /**
     * 获取某题的笔记
     */
    @Operation(summary = "获取某题的笔记")
    @GetMapping("/question/{questionId}")
    public Result<Note> getNoteByQuestion(
            @PathVariable Long questionId,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        return Result.success(noteService.getNoteByQuestion(userId, questionId));
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
    
    /**
     * 笔记请求DTO
     */
    @Data
    static class NoteRequest {
        private Long questionId;
        private Long subjectId;
        private String title;
        private String content;
    }
}



