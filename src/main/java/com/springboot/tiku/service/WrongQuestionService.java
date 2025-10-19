package com.springboot.tiku.service;

import com.springboot.tiku.dto.wrong.WrongQuestionDTO;
import com.springboot.tiku.dto.wrong.WrongQuestionStatsDTO;
import com.springboot.tiku.entity.AnswerRecord;
import com.springboot.tiku.entity.Chapter;
import com.springboot.tiku.entity.Question;
import com.springboot.tiku.entity.Subject;
import com.springboot.tiku.entity.WrongQuestion;
import com.springboot.tiku.repository.AnswerRecordRepository;
import com.springboot.tiku.repository.ChapterRepository;
import com.springboot.tiku.repository.QuestionRepository;
import com.springboot.tiku.repository.SubjectRepository;
import com.springboot.tiku.repository.WrongQuestionRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 错题本服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WrongQuestionService {
    
    private final WrongQuestionRepository wrongQuestionRepository;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final AnswerRecordRepository answerRecordRepository;
    
    /**
     * 获取用户错题列表（支持筛选）
     */
    public Page<WrongQuestionDTO> getUserWrongQuestions(
            Long userId, 
            Long subjectId, 
            Long chapterId,
            String type,
            String status,
            Pageable pageable
    ) {
        // 构建查询条件
        Specification<WrongQuestion> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 用户ID条件
            predicates.add(cb.equal(root.get("userId"), userId));
            
            // 未移除
            predicates.add(cb.equal(root.get("removed"), false));
            
            // 状态筛选
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), WrongQuestion.WrongStatus.valueOf(status)));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<WrongQuestion> wrongQuestionPage = wrongQuestionRepository.findAll(spec, pageable);
        
        List<WrongQuestionDTO> dtos = wrongQuestionPage.getContent().stream()
                .map(wq -> convertToDTO(wq, subjectId, chapterId, type))
                .filter(dto -> dto != null) // 过滤掉null（不符合筛选条件的）
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, dtos.size());
    }
    
    /**
     * 获取用户所有错题（不分页）
     */
    public List<WrongQuestionDTO> getAllUserWrongQuestions(Long userId) {
        List<WrongQuestion> wrongQuestions = wrongQuestionRepository
                .findByUserIdAndRemovedOrderByCreatedAtDesc(userId, false, Pageable.unpaged())
                .getContent();
        
        return wrongQuestions.stream()
                .map(wq -> convertToDTO(wq, null, null, null))
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取错题统计
     */
    public WrongQuestionStatsDTO getWrongQuestionStats(Long userId) {
        // 总错题数（未移除）
        long totalWrong = wrongQuestionRepository.countByUserIdAndRemoved(userId, false);
        
        // 已掌握
        long mastered = wrongQuestionRepository.findByUserIdAndStatusAndRemovedOrderByWrongCountDesc(
                userId, WrongQuestion.WrongStatus.MASTERED, false
        ).size();
        
        // 待复习 = 总数 - 已掌握
        long needReview = totalWrong - mastered;
        
        return new WrongQuestionStatsDTO(totalWrong, needReview, mastered);
    }
    
    /**
     * 标记为已掌握
     */
    @Transactional
    public void markAsMastered(Long userId, Long questionId) {
        wrongQuestionRepository.findByUserIdAndQuestionId(userId, questionId).ifPresent(wq -> {
            wq.setStatus(WrongQuestion.WrongStatus.MASTERED);
            wrongQuestionRepository.save(wq);
            log.info("用户{}标记题目{}为已掌握", userId, questionId);
        });
    }
    
    /**
     * 从错题本移除
     */
    @Transactional
    public void removeFromWrongBook(Long userId, Long questionId) {
        wrongQuestionRepository.findByUserIdAndQuestionId(userId, questionId).ifPresent(wq -> {
            wq.setRemoved(true);
            wrongQuestionRepository.save(wq);
            log.info("用户{}从错题本移除题目{}", userId, questionId);
        });
    }
    
    /**
     * 批量移除
     */
    @Transactional
    public void batchRemove(Long userId, List<Long> questionIds) {
        questionIds.forEach(questionId -> removeFromWrongBook(userId, questionId));
    }
    
    /**
     * 转换WrongQuestion为DTO
     */
    private WrongQuestionDTO convertToDTO(WrongQuestion wrongQuestion, Long subjectId, Long chapterId, String type) {
        WrongQuestionDTO dto = new WrongQuestionDTO();
        dto.setWrongQuestionId(wrongQuestion.getId());
        dto.setQuestionId(wrongQuestion.getQuestionId());
        dto.setWrongCount(wrongQuestion.getWrongCount());
        dto.setStatus(wrongQuestion.getStatus().name());
        dto.setLastWrongAt(wrongQuestion.getUpdatedAt());
        
        // 获取题目信息
        Question question = questionRepository.findById(wrongQuestion.getQuestionId()).orElse(null);
        if (question == null) {
            return null; // 题目不存在，跳过
        }
        
        dto.setType(question.getType() != null ? question.getType().name() : null);
        dto.setDifficulty(question.getDifficulty() != null ? question.getDifficulty().name() : null);
        dto.setTitle(question.getTitle());
        dto.setContent(question.getContent() != null ? question.getContent() : question.getTitle());
        dto.setOptions(question.getOptions());
        dto.setCorrectAnswer(question.getAnswer());
        dto.setExplanation(question.getAnswerAnalysis());
        
        // 学科筛选
        if (question.getSubjectId() != null) {
            dto.setSubjectId(question.getSubjectId());
            if (subjectId != null && !subjectId.equals(question.getSubjectId())) {
                return null; // 不符合学科筛选
            }
            
            Subject subject = subjectRepository.findById(question.getSubjectId()).orElse(null);
            if (subject != null) {
                dto.setSubjectName(subject.getName());
            }
        }
        
        // 章节筛选
        if (question.getChapterId() != null) {
            dto.setChapterId(question.getChapterId());
            if (chapterId != null && !chapterId.equals(question.getChapterId())) {
                return null; // 不符合章节筛选
            }
            
            Chapter chapter = chapterRepository.findById(question.getChapterId()).orElse(null);
            if (chapter != null) {
                dto.setChapterName(chapter.getName());
            }
        }
        
        // 题型筛选
        if (type != null && !type.isEmpty()) {
            if (dto.getType() == null || !dto.getType().equals(type)) {
                return null; // 不符合题型筛选
            }
        }
        
        // 获取最后一次错误的答题记录
        if (wrongQuestion.getLastAnswerRecordId() != null) {
            answerRecordRepository.findById(wrongQuestion.getLastAnswerRecordId()).ifPresent(ar -> {
                dto.setUserAnswer(ar.getUserAnswer());
            });
        }
        
        // 获取所有错误记录（最近5次）
        List<AnswerRecord> wrongRecords = answerRecordRepository
                .findAll((root, query, cb) -> {
                    query.orderBy(cb.desc(root.get("answeredAt")));
                    return cb.and(
                            cb.equal(root.get("userId"), wrongQuestion.getUserId()),
                            cb.equal(root.get("questionId"), wrongQuestion.getQuestionId()),
                            cb.equal(root.get("isCorrect"), false)
                    );
                })
                .stream()
                .limit(5)
                .collect(Collectors.toList());
        
        List<WrongQuestionDTO.WrongRecord> records = wrongRecords.stream()
                .map(ar -> {
                    WrongQuestionDTO.WrongRecord record = new WrongQuestionDTO.WrongRecord();
                    record.setWrongAt(ar.getAnsweredAt());
                    record.setUserAnswer(ar.getUserAnswer());
                    return record;
                })
                .collect(Collectors.toList());
        dto.setWrongRecords(records);
        
        return dto;
    }
}






