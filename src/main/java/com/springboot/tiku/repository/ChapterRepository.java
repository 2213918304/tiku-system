package com.springboot.tiku.repository;

import com.springboot.tiku.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 章节Repository
 */
@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long>, JpaSpecificationExecutor<Chapter> {
    
    /**
     * 根据学科ID查询所有章节，按排序号排序
     */
    List<Chapter> findBySubjectIdOrderBySortOrderAsc(Long subjectId);
    
    /**
     * 根据学科ID和父章节ID查询章节
     */
    List<Chapter> findBySubjectIdAndParentIdOrderBySortOrderAsc(Long subjectId, Long parentId);
    
    /**
     * 根据父章节ID查询子章节
     */
    List<Chapter> findByParentIdOrderBySortOrderAsc(Long parentId);
    
    /**
     * 根据学科ID和章节名称查找
     */
    Optional<Chapter> findBySubjectIdAndName(Long subjectId, String name);
}


