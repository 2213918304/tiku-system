package com.springboot.tiku.service;

import com.springboot.tiku.dto.answer.AnswerRecordDTO;
import com.springboot.tiku.entity.AnswerRecord;
import com.springboot.tiku.entity.Question;
import com.springboot.tiku.repository.AnswerRecordRepository;
import com.springboot.tiku.repository.ChapterRepository;
import com.springboot.tiku.repository.QuestionRepository;
import com.springboot.tiku.repository.SubjectRepository;
import com.springboot.tiku.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 答题记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerRecordService {
    
    private final AnswerRecordRepository answerRecordRepository;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    
    /**
     * 分页查询答题记录（带关联数据）
     */
    public Page<AnswerRecordDTO> getAnswerRecords(Long userId, Long subjectId, Boolean isCorrect, Pageable pageable) {
        Specification<AnswerRecord> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 用户ID筛选
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            }
            
            // 是否正确筛选
            if (isCorrect != null) {
                predicates.add(criteriaBuilder.equal(root.get("isCorrect"), isCorrect));
            }
            
            // 学科ID筛选 - 使用子查询
            if (subjectId != null) {
                var subquery = query.subquery(Long.class);
                var questionRoot = subquery.from(Question.class);
                subquery.select(questionRoot.get("id"))
                    .where(criteriaBuilder.equal(questionRoot.get("subjectId"), subjectId));
                
                predicates.add(root.get("questionId").in(subquery));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<AnswerRecord> records = answerRecordRepository.findAll(spec, pageable);
        return records.map(this::convertToDTO);
    }
    
    /**
     * 转换为DTO
     */
    private AnswerRecordDTO convertToDTO(AnswerRecord record) {
        AnswerRecordDTO dto = new AnswerRecordDTO();
        BeanUtils.copyProperties(record, dto);
        
        // 获取用户信息
        userRepository.findById(record.getUserId()).ifPresent(user -> {
            dto.setUsername(user.getUsername());
        });
        
        // 获取题目信息
        questionRepository.findById(record.getQuestionId()).ifPresent(question -> {
            AnswerRecordDTO.QuestionInfo questionInfo = new AnswerRecordDTO.QuestionInfo();
            questionInfo.setId(question.getId());
            questionInfo.setTitle(question.getTitle());
            questionInfo.setContent(question.getContent());
            questionInfo.setType(question.getType() != null ? question.getType().name() : null);
            questionInfo.setAnswer(question.getAnswer());
            questionInfo.setAnswerAnalysis(question.getAnswerAnalysis());
            questionInfo.setScore(question.getScore());
            
            // 获取学科信息
            subjectRepository.findById(question.getSubjectId()).ifPresent(subject -> {
                AnswerRecordDTO.SubjectInfo subjectInfo = new AnswerRecordDTO.SubjectInfo();
                subjectInfo.setId(subject.getId());
                subjectInfo.setName(subject.getName());
                subjectInfo.setCode(subject.getCode());
                questionInfo.setSubject(subjectInfo);
            });
            
            // 获取章节信息
            if (question.getChapterId() != null) {
                chapterRepository.findById(question.getChapterId()).ifPresent(chapter -> {
                    AnswerRecordDTO.ChapterInfo chapterInfo = new AnswerRecordDTO.ChapterInfo();
                    chapterInfo.setId(chapter.getId());
                    chapterInfo.setName(chapter.getName());
                    questionInfo.setChapter(chapterInfo);
                });
            }
            
            dto.setQuestion(questionInfo);
        });
        
        return dto;
    }
    
    /**
     * 删除答题记录
     */
    @Transactional
    public void deleteRecord(Long id) {
        answerRecordRepository.deleteById(id);
        log.info("删除答题记录成功：{}", id);
    }
    
    /**
     * 批量删除答题记录
     */
    @Transactional
    public void batchDeleteRecords(java.util.List<Long> ids) {
        answerRecordRepository.deleteAllById(ids);
        log.info("批量删除答题记录成功，数量：{}", ids.size());
    }
}

