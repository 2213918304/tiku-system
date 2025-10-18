package com.springboot.tiku.service;

import com.springboot.tiku.common.ResultCode;
import com.springboot.tiku.dto.favorite.FavoriteDTO;
import com.springboot.tiku.dto.favorite.FavoriteStatsDTO;
import com.springboot.tiku.entity.Favorite;
import com.springboot.tiku.entity.Question;
import com.springboot.tiku.entity.Subject;
import com.springboot.tiku.entity.Chapter;
import com.springboot.tiku.exception.BusinessException;
import com.springboot.tiku.repository.FavoriteRepository;
import com.springboot.tiku.repository.QuestionRepository;
import com.springboot.tiku.repository.SubjectRepository;
import com.springboot.tiku.repository.ChapterRepository;
import com.springboot.tiku.repository.AnswerRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {
    
    private final FavoriteRepository favoriteRepository;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final AnswerRecordRepository answerRecordRepository;
    
    /**
     * 收藏题目
     */
    @Transactional
    public Favorite addFavorite(Long userId, Long questionId, String category, String remark) {
        // 检查题目是否存在
        if (!questionRepository.existsById(questionId)) {
            throw new BusinessException(ResultCode.QUESTION_NOT_FOUND);
        }
        
        // 检查是否已收藏
        if (favoriteRepository.existsByUserIdAndQuestionId(userId, questionId)) {
            throw new BusinessException("该题目已收藏");
        }
        
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setQuestionId(questionId);
        favorite.setCategory(category);
        favorite.setRemark(remark);
        
        favorite = favoriteRepository.save(favorite);
        log.info("用户{}收藏题目{}", userId, questionId);
        
        return favorite;
    }
    
    /**
     * 取消收藏
     */
    @Transactional
    public void removeFavorite(Long userId, Long questionId) {
        favoriteRepository.deleteByUserIdAndQuestionId(userId, questionId);
        log.info("用户{}取消收藏题目{}", userId, questionId);
    }
    
    /**
     * 获取用户的收藏列表（返回DTO）
     */
    public Page<FavoriteDTO> getUserFavorites(Long userId, Pageable pageable) {
        Page<Favorite> favoritePage = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        List<FavoriteDTO> favoriteDTOs = favoritePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(favoriteDTOs, pageable, favoritePage.getTotalElements());
    }
    
    /**
     * 获取用户的所有收藏列表（不分页）
     */
    public List<FavoriteDTO> getAllUserFavorites(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return favorites.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据分类获取收藏
     */
    public List<Favorite> getFavoritesByCategory(Long userId, String category) {
        return favoriteRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(userId, category);
    }
    
    /**
     * 检查是否已收藏
     */
    public boolean isFavorite(Long userId, Long questionId) {
        return favoriteRepository.existsByUserIdAndQuestionId(userId, questionId);
    }
    
    /**
     * 批量检查收藏状态
     * @param userId 用户ID
     * @param questionIds 题目ID列表
     * @return 题目ID -> Favorite 的映射
     */
    public Map<Long, Favorite> checkBatchFavorites(Long userId, List<Long> questionIds) {
        Map<Long, Favorite> result = new HashMap<>();
        
        if (questionIds == null || questionIds.isEmpty()) {
            return result;
        }
        
        List<Favorite> favorites = favoriteRepository.findByUserIdAndQuestionIdIn(userId, questionIds);
        for (Favorite favorite : favorites) {
            result.put(favorite.getQuestionId(), favorite);
        }
        
        return result;
    }
    
    /**
     * 获取收藏统计信息
     */
    public FavoriteStatsDTO getFavoriteStats(Long userId) {
        // 总收藏数
        Long totalCount = favoriteRepository.countByUserId(userId);
        
        // 本周新增（最近7天）
        LocalDateTime weekAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        Long weekCount = favoriteRepository.countByUserIdAndCreatedAtAfter(userId, weekAgo);
        
        // 已练习数量（收藏的题目中，有答题记录的数量）
        List<Long> questionIds = favoriteRepository.findQuestionIdsByUserId(userId);
        Long practicedCount = 0L;
        if (!questionIds.isEmpty()) {
            practicedCount = answerRecordRepository.countDistinctQuestionsByUserIdAndQuestionIdIn(userId, questionIds);
        }
        
        return new FavoriteStatsDTO(totalCount, weekCount, practicedCount);
    }
    
    /**
     * 转换Favorite为FavoriteDTO
     */
    private FavoriteDTO convertToDTO(Favorite favorite) {
        FavoriteDTO dto = new FavoriteDTO();
        dto.setFavoriteId(favorite.getId());
        dto.setQuestionId(favorite.getQuestionId());
        dto.setCategory(favorite.getCategory());
        dto.setRemark(favorite.getRemark());
        dto.setFavoriteAt(favorite.getCreatedAt());
        
        // 获取题目信息
        Question question = questionRepository.findById(favorite.getQuestionId()).orElse(null);
        if (question != null) {
            dto.setType(question.getType() != null ? question.getType().name() : null);
            dto.setDifficulty(question.getDifficulty() != null ? question.getDifficulty().name() : null);
            dto.setTitle(question.getTitle());
            dto.setContent(question.getContent() != null ? question.getContent() : question.getTitle());
            dto.setOptions(question.getOptions()); // 直接返回JSON字符串，让前端解析
            dto.setAnswer(question.getAnswer());
            dto.setExplanation(question.getAnswerAnalysis());
            
            // 获取学科信息
            if (question.getSubjectId() != null) {
                dto.setSubjectId(question.getSubjectId());
                Subject subject = subjectRepository.findById(question.getSubjectId()).orElse(null);
                if (subject != null) {
                    dto.setSubjectName(subject.getName());
                }
            }
            
            // 获取章节信息
            if (question.getChapterId() != null) {
                dto.setChapterId(question.getChapterId());
                Chapter chapter = chapterRepository.findById(question.getChapterId()).orElse(null);
                if (chapter != null) {
                    dto.setChapterName(chapter.getName());
                }
            }
            
            // 获取练习次数
            Integer practiceCount = answerRecordRepository.countByUserIdAndQuestionId(favorite.getUserId(), question.getId());
            dto.setPracticeCount(practiceCount);
        }
        
        return dto;
    }
}



