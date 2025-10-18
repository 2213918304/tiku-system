package com.springboot.tiku.service;

import com.springboot.tiku.common.ResultCode;
import com.springboot.tiku.dto.practice.PracticeMode;
import com.springboot.tiku.dto.practice.PracticeRequest;
import com.springboot.tiku.dto.practice.PracticeSession;
import com.springboot.tiku.dto.question.QuestionDTO;
import com.springboot.tiku.entity.*;
import com.springboot.tiku.exception.BusinessException;
import com.springboot.tiku.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 刷题服务
 * 实现9种刷题模式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PracticeService {
    
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final AnswerRecordRepository answerRecordRepository;
    private final WrongQuestionRepository wrongQuestionRepository;
    private final FavoriteRepository favoriteRepository;
    
    /**
     * 开始刷题（根据模式选择对应的策略）
     */
    public PracticeSession startPractice(PracticeRequest request, Long userId) {
        // 验证学科是否存在
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new BusinessException(ResultCode.SUBJECT_NOT_FOUND));
        
        // 根据不同模式获取题目
        List<Question> questions = switch (request.getMode()) {
            case SEQUENTIAL -> getSequentialQuestions(request, userId);
            case RANDOM -> getRandomQuestions(request);
            case CHAPTER -> getChapterQuestions(request);
            case EXAM -> getExamQuestions(request);
            case WRONG_QUESTION -> getWrongQuestions(request, userId);
            case FAVORITE -> getFavoriteQuestions(request, userId);
            case CHALLENGE -> getChallengeQuestions(request, userId);
            case TIMED -> getTimedChallengeQuestions(request);
            case SMART_RECOMMEND -> getSmartRecommendQuestions(request, userId);
        };
        
        if (questions.isEmpty()) {
            throw new BusinessException("没有找到符合条件的题目");
        }
        
        // 转换为DTO
        List<QuestionDTO> questionDTOs = questions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 构建刷题会话
        return buildPracticeSession(request, subject, questionDTOs);
    }
    
    /**
     * 1. 顺序刷题
     */
    private List<Question> getSequentialQuestions(PracticeRequest request, Long userId) {
        Specification<Question> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("subjectId"), request.getSubjectId()));
            predicates.add(cb.equal(root.get("status"), 1));
            
            if (request.getQuestionType() != null) {
                predicates.add(cb.equal(root.get("type"), request.getQuestionType()));
            }
            if (request.getDifficulty() != null) {
                predicates.add(cb.equal(root.get("difficulty"), request.getDifficulty()));
            }
            if (request.getChapterId() != null) {
                predicates.add(cb.equal(root.get("chapterId"), request.getChapterId()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // 如果继续上次进度，获取上次最后答题的位置
        int offset = 0;
        if (Boolean.TRUE.equals(request.getContinueProgress())) {
            offset = getLastProgress(userId, request.getSubjectId());
        }
        
        Pageable pageable = PageRequest.of(offset / request.getCount(), request.getCount());
        return questionRepository.findAll(spec, pageable).getContent();
    }
    
    /**
     * 2. 随机刷题
     */
    private List<Question> getRandomQuestions(PracticeRequest request) {
        if (request.getQuestionType() != null) {
            return questionRepository.findRandomQuestionsByType(
                    request.getSubjectId(),
                    request.getQuestionType().name(),
                    1,
                    request.getCount()
            );
        } else {
            return questionRepository.findRandomQuestions(
                    request.getSubjectId(),
                    1,
                    request.getCount()
            );
        }
    }
    
    /**
     * 3. 章节练习
     */
    private List<Question> getChapterQuestions(PracticeRequest request) {
        if (request.getChapterId() == null) {
            throw new BusinessException("章节练习模式必须指定章节");
        }
        
        return questionRepository.findRandomQuestionsByChapter(
                request.getChapterId(),
                1,
                request.getCount()
        );
    }
    
    /**
     * 4. 考试模拟（按章节抽题）
     */
    private List<Question> getExamQuestions(PracticeRequest request) {
        // 获取学科的所有章节
        List<Chapter> chapters = chapterRepository.findBySubjectIdOrderBySortOrderAsc(request.getSubjectId());
        
        if (chapters.isEmpty()) {
            // 如果没有章节，随机抽取
            return getRandomQuestions(request);
        }
        
        // 每个章节抽取的题目数
        int questionsPerChapter = Math.max(1, request.getCount() / chapters.size());
        List<Question> allQuestions = new ArrayList<>();
        
        for (Chapter chapter : chapters) {
            List<Question> chapterQuestions = questionRepository.findRandomQuestionsByChapter(
                    chapter.getId(),
                    1,
                    questionsPerChapter
            );
            allQuestions.addAll(chapterQuestions);
        }
        
        // 打乱顺序
        Collections.shuffle(allQuestions);
        
        // 限制总数
        return allQuestions.stream()
                .limit(request.getCount())
                .collect(Collectors.toList());
    }
    
    /**
     * 5. 错题强化
     */
    private List<Question> getWrongQuestions(PracticeRequest request, Long userId) {
        // 获取用户的错题
        List<WrongQuestion> wrongQuestions = wrongQuestionRepository
                .findByUserIdAndStatusAndRemovedOrderByWrongCountDesc(
                        userId,
                        WrongQuestion.WrongStatus.WRONG,
                        false
                );
        
        if (wrongQuestions.isEmpty()) {
            wrongQuestions = wrongQuestionRepository
                    .findByUserIdAndStatusAndRemovedOrderByWrongCountDesc(
                            userId,
                            WrongQuestion.WrongStatus.REPEATED_WRONG,
                            false
                    );
        }
        
        List<Long> questionIds = wrongQuestions.stream()
                .map(WrongQuestion::getQuestionId)
                .limit(request.getCount())
                .collect(Collectors.toList());
        
        if (questionIds.isEmpty()) {
            throw new BusinessException("暂无错题，建议先完成一些练习");
        }
        
        return questionRepository.findAllById(questionIds);
    }
    
    /**
     * 6. 收藏专练
     */
    private List<Question> getFavoriteQuestions(PracticeRequest request, Long userId) {
        Pageable pageable = PageRequest.of(0, request.getCount());
        List<Favorite> favorites = favoriteRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .getContent();
        
        if (favorites.isEmpty()) {
            throw new BusinessException("暂无收藏题目，建议先收藏一些重点题目");
        }
        
        List<Long> questionIds = favorites.stream()
                .map(Favorite::getQuestionId)
                .collect(Collectors.toList());
        
        return questionRepository.findAllById(questionIds);
    }
    
    /**
     * 7. 闯关模式
     */
    private List<Question> getChallengeQuestions(PracticeRequest request, Long userId) {
        int level = request.getChallengeLevel() != null ? request.getChallengeLevel() : 1;
        
        // 根据关卡调整题目难度和数量
        Question.Difficulty difficulty;
        int count;
        
        if (level <= 3) {
            difficulty = Question.Difficulty.EASY;
            count = 10;
        } else if (level <= 6) {
            difficulty = Question.Difficulty.MEDIUM;
            count = 15;
        } else {
            difficulty = Question.Difficulty.HARD;
            count = 20;
        }
        
        Specification<Question> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("subjectId"), request.getSubjectId()));
            predicates.add(cb.equal(root.get("difficulty"), difficulty));
            predicates.add(cb.equal(root.get("status"), 1));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Pageable pageable = PageRequest.of(0, count);
        return questionRepository.findAll(spec, pageable).getContent();
    }
    
    /**
     * 8. 限时挑战
     */
    private List<Question> getTimedChallengeQuestions(PracticeRequest request) {
        // 限时挑战通常选择中等难度的题目
        Specification<Question> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("subjectId"), request.getSubjectId()));
            predicates.add(cb.equal(root.get("difficulty"), Question.Difficulty.MEDIUM));
            predicates.add(cb.equal(root.get("status"), 1));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Pageable pageable = PageRequest.of(0, request.getCount());
        return questionRepository.findAll(spec, pageable).getContent();
    }
    
    /**
     * 9. 智能推荐
     */
    private List<Question> getSmartRecommendQuestions(PracticeRequest request, Long userId) {
        // 分析用户的答题数据
        List<AnswerRecord> records = answerRecordRepository
                .findByUserIdOrderByAnsweredAtDesc(userId, PageRequest.of(0, 100))
                .getContent();
        
        // 统计每个章节的正确率
        Map<Long, List<AnswerRecord>> chapterRecords = records.stream()
                .filter(r -> r.getIsCorrect() != null)
                .collect(Collectors.groupingBy(r -> {
                    Question q = questionRepository.findById(r.getQuestionId()).orElse(null);
                    return q != null && q.getChapterId() != null ? q.getChapterId() : 0L;
                }));
        
        // 找出正确率最低的章节
        Long weakChapterId = chapterRecords.entrySet().stream()
                .filter(entry -> entry.getKey() > 0)
                .min(Comparator.comparingDouble(entry -> {
                    List<AnswerRecord> chapterAnswers = entry.getValue();
                    long correctCount = chapterAnswers.stream()
                            .filter(r -> Boolean.TRUE.equals(r.getIsCorrect()))
                            .count();
                    return (double) correctCount / chapterAnswers.size();
                }))
                .map(Map.Entry::getKey)
                .orElse(null);
        
        // 如果找到薄弱章节，推荐该章节的题目
        if (weakChapterId != null) {
            return questionRepository.findRandomQuestionsByChapter(
                    weakChapterId,
                    1,
                    request.getCount()
            );
        }
        
        // 否则随机推荐
        return getRandomQuestions(request);
    }
    
    /**
     * 获取用户上次答题进度
     */
    private int getLastProgress(Long userId, Long subjectId) {
        // 查询用户在该学科的答题总数
        long count = answerRecordRepository.countByUserIdAndAnsweredAtBetween(
                userId,
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now()
        );
        return (int) count;
    }
    
    /**
     * 构建刷题会话
     */
    private PracticeSession buildPracticeSession(PracticeRequest request, Subject subject, List<QuestionDTO> questions) {
        String sessionId = UUID.randomUUID().toString();
        
        PracticeSession.PracticeSessionBuilder builder = PracticeSession.builder()
                .sessionId(sessionId)
                .mode(request.getMode())
                .subjectId(subject.getId())
                .subjectName(subject.getName())
                .questions(questions)
                .totalCount(questions.size())
                .currentProgress(0)
                .startTime(LocalDateTime.now());
        
        // 根据不同模式设置额外信息
        switch (request.getMode()) {
            case EXAM:
                builder.examDuration(request.getExamDuration())
                        .endTime(LocalDateTime.now().plusMinutes(request.getExamDuration()))
                        .tip("模拟考试，限时" + request.getExamDuration() + "分钟，请认真作答");
                break;
            case TIMED:
                builder.timePerQuestion(request.getTimePerQuestion())
                        .tip("限时挑战，每题" + request.getTimePerQuestion() + "秒，快速作答");
                break;
            case CHALLENGE:
                int level = request.getChallengeLevel() != null ? request.getChallengeLevel() : 1;
                int passRequired = (int) (questions.size() * 0.8);
                builder.challengeLevel(level)
                        .passRequiredCorrect(passRequired)
                        .tip("第" + level + "关，答对" + passRequired + "题即可通关");
                break;
            case CHAPTER:
                if (request.getChapterId() != null) {
                    chapterRepository.findById(request.getChapterId()).ifPresent(chapter -> {
                        builder.chapterId(chapter.getId())
                                .chapterName(chapter.getName())
                                .tip("章节专项练习：" + chapter.getName());
                    });
                }
                break;
            case WRONG_QUESTION:
                builder.tip("错题强化模式，专攻薄弱知识点");
                break;
            case FAVORITE:
                builder.tip("收藏题专练，复习重点内容");
                break;
            case SMART_RECOMMEND:
                builder.tip("AI智能推荐，针对性提升");
                break;
            default:
                builder.tip("开始练习，加油！");
        }
        
        return builder.build();
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
                Object options = new com.fasterxml.jackson.databind.ObjectMapper().readValue(question.getOptions(), Object.class);
                dto.setOptions(options);
            }
            if (question.getAnswer() != null && !question.getAnswer().isEmpty()) {
                Object answer = new com.fasterxml.jackson.databind.ObjectMapper().readValue(question.getAnswer(), Object.class);
                dto.setAnswer(answer);
            }
        } catch (Exception e) {
            log.error("反序列化JSON字段失败: questionId={}", question.getId(), e);
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
        
        return dto;
    }
}



