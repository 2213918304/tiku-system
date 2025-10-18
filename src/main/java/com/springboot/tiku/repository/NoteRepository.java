package com.springboot.tiku.repository;

import com.springboot.tiku.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 笔记Repository
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {
    
    /**
     * 查询用户的所有笔记（分页）
     */
    Page<Note> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 查询用户的所有笔记（不分页）
     */
    List<Note> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 查询用户对某题的笔记
     */
    Optional<Note> findByUserIdAndQuestionId(Long userId, Long questionId);
    
    /**
     * 查询题目的所有笔记
     */
    List<Note> findByQuestionIdOrderByCreatedAtDesc(Long questionId);
    
    /**
     * 统计用户笔记数
     */
    long countByUserId(Long userId);
}


