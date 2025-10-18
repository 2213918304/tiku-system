package com.springboot.tiku.repository;

import com.springboot.tiku.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 成就Repository
 */
@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long>, JpaSpecificationExecutor<Achievement> {
    
    /**
     * 根据代码查询成就
     */
    Optional<Achievement> findByCode(String code);
    
    /**
     * 查询所有成就，按排序
     */
    List<Achievement> findAllByOrderBySortOrderAsc();
    
    /**
     * 根据类型查询成就
     */
    List<Achievement> findByTypeOrderBySortOrderAsc(Achievement.AchievementType type);
}




