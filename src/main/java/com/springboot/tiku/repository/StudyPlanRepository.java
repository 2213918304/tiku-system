package com.springboot.tiku.repository;

import com.springboot.tiku.entity.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 学习计划Repository
 */
@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long>, JpaSpecificationExecutor<StudyPlan> {
    
    /**
     * 查询用户的所有学习计划
     */
    List<StudyPlan> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 查询用户进行中的学习计划
     */
    List<StudyPlan> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, StudyPlan.PlanStatus status);
    
    /**
     * 查询用户某学科的学习计划
     */
    List<StudyPlan> findByUserIdAndSubjectIdOrderByCreatedAtDesc(Long userId, Long subjectId);
}




