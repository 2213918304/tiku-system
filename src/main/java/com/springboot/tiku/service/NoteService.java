package com.springboot.tiku.service;

import com.springboot.tiku.common.ResultCode;
import com.springboot.tiku.dto.note.NoteDTO;
import com.springboot.tiku.entity.Chapter;
import com.springboot.tiku.entity.Note;
import com.springboot.tiku.entity.Question;
import com.springboot.tiku.entity.Subject;
import com.springboot.tiku.exception.BusinessException;
import com.springboot.tiku.repository.ChapterRepository;
import com.springboot.tiku.repository.NoteRepository;
import com.springboot.tiku.repository.QuestionRepository;
import com.springboot.tiku.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 笔记服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {
    
    private final NoteRepository noteRepository;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    
    /**
     * 添加笔记
     */
    @Transactional
    public Note addNote(Long userId, Long questionId, Long subjectId, String title, String content) {
        // 如果关联题目，检查题目是否存在
        if (questionId != null && !questionRepository.existsById(questionId)) {
            throw new BusinessException(ResultCode.QUESTION_NOT_FOUND);
        }
        
        Note note = new Note();
        note.setUserId(userId);
        note.setQuestionId(questionId);
        note.setSubjectId(subjectId);
        note.setTitle(title);
        note.setContent(content);
        
        note = noteRepository.save(note);
        log.info("用户{}添加笔记，题目ID：{}", userId, questionId);
        return note;
    }
    
    /**
     * 更新笔记
     */
    @Transactional
    public Note updateNote(Long id, Long userId, Long subjectId, String title, String content) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("笔记不存在"));
        
        // 检查权限
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        
        note.setTitle(title);
        note.setContent(content);
        note.setSubjectId(subjectId);
        note = noteRepository.save(note);
        
        log.info("用户{}更新笔记{}", userId, id);
        return note;
    }
    
    /**
     * 删除笔记
     */
    @Transactional
    public void deleteNote(Long id, Long userId) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("笔记不存在"));
        
        // 检查权限
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        
        noteRepository.delete(note);
        log.info("用户{}删除笔记{}", userId, id);
    }
    
    /**
     * 获取用户的笔记列表（返回DTO）
     */
    public Page<NoteDTO> getUserNotes(Long userId, Long subjectId, String keyword, Pageable pageable) {
        // 构建查询条件
        Specification<Note> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            // 用户ID条件
            predicates.add(cb.equal(root.get("userId"), userId));
            
            // 学科筛选
            if (subjectId != null) {
                predicates.add(cb.equal(root.get("subjectId"), subjectId));
            }
            
            // 关键词搜索（标题或内容）
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword + "%";
                predicates.add(cb.or(
                    cb.like(root.get("title"), likePattern),
                    cb.like(root.get("content"), likePattern)
                ));
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        
        Page<Note> notePage = noteRepository.findAll(spec, pageable);
        
        List<NoteDTO> noteDTOs = notePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(noteDTOs, pageable, notePage.getTotalElements());
    }
    
    /**
     * 获取用户的所有笔记（不分页）
     */
    public List<NoteDTO> getAllUserNotes(Long userId) {
        List<Note> notes = noteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户对某题的笔记
     */
    public Note getNoteByQuestion(Long userId, Long questionId) {
        return noteRepository.findByUserIdAndQuestionId(userId, questionId)
                .orElse(null);
    }
    
    /**
     * 转换Note为NoteDTO
     */
    private NoteDTO convertToDTO(Note note) {
        NoteDTO dto = new NoteDTO();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setUserId(note.getUserId());
        dto.setQuestionId(note.getQuestionId());
        dto.setSubjectId(note.getSubjectId());
        dto.setCreateTime(note.getCreatedAt());
        dto.setUpdateTime(note.getUpdatedAt());
        
        // 获取题目信息
        if (note.getQuestionId() != null) {
            Question question = questionRepository.findById(note.getQuestionId()).orElse(null);
            if (question != null) {
                dto.setQuestionTitle(question.getTitle());
                
                // 从题目获取学科和章节信息（如果笔记本身没有）
                if (dto.getSubjectId() == null && question.getSubjectId() != null) {
                    dto.setSubjectId(question.getSubjectId());
                }
                
                if (question.getChapterId() != null) {
                    dto.setChapterId(question.getChapterId());
                    Chapter chapter = chapterRepository.findById(question.getChapterId()).orElse(null);
                    if (chapter != null) {
                        dto.setChapterName(chapter.getName());
                    }
                }
            }
        }
        
        // 获取学科名称
        if (dto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(dto.getSubjectId()).orElse(null);
            if (subject != null) {
                dto.setSubjectName(subject.getName());
            }
        }
        
        return dto;
    }
}



