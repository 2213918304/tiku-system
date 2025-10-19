package com.springboot.tiku.controller;

import com.springboot.tiku.common.Result;
import com.springboot.tiku.dto.favorite.FavoriteDTO;
import com.springboot.tiku.dto.favorite.FavoriteStatsDTO;
import com.springboot.tiku.entity.Favorite;
import com.springboot.tiku.service.FavoriteService;
import com.springboot.tiku.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 收藏控制器
 */
@Tag(name = "收藏管理", description = "题目收藏功能")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    
    private final FavoriteService favoriteService;
    private final JwtUtil jwtUtil;
    
    /**
     * 收藏题目
     */
    @Operation(summary = "收藏题目")
    @PostMapping
    public Result<Favorite> addFavorite(
            @RequestBody Map<String, Object> params,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        Long questionId = Long.valueOf(params.get("questionId").toString());
        String category = params.containsKey("category") ? params.get("category").toString() : null;
        String remark = params.containsKey("remark") ? params.get("remark").toString() : null;
        
        Favorite favorite = favoriteService.addFavorite(userId, questionId, category, remark);
        
        return Result.success(favorite);
    }
    
    /**
     * 取消收藏
     */
    @Operation(summary = "取消收藏")
    @DeleteMapping("/{questionId}")
    public Result<Void> removeFavorite(
            @PathVariable Long questionId,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        favoriteService.removeFavorite(userId, questionId);
        return Result.success();
    }
    
    /**
     * 获取我的收藏列表（分页）
     */
    @Operation(summary = "获取我的收藏列表（分页）")
    @GetMapping
    public Result<Page<FavoriteDTO>> getMyFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        Pageable pageable = PageRequest.of(page, size);
        return Result.success(favoriteService.getUserFavorites(userId, pageable));
    }
    
    /**
     * 获取我的所有收藏（不分页）
     */
    @Operation(summary = "获取我的所有收藏（不分页）")
    @GetMapping("/all")
    public Result<List<FavoriteDTO>> getAllMyFavorites(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return Result.success(favoriteService.getAllUserFavorites(userId));
    }
    
    /**
     * 获取收藏统计信息
     */
    @Operation(summary = "获取收藏统计信息")
    @GetMapping("/stats")
    public Result<FavoriteStatsDTO> getFavoriteStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return Result.success(favoriteService.getFavoriteStats(userId));
    }
    
    /**
     * 根据分类获取收藏
     */
    @Operation(summary = "根据分类获取收藏")
    @GetMapping("/category/{category}")
    public Result<List<Favorite>> getFavoritesByCategory(
            @PathVariable String category,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        return Result.success(favoriteService.getFavoritesByCategory(userId, category));
    }
    
    /**
     * 检查是否已收藏
     */
    @Operation(summary = "检查是否已收藏")
    @GetMapping("/check/{questionId}")
    public Result<Boolean> isFavorite(
            @PathVariable Long questionId,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        return Result.success(favoriteService.isFavorite(userId, questionId));
    }
    
    /**
     * 批量检查收藏状态
     */
    @Operation(summary = "批量检查收藏状态")
    @PostMapping("/check/batch")
    public Result<Map<Long, Favorite>> checkBatchFavorites(
            @RequestBody Map<String, List<Long>> params,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        List<Long> questionIds = params.get("questionIds");
        return Result.success(favoriteService.checkBatchFavorites(userId, questionIds));
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



