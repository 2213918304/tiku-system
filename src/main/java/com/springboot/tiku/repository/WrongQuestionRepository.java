package com.springboot.tiku.repository;

import com.springboot.tiku.entity.WrongQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 错题本Repository
 */
@Repository
public interface WrongQuestionRepository extends JpaRepository<WrongQuestion, Long>, JpaSpecificationExecutor<WrongQuestion> {
    
    /**
     * 查询用户的错题
     */
    Page<WrongQuestion> findByUserIdAndRemovedOrderByCreatedAtDesc(Long userId, Boolean removed, Pageable pageable);
    
    /**
     * 根据状态查询错题
     */
    List<WrongQuestion> findByUserIdAndStatusAndRemovedOrderByWrongCountDesc(Long userId, WrongQuestion.WrongStatus status, Boolean removed);
    
    /**
     * 查找特定错题
     */
    Optional<WrongQuestion> findByUserIdAndQuestionId(Long userId, Long questionId);
    
    /**
     * 检查是否在错题本中
     */
    boolean existsByUserIdAndQuestionIdAndRemoved(Long userId, Long questionId, Boolean removed);
    
    /**
     * 统计用户错题数
     */
    long countByUserIdAndRemoved(Long userId, Boolean removed);
}




