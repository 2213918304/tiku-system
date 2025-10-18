package com.springboot.tiku.repository;

import com.springboot.tiku.entity.ExamRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 考试记录Repository
 */
@Repository
public interface ExamRecordRepository extends JpaRepository<ExamRecord, Long>, JpaSpecificationExecutor<ExamRecord> {
    
    /**
     * 查询用户的考试记录
     */
    Page<ExamRecord> findByUserIdOrderByStartTimeDesc(Long userId, Pageable pageable);
    
    /**
     * 查询用户某次考试的记录
     */
    Optional<ExamRecord> findByUserIdAndExamIdAndStatus(Long userId, Long examId, ExamRecord.ExamStatus status);
    
    /**
     * 查询考试的所有记录
     */
    List<ExamRecord> findByExamIdOrderByScoreDesc(Long examId);
    
    /**
     * 统计考试参与人数
     */
    long countByExamId(Long examId);
    
    /**
     * 查询考试的平均分
     */
    @Query("SELECT AVG(e.score) FROM ExamRecord e WHERE e.examId = :examId AND e.status = 'SUBMITTED'")
    Double findAvgScoreByExamId(@Param("examId") Long examId);
}




