package com.springboot.tiku.repository;

import com.springboot.tiku.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 收藏Repository
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long>, JpaSpecificationExecutor<Favorite> {
    
    /**
     * 查询用户的所有收藏
     */
    Page<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 根据分类查询收藏
     */
    List<Favorite> findByUserIdAndCategoryOrderByCreatedAtDesc(Long userId, String category);
    
    /**
     * 检查是否已收藏
     */
    boolean existsByUserIdAndQuestionId(Long userId, Long questionId);
    
    /**
     * 查找特定收藏
     */
    Optional<Favorite> findByUserIdAndQuestionId(Long userId, Long questionId);
    
    /**
     * 删除收藏
     */
    void deleteByUserIdAndQuestionId(Long userId, Long questionId);
    
    /**
     * 统计用户收藏数
     */
    long countByUserId(Long userId);
    
    /**
     * 批量查询用户的收藏
     */
    List<Favorite> findByUserIdAndQuestionIdIn(Long userId, List<Long> questionIds);
    
    /**
     * 查询用户所有收藏（不分页）
     */
    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 统计指定时间后的收藏数
     */
    Long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime dateTime);
    
    /**
     * 获取用户收藏的所有题目ID
     */
    @Query("SELECT f.questionId FROM Favorite f WHERE f.userId = :userId")
    List<Long> findQuestionIdsByUserId(@Param("userId") Long userId);
}


