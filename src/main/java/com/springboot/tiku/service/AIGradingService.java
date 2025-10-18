package com.springboot.tiku.service;

import com.springboot.tiku.dto.answer.AIGradingRecordDTO;
import com.springboot.tiku.entity.AiGradingRecord;
import com.springboot.tiku.repository.AiGradingRecordRepository;
import com.springboot.tiku.repository.QuestionRepository;
import com.springboot.tiku.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

/**
 * AI判题服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIGradingService {
    
    private final AiGradingRecordRepository aiGradingRecordRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    
    /**
     * 分页查询AI判题记录（带关联数据）
     */
    public Page<AIGradingRecordDTO> getAIGradingRecords(String status, Pageable pageable) {
        Page<AiGradingRecord> records;
        
        if ("pending".equals(status)) {
            // 查询待审核（低置信度<0.7）的记录
            records = aiGradingRecordRepository.findByAiConfidenceLessThan(BigDecimal.valueOf(0.7), pageable);
        } else {
            records = aiGradingRecordRepository.findAll(pageable);
        }
        
        return records.map(this::convertToDTO);
    }
    
    /**
     * 转换为DTO
     */
    private AIGradingRecordDTO convertToDTO(AiGradingRecord record) {
        AIGradingRecordDTO dto = new AIGradingRecordDTO();
        
        dto.setId(record.getId());
        dto.setAnswerRecordId(record.getAnswerRecordId());
        dto.setQuestionId(record.getQuestionId());
        dto.setUserId(record.getUserId());
        dto.setUserAnswer(record.getStudentAnswer());
        dto.setScore(record.getAiScore());
        dto.setConfidence(record.getAiConfidence());
        dto.setAiModel(record.getAiModel());
        dto.setManualReview(record.getManualReview());
        dto.setManualScore(record.getManualScore());
        dto.setReviewerId(record.getReviewerId());
        dto.setReviewComment(record.getReviewComment());
        dto.setFinalScore(record.getFinalScore());
        dto.setGradingTime(record.getGradingTime());
        dto.setGradedAt(record.getCreatedAt()); // 使用创建时间作为判题时间
        dto.setCreatedAt(record.getCreatedAt());
        
        // 解析并设置aiFeedback
        if (record.getAiFeedback() != null && !record.getAiFeedback().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> feedback = mapper.readValue(record.getAiFeedback(), Map.class);
                dto.setAiFeedback(feedback);
                
                // 从aiFeedback中提取comment作为feedback
                if (feedback.containsKey("comment")) {
                    dto.setFeedback(String.valueOf(feedback.get("comment")));
                } else if (feedback.containsKey("suggestions")) {
                    dto.setFeedback(String.valueOf(feedback.get("suggestions")));
                }
            } catch (Exception e) {
                log.warn("解析AI反馈失败", e);
            }
        }
        
        // 获取用户名
        userRepository.findById(record.getUserId()).ifPresent(user -> {
            dto.setUsername(user.getUsername());
        });
        
        // 获取题目内容
        questionRepository.findById(record.getQuestionId()).ifPresent(question -> {
            // 优先使用content，如果为空则使用title
            String content = question.getContent();
            if (content == null || content.trim().isEmpty()) {
                content = question.getTitle();
            }
            dto.setQuestionContent(content);
        });
        
        return dto;
    }
    
    /**
     * 获取统计信息
     */
    public java.util.Map<String, Object> getStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // 总判题数
        long totalCount = aiGradingRecordRepository.count();
        stats.put("totalCount", totalCount);
        
        // 待审核数（低置信度<0.7）
        long pendingCount = aiGradingRecordRepository.countByAiConfidenceLessThan(BigDecimal.valueOf(0.7));
        stats.put("pendingCount", pendingCount);
        
        // 高置信度数（≥0.9）
        long highConfidenceCount = aiGradingRecordRepository.countByAiConfidenceGreaterThanEqual(BigDecimal.valueOf(0.9));
        stats.put("highConfidenceCount", highConfidenceCount);
        
        // 平均置信度
        BigDecimal avgConfidence = aiGradingRecordRepository.findAverageConfidence();
        stats.put("avgConfidence", avgConfidence != null ? avgConfidence.doubleValue() : 0.0);
        
        return stats;
    }
    
    /**
     * 审核AI判题结果
     */
    @Transactional
    public void reviewAIGrading(Long id, BigDecimal score, String comment) {
        AiGradingRecord record = aiGradingRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI判题记录不存在"));
        
        // 更新人工复核信息
        record.setManualReview(true);
        record.setManualScore(score);
        record.setReviewComment(comment);
        record.setFinalScore(score); // 最终得分使用人工评分
        
        aiGradingRecordRepository.save(record);
        
        log.info("AI判题审核完成：recordId={}, score={}", id, score);
    }
    
    /**
     * 批量审核通过
     */
    @Transactional
    public void batchApprove(java.util.List<Long> ids) {
        for (Long id : ids) {
            aiGradingRecordRepository.findById(id).ifPresent(record -> {
                // 标记为已复核，使用AI评分作为最终得分
                record.setManualReview(true);
                record.setFinalScore(record.getAiScore());
                aiGradingRecordRepository.save(record);
            });
        }
        log.info("批量审核完成，数量：{}", ids.size());
    }
    
    /**
     * 删除AI判题记录
     */
    @Transactional
    public void deleteRecord(Long id) {
        aiGradingRecordRepository.deleteById(id);
        log.info("删除AI判题记录：{}", id);
    }
}

