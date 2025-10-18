package com.springboot.tiku.repository;

import com.springboot.tiku.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 学科Repository
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {
    
    /**
     * 根据代码查找学科
     */
    Optional<Subject> findByCode(String code);
    
    /**
     * 查询所有启用的学科，按排序号排序
     */
    List<Subject> findByStatusOrderBySortOrderAsc(Integer status);
    
    /**
     * 检查学科代码是否存在
     */
    boolean existsByCode(String code);
}




