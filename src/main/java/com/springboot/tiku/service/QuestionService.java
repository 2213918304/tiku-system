package com.springboot.tiku.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.tiku.common.ResultCode;
import com.springboot.tiku.dto.question.QuestionDTO;
import com.springboot.tiku.dto.question.QuestionQueryRequest;
import com.springboot.tiku.dto.question.QuestionRequest;
import com.springboot.tiku.entity.Chapter;
import com.springboot.tiku.entity.Question;
import com.springboot.tiku.entity.Subject;
import com.springboot.tiku.entity.User;
import com.springboot.tiku.exception.BusinessException;
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
 * 题目服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {
    
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 创建题目
     */
    @Transactional
    public QuestionDTO createQuestion(QuestionRequest request, Long userId) {
        // 检查学科是否存在
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new BusinessException(ResultCode.SUBJECT_NOT_FOUND));
        
        // 检查章节是否存在
        if (request.getChapterId() != null) {
            chapterRepository.findById(request.getChapterId())
                    .orElseThrow(() -> new BusinessException(ResultCode.CHAPTER_NOT_FOUND));
        }
        
        Question question = new Question();
        BeanUtils.copyProperties(request, question);
        question.setCreatorId(userId);
        
        // 手动将对象字段转换为 JSON 字符串
        try {
            if (request.getOptions() != null) {
                question.setOptions(objectMapper.writeValueAsString(request.getOptions()));
            }
            if (request.getAnswer() != null) {
                question.setAnswer(objectMapper.writeValueAsString(request.getAnswer()));
            }
            if (request.getAiGradingConfig() != null) {
                question.setAiGradingConfig(objectMapper.writeValueAsString(request.getAiGradingConfig()));
            }
            if (request.getScoringCriteria() != null) {
                question.setScoringCriteria(objectMapper.writeValueAsString(request.getScoringCriteria()));
            }
            if (request.getReferenceKeywords() != null) {
                question.setReferenceKeywords(objectMapper.writeValueAsString(request.getReferenceKeywords()));
            }
        } catch (JsonProcessingException e) {
            log.error("序列化JSON字段失败", e);
            throw new BusinessException(ResultCode.DATA_FORMAT_ERROR, "题目数据格式错误");
        }
        
        question = questionRepository.save(question);
        
        // 更新学科题目数量
        subject.setQuestionCount(subject.getQuestionCount() + 1);
        subjectRepository.save(subject);
        
        log.info("创建题目成功：{}", question.getTitle());
        return convertToDTO(question);
    }
    
    /**
     * 更新题目
     */
    @Transactional
    public QuestionDTO updateQuestion(Long id, QuestionRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.QUESTION_NOT_FOUND));
        
        // 如果更换了学科，需要更新学科的题目数量
        if (!request.getSubjectId().equals(question.getSubjectId())) {
            // 原学科题目数减1
            Subject oldSubject = subjectRepository.findById(question.getSubjectId()).orElse(null);
            if (oldSubject != null) {
                oldSubject.setQuestionCount(Math.max(0, oldSubject.getQuestionCount() - 1));
                subjectRepository.save(oldSubject);
            }
            
            // 新学科题目数加1
            Subject newSubject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new BusinessException(ResultCode.SUBJECT_NOT_FOUND));
            newSubject.setQuestionCount(newSubject.getQuestionCount() + 1);
            subjectRepository.save(newSubject);
        }
        
        // 更新题目字段
        question.setSubjectId(request.getSubjectId());
        question.setChapterId(request.getChapterId());
        question.setType(request.getType());
        question.setTitle(request.getTitle());
        question.setContent(request.getContent());
        question.setDifficulty(request.getDifficulty());
        question.setScore(request.getScore());
        question.setAnswerAnalysis(request.getAnswerAnalysis());
        question.setAiGradingEnabled(request.getAiGradingEnabled());
        question.setTags(request.getTags());
        question.setKnowledgePoints(request.getKnowledgePoints());
        question.setStatus(request.getStatus());
        
        // 手动将对象字段转换为 JSON 字符串
        try {
            if (request.getOptions() != null) {
                question.setOptions(objectMapper.writeValueAsString(request.getOptions()));
            }
            if (request.getAnswer() != null) {
                question.setAnswer(objectMapper.writeValueAsString(request.getAnswer()));
            }
            if (request.getAiGradingConfig() != null) {
                question.setAiGradingConfig(objectMapper.writeValueAsString(request.getAiGradingConfig()));
            }
            if (request.getScoringCriteria() != null) {
                question.setScoringCriteria(objectMapper.writeValueAsString(request.getScoringCriteria()));
            }
            if (request.getReferenceKeywords() != null) {
                question.setReferenceKeywords(objectMapper.writeValueAsString(request.getReferenceKeywords()));
            }
        } catch (JsonProcessingException e) {
            log.error("序列化JSON字段失败", e);
            throw new BusinessException(ResultCode.DATA_FORMAT_ERROR, "题目数据格式错误");
        }
        
        question = questionRepository.save(question);
        
        log.info("更新题目成功：{}", question.getTitle());
        return convertToDTO(question);
    }
    
    /**
     * 删除题目
     */
    @Transactional
    public void deleteQuestion(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.QUESTION_NOT_FOUND));
        
        // 更新学科题目数量
        Subject subject = subjectRepository.findById(question.getSubjectId()).orElse(null);
        if (subject != null) {
            subject.setQuestionCount(Math.max(0, subject.getQuestionCount() - 1));
            subjectRepository.save(subject);
        }
        
        questionRepository.delete(question);
        log.info("删除题目成功：{}", question.getTitle());
    }
    
    /**
     * 批量删除题目
     */
    @Transactional
    public void batchDeleteQuestions(List<Long> ids) {
        for (Long id : ids) {
            deleteQuestion(id);
        }
    }
    
    /**
     * 获取题目详情
     */
    public QuestionDTO getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.QUESTION_NOT_FOUND));
        return convertToDTO(question);
    }
    
    /**
     * 分页查询题目
     */
    public Page<QuestionDTO> getQuestions(QuestionQueryRequest queryRequest, Pageable pageable) {
        Specification<Question> spec = buildSpecification(queryRequest);
        return questionRepository.findAll(spec, pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * 随机获取题目
     */
    public List<QuestionDTO> getRandomQuestions(Long subjectId, Question.QuestionType type, Integer count) {
        List<Question> questions;
        if (type != null) {
            questions = questionRepository.findRandomQuestionsByType(subjectId, type.name(), 1, count);
        } else {
            questions = questionRepository.findRandomQuestions(subjectId, 1, count);
        }
        return questions.stream().map(this::convertToDTO).toList();
    }
    
    /**
     * 更新题目状态
     */
    @Transactional
    public void updateQuestionStatus(Long id, Integer status) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.QUESTION_NOT_FOUND));
        question.setStatus(status);
        questionRepository.save(question);
    }
    
    /**
     * 构建查询条件
     */
    private Specification<Question> buildSpecification(QuestionQueryRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (request.getSubjectId() != null) {
                predicates.add(cb.equal(root.get("subjectId"), request.getSubjectId()));
            }
            
            if (request.getChapterId() != null) {
                predicates.add(cb.equal(root.get("chapterId"), request.getChapterId()));
            }
            
            if (request.getType() != null) {
                predicates.add(cb.equal(root.get("type"), request.getType()));
            }
            
            if (request.getDifficulty() != null) {
                predicates.add(cb.equal(root.get("difficulty"), request.getDifficulty()));
            }
            
            if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                String likePattern = "%" + request.getKeyword() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), likePattern),
                        cb.like(root.get("content"), likePattern)
                ));
            }
            
            if (request.getTag() != null && !request.getTag().isEmpty()) {
                predicates.add(cb.like(root.get("tags"), "%" + request.getTag() + "%"));
            }
            
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            
            if (request.getCreatorId() != null) {
                predicates.add(cb.equal(root.get("creatorId"), request.getCreatorId()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * 转换为DTO
     */
    private QuestionDTO convertToDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        BeanUtils.copyProperties(question, dto);
        
        // 将 JSON 字符串反序列化为对象
        try {
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                Object options = objectMapper.readValue(question.getOptions(), Object.class);
                dto.setOptions(options);
            }
            if (question.getAnswer() != null && !question.getAnswer().isEmpty()) {
                Object answer = objectMapper.readValue(question.getAnswer(), Object.class);
                dto.setAnswer(answer);
            }
            if (question.getAiGradingConfig() != null && !question.getAiGradingConfig().isEmpty()) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> config = objectMapper.readValue(question.getAiGradingConfig(), java.util.Map.class);
                dto.setAiGradingConfig(config);
            }
            if (question.getScoringCriteria() != null && !question.getScoringCriteria().isEmpty()) {
                Object criteria = objectMapper.readValue(question.getScoringCriteria(), Object.class);
                dto.setScoringCriteria(criteria);
            }
            if (question.getReferenceKeywords() != null && !question.getReferenceKeywords().isEmpty()) {
                Object keywords = objectMapper.readValue(question.getReferenceKeywords(), Object.class);
                dto.setReferenceKeywords(keywords);
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化JSON字段失败: questionId={}", question.getId(), e);
            // 发生错误时，保持字段为 null，不影响其他数据
        }
        
        // 填充学科名称
        subjectRepository.findById(question.getSubjectId()).ifPresent(subject -> {
            dto.setSubjectName(subject.getName());
        });
        
        // 填充章节名称
        if (question.getChapterId() != null) {
            chapterRepository.findById(question.getChapterId()).ifPresent(chapter -> {
                dto.setChapterName(chapter.getName());
            });
        }
        
        // 填充创建者名称
        if (question.getCreatorId() != null) {
            userRepository.findById(question.getCreatorId()).ifPresent(user -> {
                dto.setCreatorName(user.getUsername());
            });
        }
        
        return dto;
    }
}



