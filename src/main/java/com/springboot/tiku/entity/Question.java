package com.springboot.tiku.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 题目实体（支持13种题型）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "question", indexes = {
    @Index(name = "idx_subject_id", columnList = "subject_id"),
    @Index(name = "idx_chapter_id", columnList = "chapter_id"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_difficulty", columnList = "difficulty")
})
public class Question extends BaseEntity {
    
    /**
     * 学科ID
     */
    @Column(name = "subject_id", nullable = false)
    private Long subjectId;
    
    /**
     * 章节ID
     */
    @Column(name = "chapter_id")
    private Long chapterId;
    
    /**
     * 题型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionType type;
    
    /**
     * 题目标题/题干
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;
    
    /**
     * 题目内容（富文本、材料等）
     */
    @Column(columnDefinition = "TEXT")
    private String content;
    
    /**
     * 难度等级
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty = Difficulty.MEDIUM;
    
    /**
     * 分值
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score = BigDecimal.valueOf(5.0);
    
    /**
     * 选项（JSON格式，适用于单选、多选、匹配等题型）
     * 示例：["A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"]
     */
    @Column(columnDefinition = "JSON")
    private String options;
    
    /**
     * 答案（JSON格式，支持各种题型的答案）
     * 单选：{"answer": "A"}
     * 多选：{"answer": ["A", "B", "C"]}
     * 判断：{"answer": true}
     * 填空：{"answer": ["答案1", "答案2"]}
     * 主观题：{"answer": "参考答案", "keywords": ["关键词1", "关键词2"]}
     */
    @Column(nullable = false, columnDefinition = "JSON")
    private String answer;
    
    /**
     * 答案解析
     */
    @Column(columnDefinition = "TEXT")
    private String answerAnalysis;
    
    /**
     * 是否启用AI判题
     */
    @Column(nullable = false)
    private Boolean aiGradingEnabled = false;
    
    /**
     * AI判题配置（JSON格式）
     * {
     *   "model": "Qwen/Qwen2.5-7B-Instruct",
     *   "temperature": 0.3,
     *   "scoringDimensions": [...]
     * }
     */
    @Column(columnDefinition = "JSON")
    private String aiGradingConfig;
    
    /**
     * 评分标准（JSON格式，用于主观题）
     * [
     *   {"dimension": "要点完整性", "score": 40, "description": "..."},
     *   {"dimension": "准确性", "score": 30, "description": "..."}
     * ]
     */
    @Column(columnDefinition = "JSON")
    private String scoringCriteria;
    
    /**
     * 参考关键词（用于主观题判题）
     */
    @Column(columnDefinition = "JSON")
    private String referenceKeywords;
    
    /**
     * 使用次数
     */
    @Column(nullable = false)
    private Integer useCount = 0;
    
    /**
     * 答对次数
     */
    @Column(nullable = false)
    private Integer correctCount = 0;
    
    /**
     * 答错次数
     */
    @Column(nullable = false)
    private Integer wrongCount = 0;
    
    /**
     * 题目序号（在学科内的顺序号）
     */
    @Column(name = "serial_number")
    private Integer serialNumber;
    
    /**
     * 标签（逗号分隔）
     */
    @Column(length = 500)
    private String tags;
    
    /**
     * 知识点（逗号分隔）
     */
    @Column(length = 500)
    private String knowledgePoints;
    
    /**
     * 创建者ID
     */
    @Column(name = "creator_id")
    private Long creatorId;
    
    /**
     * 状态：1-启用, 0-禁用
     */
    @Column(nullable = false)
    private Integer status = 1;
    
    /**
     * 题型枚举
     */
    public enum QuestionType {
        SINGLE,              // 单选题
        MULTIPLE,            // 多选题
        JUDGE,              // 判断题
        FILL,               // 填空题
        SHORT_ANSWER,       // 简答题
        ESSAY,              // 论述题
        CASE_ANALYSIS,      // 案例分析题
        MATERIAL_ANALYSIS,  // 材料分析题
        CALCULATION,        // 计算题
        ORDERING,           // 排序题
        MATCHING,           // 匹配题
        COMPOSITE,          // 组合题
        PROGRAMMING         // 编程题
    }
    
    /**
     * 难度枚举
     */
    public enum Difficulty {
        EASY,    // 简单
        MEDIUM,  // 中等
        HARD     // 困难
    }
}




