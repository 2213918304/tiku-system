package com.springboot.tiku.service;

import com.springboot.tiku.common.ResultCode;
import com.springboot.tiku.dto.subject.SubjectDTO;
import com.springboot.tiku.dto.subject.SubjectRequest;
import com.springboot.tiku.entity.Subject;
import com.springboot.tiku.entity.User;
import com.springboot.tiku.exception.BusinessException;
import com.springboot.tiku.repository.AnswerRecordRepository;
import com.springboot.tiku.repository.ChapterRepository;
import com.springboot.tiku.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学科服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectService {
    
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final AnswerRecordRepository answerRecordRepository;
    
    /**
     * 创建学科
     */
    @Transactional
    public SubjectDTO createSubject(SubjectRequest request) {
        // 检查学科代码是否重复
        if (request.getCode() != null && subjectRepository.existsByCode(request.getCode())) {
            throw new BusinessException(ResultCode.SUBJECT_CODE_EXISTS);
        }
        
        Subject subject = new Subject();
        BeanUtils.copyProperties(request, subject);
        subject = subjectRepository.save(subject);
        
        log.info("创建学科成功：{}", subject.getName());
        return convertToDTO(subject);
    }
    
    /**
     * 更新学科
     */
    @Transactional
    public SubjectDTO updateSubject(Long id, SubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.SUBJECT_NOT_FOUND));
        
        // 检查学科代码是否重复（排除自己）
        if (request.getCode() != null && !request.getCode().equals(subject.getCode())) {
            if (subjectRepository.existsByCode(request.getCode())) {
                throw new BusinessException(ResultCode.SUBJECT_CODE_EXISTS);
            }
        }
        
        BeanUtils.copyProperties(request, subject);
        subject.setId(id);
        subject = subjectRepository.save(subject);
        
        log.info("更新学科成功：{}", subject.getName());
        return convertToDTO(subject);
    }
    
    /**
     * 删除学科
     */
    @Transactional
    public void deleteSubject(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.SUBJECT_NOT_FOUND));
        
        // 检查是否有题目，如有则不允许删除
        if (subject.getQuestionCount() > 0) {
            throw new BusinessException("该学科下有题目，不能删除");
        }
        
        subjectRepository.delete(subject);
        log.info("删除学科成功：{}", subject.getName());
    }
    
    /**
     * 获取学科详情
     */
    public SubjectDTO getSubjectById(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.SUBJECT_NOT_FOUND));
        return convertToDTO(subject);
    }
    
    /**
     * 获取所有启用的学科（带统计信息）
     */
    public List<SubjectDTO> getAllEnabledSubjects() {
        List<Subject> subjects = subjectRepository.findByStatusOrderBySortOrderAsc(1);
        Long currentUserId = getCurrentUserId();
        
        return subjects.stream()
                .map(subject -> convertToDTOWithStats(subject, currentUserId))
                .collect(Collectors.toList());
    }
    
    /**
     * 分页查询学科
     */
    public Page<SubjectDTO> getSubjects(Pageable pageable) {
        return subjectRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * 更新学科状态
     */
    @Transactional
    public void updateSubjectStatus(Long id, Integer status) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.SUBJECT_NOT_FOUND));
        subject.setStatus(status);
        subjectRepository.save(subject);
        log.info("更新学科状态：{} -> {}", subject.getName(), status);
    }
    
    /**
     * 转换为DTO
     */
    private SubjectDTO convertToDTO(Subject subject) {
        SubjectDTO dto = new SubjectDTO();
        BeanUtils.copyProperties(subject, dto);
        return dto;
    }
    
    /**
     * 转换为DTO（带统计信息）
     */
    private SubjectDTO convertToDTOWithStats(Subject subject, Long userId) {
        SubjectDTO dto = new SubjectDTO();
        BeanUtils.copyProperties(subject, dto);
        
        // 统计章节数
        long chapterCount = chapterRepository.findBySubjectIdOrderBySortOrderAsc(subject.getId()).size();
        dto.setChapterCount((int) chapterCount);
        
        // 统计用户已答题数（如果用户已登录）
        if (userId != null) {
            long answeredCount = answerRecordRepository.countByUserIdAndQuestionSubjectId(userId, subject.getId());
            dto.setAnsweredCount((int) answeredCount);
        } else {
            dto.setAnsweredCount(0);
        }
        
        return dto;
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() 
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof User) {
                    return ((User) principal).getId();
                }
            }
        } catch (Exception e) {
            log.debug("获取当前用户ID失败: {}", e.getMessage());
        }
        return null;
    }
}



