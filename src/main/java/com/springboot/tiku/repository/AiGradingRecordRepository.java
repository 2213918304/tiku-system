package com.springboot.tiku.repository;

import com.springboot.tiku.entity.AiGradingRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI判题记录Repository
 */
@Repository
public interface AiGradingRecordRepository extends JpaRepository<AiGradingRecord, Long>, JpaSpecificationExecutor<AiGradingRecord> {
    
    /**
     * 根据答题记录ID查询
     */
    Optional<AiGradingRecord> findByAnswerRecordId(Long answerRecordId);
    
    /**
     * 查询用户的AI判题记录
     */
    List<AiGradingRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 查询待人工复核的记录
     */
    List<AiGradingRecord> findByManualReviewOrderByCreatedAtDesc(Boolean manualReview);
    
    /**
     * 根据置信度查询（分页）
     */
    Page<AiGradingRecord> findByAiConfidenceLessThan(java.math.BigDecimal aiConfidence, Pageable pageable);
    
    /**
     * 统计低置信度记录数
     */
    long countByAiConfidenceLessThan(java.math.BigDecimal aiConfidence);
    
    /**
     * 统计高置信度记录数
     */
    long countByAiConfidenceGreaterThanEqual(java.math.BigDecimal aiConfidence);
    
    /**
     * 计算平均置信度
     */
    @Query("SELECT AVG(a.aiConfidence) FROM AiGradingRecord a")
    java.math.BigDecimal findAverageConfidence();
}



