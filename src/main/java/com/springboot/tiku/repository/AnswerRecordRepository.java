package com.springboot.tiku.repository;

import com.springboot.tiku.entity.AnswerRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 答题记录Repository
 */
@Repository
public interface AnswerRecordRepository extends JpaRepository<AnswerRecord, Long>, JpaSpecificationExecutor<AnswerRecord> {
    
    /**
     * 查询用户的答题记录
     */
    Page<AnswerRecord> findByUserIdOrderByAnsweredAtDesc(Long userId, Pageable pageable);
    
    /**
     * 查询用户对某题的最新答题记录
     */
    Optional<AnswerRecord> findFirstByUserIdAndQuestionIdOrderByAnsweredAtDesc(Long userId, Long questionId);
    
    /**
     * 查询用户某次考试的所有答题记录
     */
    List<AnswerRecord> findByUserIdAndExamId(Long userId, Long examId);
    
    /**
     * 统计用户总答题数
     */
    long countByUserId(Long userId);
    
    /**
     * 统计用户正确答题数
     */
    long countByUserIdAndIsCorrect(Long userId, Boolean isCorrect);
    
    /**
     * 查询待判题的记录
     */
    Page<AnswerRecord> findByGradingStatusOrderByAnsweredAtDesc(AnswerRecord.GradingStatus status, Pageable pageable);
    
    /**
     * 统计某时间段内的答题数
     */
    @Query("SELECT COUNT(a) FROM AnswerRecord a WHERE a.userId = :userId AND a.answeredAt BETWEEN :startTime AND :endTime")
    long countByUserIdAndAnsweredAtBetween(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计题目的答题情况
     */
    @Query("SELECT COUNT(a) FROM AnswerRecord a WHERE a.questionId = :questionId AND a.isCorrect = :isCorrect")
    long countByQuestionIdAndIsCorrect(@Param("questionId") Long questionId, @Param("isCorrect") Boolean isCorrect);
    
    /**
     * 根据学科统计答题数
     */
    @Query("SELECT COUNT(a) FROM AnswerRecord a JOIN Question q ON a.questionId = q.id WHERE a.userId = :userId AND q.subjectId = :subjectId")
    long countByUserIdAndQuestionSubjectId(@Param("userId") Long userId, @Param("subjectId") Long subjectId);
    
    /**
     * 根据学科统计正确题数
     */
    @Query("SELECT COUNT(a) FROM AnswerRecord a JOIN Question q ON a.questionId = q.id WHERE a.userId = :userId AND q.subjectId = :subjectId AND a.isCorrect = :isCorrect")
    long countByUserIdAndQuestionSubjectIdAndIsCorrect(@Param("userId") Long userId, @Param("subjectId") Long subjectId, @Param("isCorrect") Boolean isCorrect);
    
    /**
     * 根据章节统计答题数
     */
    @Query("SELECT COUNT(a) FROM AnswerRecord a JOIN Question q ON a.questionId = q.id WHERE a.userId = :userId AND q.chapterId = :chapterId")
    long countByUserIdAndQuestionChapterId(@Param("userId") Long userId, @Param("chapterId") Long chapterId);
    
    /**
     * 根据章节统计正确题数
     */
    @Query("SELECT COUNT(a) FROM AnswerRecord a JOIN Question q ON a.questionId = q.id WHERE a.userId = :userId AND q.chapterId = :chapterId AND a.isCorrect = :isCorrect")
    long countByUserIdAndQuestionChapterIdAndIsCorrect(@Param("userId") Long userId, @Param("chapterId") Long chapterId, @Param("isCorrect") Boolean isCorrect);
    
    /**
     * 查询时间范围内的答题记录
     */
    List<AnswerRecord> findByUserIdAndAnsweredAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
    
    /**
     * 获取用户最近的答题记录
     */
    Optional<AnswerRecord> findFirstByUserIdOrderByAnsweredAtDesc(Long userId);
    
    /**
     * 根据用户ID分页查询
     */
    Page<AnswerRecord> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 根据学科ID分页查询（需要join Question表）
     */
    @Query("SELECT a FROM AnswerRecord a JOIN Question q ON a.questionId = q.id WHERE q.subjectId = :subjectId")
    Page<AnswerRecord> findByQuestionSubjectId(@Param("subjectId") Long subjectId, Pageable pageable);
    
    /**
     * 统计是否正确的记录数
     */
    long countByIsCorrect(Boolean isCorrect);
    
    /**
     * 统计某时间之后的记录数
     */
    long countByAnsweredAtAfter(LocalDateTime answeredAt);
    
    /**
     * 统计某时间段内不同用户数
     */
    @Query("SELECT COUNT(DISTINCT a.userId) FROM AnswerRecord a WHERE a.answeredAt > :answeredAt")
    long countDistinctUsersByAnsweredAtAfter(@Param("answeredAt") LocalDateTime answeredAt);
    
    /**
     * 统计某时间段内的答题记录数
     */
    long countByAnsweredAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * 统计某时间段内不同用户数
     */
    @Query("SELECT COUNT(DISTINCT a.userId) FROM AnswerRecord a WHERE a.answeredAt BETWEEN :start AND :end")
    long countDistinctUsersByAnsweredAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * 统计用户对某道题的答题次数
     */
    Integer countByUserIdAndQuestionId(Long userId, Long questionId);
    
    /**
     * 统计用户在指定题目列表中做过的不同题目数量
     */
    @Query("SELECT COUNT(DISTINCT a.questionId) FROM AnswerRecord a WHERE a.userId = :userId AND a.questionId IN :questionIds")
    Long countDistinctQuestionsByUserIdAndQuestionIdIn(@Param("userId") Long userId, @Param("questionIds") List<Long> questionIds);
}


