package com.springboot.tiku.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.tiku.common.ResultCode;
import com.springboot.tiku.dto.grading.GradingResult;
import com.springboot.tiku.dto.grading.SubmitAnswerRequest;
import com.springboot.tiku.entity.AiGradingRecord;
import com.springboot.tiku.entity.AnswerRecord;
import com.springboot.tiku.entity.Question;
import com.springboot.tiku.entity.WrongQuestion;
import com.springboot.tiku.exception.BusinessException;
import com.springboot.tiku.repository.*;
import com.springboot.tiku.service.grading.AIGradingStrategy;
import com.springboot.tiku.service.grading.AutoGradingStrategy;
import com.springboot.tiku.service.grading.GradingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 判题服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GradingService {
    
    private final QuestionRepository questionRepository;
    private final AnswerRecordRepository answerRecordRepository;
    private final WrongQuestionRepository wrongQuestionRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final AiGradingRecordRepository aiGradingRecordRepository;
    
    private final AutoGradingStrategy autoGradingStrategy;
    private final AIGradingStrategy aiGradingStrategy;
    
    private final ObjectMapper objectMapper;
    
    /**
     * 提交答案并判题
     */
    @Transactional
    public GradingResult submitAndGrade(SubmitAnswerRequest request, Long userId) {
        // 获取题目
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new BusinessException(ResultCode.QUESTION_NOT_FOUND));
        
        // 选择判题策略
        GradingStrategy strategy = selectGradingStrategy(question);
        
        // 执行判题
        GradingResult result = strategy.grade(question, request.getUserAnswer());
        
        // 保存答题记录
        AnswerRecord record = saveAnswerRecord(request, userId, question, result);
        result.setAnswerRecordId(record.getId());
        
        // 如果是AI判题，保存AI判题记录
        if ("AI".equals(result.getGradingType()) && result.getAiFeedback() != null) {
            saveAIGradingRecord(record, question, result);
        }
        
        // 更新题目统计
        updateQuestionStatistics(question, result.getIsCorrect());
        
        // 更新用户统计
        updateUserStatistics(userId, result.getIsCorrect());
        
        // 处理错题本
        handleWrongQuestion(userId, question.getId(), result.getIsCorrect());
        
        return result;
    }
    
    /**
     * 批量提交答案（考试场景）
     */
    @Transactional
    public List<GradingResult> batchSubmitAndGrade(List<SubmitAnswerRequest> requests, Long userId) {
        return requests.stream()
                .map(request -> submitAndGrade(request, userId))
                .toList();
    }
    
    /**
     * 选择判题策略
     */
    private GradingStrategy selectGradingStrategy(Question question) {
        // 1. 优先检查：如果题型只能用AI判题（如论述题、简答题等主观题）
        if (aiGradingStrategy.supports(question.getType()) && !autoGradingStrategy.supports(question.getType())) {
            log.debug("题目ID: {} 是主观题，使用AI判题", question.getId());
            return aiGradingStrategy;
        }
        
        // 2. 如果题目明确启用了AI判题，且题型支持AI判题
        if (Boolean.TRUE.equals(question.getAiGradingEnabled()) && aiGradingStrategy.supports(question.getType())) {
            log.debug("题目ID: {} 启用了AI判题", question.getId());
            return aiGradingStrategy;
        }
        
        // 3. 否则使用自动判题
        if (autoGradingStrategy.supports(question.getType())) {
            log.debug("题目ID: {} 使用自动判题", question.getId());
            return autoGradingStrategy;
        }
        
        // 4. 都不支持
        log.error("题目ID: {} 类型: {} 不支持判题", question.getId(), question.getType());
        throw new BusinessException(ResultCode.QUESTION_TYPE_NOT_SUPPORT);
    }
    
    /**
     * 保存答题记录
     */
    private AnswerRecord saveAnswerRecord(SubmitAnswerRequest request, Long userId, Question question, GradingResult result) {
        AnswerRecord record = new AnswerRecord();
        record.setUserId(userId);
        record.setQuestionId(question.getId());
        record.setPracticeMode(request.getPracticeMode());
        record.setExamId(request.getExamId());
        
        // 序列化用户答案为JSON字符串
        try {
            String userAnswerJson = objectMapper.writeValueAsString(request.getUserAnswer());
            record.setUserAnswer(userAnswerJson);
        } catch (Exception e) {
            log.error("序列化用户答案失败", e);
            // 如果序列化失败，尝试直接转换为字符串
            record.setUserAnswer(String.valueOf(request.getUserAnswer()));
        }
        
        record.setIsCorrect(result.getIsCorrect());
        record.setScore(result.getScore());
        record.setTimeSpent(request.getTimeSpent());
        record.setAnsweredAt(LocalDateTime.now());
        
        // 设置判题类型和状态
        if ("AI".equals(result.getGradingType())) {
            record.setGradingType(AnswerRecord.GradingType.AI);
            if (Boolean.TRUE.equals(result.getNeedManualReview())) {
                record.setGradingStatus(AnswerRecord.GradingStatus.REVIEWING);
            } else {
                record.setGradingStatus(AnswerRecord.GradingStatus.GRADED);
                record.setGradedAt(LocalDateTime.now());
            }
        } else {
            record.setGradingType(AnswerRecord.GradingType.AUTO);
            record.setGradingStatus(AnswerRecord.GradingStatus.GRADED);
            record.setGradedAt(LocalDateTime.now());
        }
        
        return answerRecordRepository.save(record);
    }
    
    /**
     * 保存AI判题记录
     */
    private void saveAIGradingRecord(AnswerRecord answerRecord, Question question, GradingResult result) {
        try {
            AiGradingRecord aiRecord = new AiGradingRecord();
            aiRecord.setAnswerRecordId(answerRecord.getId());
            aiRecord.setQuestionId(question.getId());
            aiRecord.setUserId(answerRecord.getUserId());
            
            // 提取用户答案文本
            String userAnswerText = extractAnswerText(answerRecord.getUserAnswer());
            aiRecord.setStudentAnswer(userAnswerText);
            
            // 设置AI判题结果
            aiRecord.setAiScore(result.getScore());
            aiRecord.setAiConfidence(result.getAiFeedback().getConfidence());
            aiRecord.setAiModel(result.getAiFeedback().getModel());
            
            // 序列化AI反馈为JSON
            String aiFeedbackJson = objectMapper.writeValueAsString(result.getAiFeedback());
            aiRecord.setAiFeedback(aiFeedbackJson);
            
            // 设置是否需要人工复核
            aiRecord.setManualReview(result.getNeedManualReview() != null && result.getNeedManualReview());
            
            // 如果不需要人工复核，最终分数就是AI分数
            if (!aiRecord.getManualReview()) {
                aiRecord.setFinalScore(result.getScore());
            }
            
            aiGradingRecordRepository.save(aiRecord);
            log.info("AI判题记录已保存：answerRecordId={}, questionId={}", answerRecord.getId(), question.getId());
            
        } catch (Exception e) {
            log.error("保存AI判题记录失败：answerRecordId={}, error={}", answerRecord.getId(), e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 提取答案文本
     */
    private String extractAnswerText(String userAnswerJson) {
        try {
            // 尝试从JSON中提取answer字段
            com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSON.parseObject(userAnswerJson);
            if (json != null && json.containsKey("answer")) {
                Object answer = json.get("answer");
                return answer != null ? answer.toString() : userAnswerJson;
            }
            return userAnswerJson;
        } catch (Exception e) {
            // 如果不是JSON，直接返回原文本
            return userAnswerJson;
        }
    }
    
    /**
     * 更新题目统计
     */
    private void updateQuestionStatistics(Question question, Boolean isCorrect) {
        question.setUseCount(question.getUseCount() + 1);
        if (Boolean.TRUE.equals(isCorrect)) {
            question.setCorrectCount(question.getCorrectCount() + 1);
        } else {
            question.setWrongCount(question.getWrongCount() + 1);
        }
        questionRepository.save(question);
    }
    
    /**
     * 更新用户统计
     */
    private void updateUserStatistics(Long userId, Boolean isCorrect) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setTotalAnswerCount(user.getTotalAnswerCount() + 1);
            if (Boolean.TRUE.equals(isCorrect)) {
                user.setTotalCorrectCount(user.getTotalCorrectCount() + 1);
            }
            userRepository.save(user);
        });
    }
    
    /**
     * 处理错题本
     */
    private void handleWrongQuestion(Long userId, Long questionId, Boolean isCorrect) {
        if (Boolean.FALSE.equals(isCorrect)) {
            // 答错了，加入或更新错题本
            WrongQuestion wrongQuestion = wrongQuestionRepository
                    .findByUserIdAndQuestionId(userId, questionId)
                    .orElse(new WrongQuestion());
            
            wrongQuestion.setUserId(userId);
            wrongQuestion.setQuestionId(questionId);
            wrongQuestion.setWrongCount(wrongQuestion.getWrongCount() == null ? 1 : wrongQuestion.getWrongCount() + 1);
            wrongQuestion.setRemoved(false);
            
            // 根据错误次数设置状态
            if (wrongQuestion.getWrongCount() >= 3) {
                wrongQuestion.setStatus(WrongQuestion.WrongStatus.REPEATED_WRONG);
            } else {
                wrongQuestion.setStatus(WrongQuestion.WrongStatus.WRONG);
            }
            
            wrongQuestionRepository.save(wrongQuestion);
        } else {
            // 答对了，更新错题状态为已掌握
            wrongQuestionRepository.findByUserIdAndQuestionId(userId, questionId)
                    .ifPresent(wrongQuestion -> {
                        wrongQuestion.setStatus(WrongQuestion.WrongStatus.MASTERED);
                        wrongQuestionRepository.save(wrongQuestion);
                    });
        }
    }
}

