package com.springboot.tiku.service;

import com.springboot.tiku.common.ResultCode;
import com.springboot.tiku.dto.chapter.ChapterDTO;
import com.springboot.tiku.dto.chapter.ChapterRequest;
import com.springboot.tiku.entity.Chapter;
import com.springboot.tiku.exception.BusinessException;
import com.springboot.tiku.repository.ChapterRepository;
import com.springboot.tiku.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 章节服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterService {
    
    private final ChapterRepository chapterRepository;
    private final SubjectRepository subjectRepository;
    
    /**
     * 创建章节
     */
    @Transactional
    public ChapterDTO createChapter(ChapterRequest request) {
        // 检查学科是否存在
        if (!subjectRepository.existsById(request.getSubjectId())) {
            throw new BusinessException(ResultCode.SUBJECT_NOT_FOUND);
        }
        
        // 如果有父章节，检查父章节是否存在
        if (request.getParentId() != null && request.getParentId() > 0) {
            if (!chapterRepository.existsById(request.getParentId())) {
                throw new BusinessException(ResultCode.CHAPTER_NOT_FOUND);
            }
        }
        
        Chapter chapter = new Chapter();
        BeanUtils.copyProperties(request, chapter);
        chapter = chapterRepository.save(chapter);
        
        log.info("创建章节成功：{}", chapter.getName());
        return convertToDTO(chapter);
    }
    
    /**
     * 更新章节
     */
    @Transactional
    public ChapterDTO updateChapter(Long id, ChapterRequest request) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.CHAPTER_NOT_FOUND));
        
        BeanUtils.copyProperties(request, chapter);
        chapter.setId(id);
        chapter = chapterRepository.save(chapter);
        
        log.info("更新章节成功：{}", chapter.getName());
        return convertToDTO(chapter);
    }
    
    /**
     * 删除章节
     */
    @Transactional
    public void deleteChapter(Long id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.CHAPTER_NOT_FOUND));
        
        // 检查是否有子章节
        List<Chapter> children = chapterRepository.findByParentIdOrderBySortOrderAsc(id);
        if (!children.isEmpty()) {
            throw new BusinessException("该章节下有子章节，不能删除");
        }
        
        // 检查是否有题目
        if (chapter.getQuestionCount() > 0) {
            throw new BusinessException("该章节下有题目，不能删除");
        }
        
        chapterRepository.delete(chapter);
        log.info("删除章节成功：{}", chapter.getName());
    }
    
    /**
     * 获取章节详情
     */
    public ChapterDTO getChapterById(Long id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.CHAPTER_NOT_FOUND));
        return convertToDTO(chapter);
    }
    
    /**
     * 获取学科的所有章节（平铺）
     */
    public List<ChapterDTO> getChaptersBySubjectId(Long subjectId) {
        List<Chapter> chapters = chapterRepository.findBySubjectIdOrderBySortOrderAsc(subjectId);
        return chapters.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取学科的章节树
     */
    public List<ChapterDTO> getChapterTree(Long subjectId) {
        List<Chapter> allChapters = chapterRepository.findBySubjectIdOrderBySortOrderAsc(subjectId);
        
        // 构建树形结构
        Map<Long, List<Chapter>> childrenMap = allChapters.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() > 0)
                .collect(Collectors.groupingBy(Chapter::getParentId));
        
        // 获取顶级章节
        List<ChapterDTO> tree = allChapters.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .map(chapter -> buildChapterTree(chapter, childrenMap))
                .collect(Collectors.toList());
        
        return tree;
    }
    
    /**
     * 递归构建章节树
     */
    private ChapterDTO buildChapterTree(Chapter chapter, Map<Long, List<Chapter>> childrenMap) {
        ChapterDTO dto = convertToDTO(chapter);
        
        List<Chapter> children = childrenMap.get(chapter.getId());
        if (children != null && !children.isEmpty()) {
            List<ChapterDTO> childrenDTOs = children.stream()
                    .map(child -> buildChapterTree(child, childrenMap))
                    .collect(Collectors.toList());
            dto.setChildren(childrenDTOs);
        } else {
            dto.setChildren(new ArrayList<>());
        }
        
        return dto;
    }
    
    /**
     * 转换为DTO
     */
    private ChapterDTO convertToDTO(Chapter chapter) {
        ChapterDTO dto = new ChapterDTO();
        BeanUtils.copyProperties(chapter, dto);
        return dto;
    }
}



