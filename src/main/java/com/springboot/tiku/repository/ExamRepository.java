package com.springboot.tiku.repository;

import com.springboot.tiku.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 考试Repository
 */
@Repository
public interface ExamRepository extends JpaRepository<Exam, Long>, JpaSpecificationExecutor<Exam> {
    
    /**
     * 根据学科ID查询考试
     */
    Page<Exam> findBySubjectIdAndStatusOrderByCreatedAtDesc(Long subjectId, Integer status, Pageable pageable);
    
    /**
     * 查询创建者的考试
     */
    List<Exam> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);
    
    /**
     * 根据类型查询考试
     */
    List<Exam> findByTypeAndStatusOrderByCreatedAtDesc(Exam.ExamType type, Integer status);
}




