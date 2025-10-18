package com.springboot.tiku.repository;

import com.springboot.tiku.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户成就Repository
 */
@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long>, JpaSpecificationExecutor<UserAchievement> {
    
    /**
     * 查询用户的所有成就
     */
    List<UserAchievement> findByUserIdOrderByAchievedAtDesc(Long userId);
    
    /**
     * 检查用户是否已获得某成就
     */
    boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);
    
    /**
     * 统计用户成就数（已解锁的成就）
     */
    long countByUserId(Long userId);
}


