package com.springboot.tiku.repository;

import com.springboot.tiku.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目Repository
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {
    
    /**
     * 根据学科ID查询题目
     */
    Page<Question> findBySubjectIdAndStatus(Long subjectId, Integer status, Pageable pageable);
    
    /**
     * 根据学科ID和章节ID查询题目
     */
    Page<Question> findBySubjectIdAndChapterIdAndStatus(Long subjectId, Long chapterId, Integer status, Pageable pageable);
    
    /**
     * 根据题型查询题目
     */
    List<Question> findByTypeAndStatus(Question.QuestionType type, Integer status);
    
    /**
     * 根据学科ID和题型查询题目数量
     */
    long countBySubjectIdAndTypeAndStatus(Long subjectId, Question.QuestionType type, Integer status);
    
    /**
     * 根据章节ID和题型查询题目数量
     */
    long countByChapterIdAndTypeAndStatus(Long chapterId, Question.QuestionType type, Integer status);
    
    /**
     * 根据学科ID和状态查询题目数量
     */
    long countBySubjectIdAndStatus(Long subjectId, Integer status);
    
    /**
     * 根据章节ID和状态查询题目数量
     */
    long countByChapterIdAndStatus(Long chapterId, Integer status);
    
    /**
     * 随机获取题目
     */
    @Query(value = "SELECT * FROM question WHERE subject_id = :subjectId AND status = :status ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomQuestions(@Param("subjectId") Long subjectId, @Param("status") Integer status, @Param("limit") Integer limit);
    
    /**
     * 根据学科ID和题型随机获取题目
     */
    @Query(value = "SELECT * FROM question WHERE subject_id = :subjectId AND type = :type AND status = :status ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomQuestionsByType(@Param("subjectId") Long subjectId, @Param("type") String type, @Param("status") Integer status, @Param("limit") Integer limit);
    
    /**
     * 根据章节ID随机获取题目
     */
    @Query(value = "SELECT * FROM question WHERE chapter_id = :chapterId AND status = :status ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomQuestionsByChapter(@Param("chapterId") Long chapterId, @Param("status") Integer status, @Param("limit") Integer limit);
    
    /**
     * 统计某时间之后创建的题目数
     */
    long countByCreatedAtAfter(LocalDateTime createdAt);
    
    /**
     * 根据学科ID统计题目数
     */
    long countBySubjectId(Long subjectId);
    
    /**
     * 获取学科中最大的序号
     */
    @Query("SELECT MAX(q.serialNumber) FROM Question q WHERE q.subjectId = :subjectId")
    Integer findMaxSerialNumberBySubjectId(@Param("subjectId") Long subjectId);
    
    /**
     * 获取章节中最大的序号
     */
    @Query("SELECT MAX(q.serialNumber) FROM Question q WHERE q.chapterId = :chapterId")
    Integer findMaxSerialNumberByChapterId(@Param("chapterId") Long chapterId);
}


