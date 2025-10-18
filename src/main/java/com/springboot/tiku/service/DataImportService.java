package com.springboot.tiku.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.tiku.dto.question.ImportResultDTO;
import com.springboot.tiku.dto.question.SmartImportRequest;
import com.springboot.tiku.entity.Chapter;
import com.springboot.tiku.entity.Question;
import com.springboot.tiku.entity.Subject;
import com.springboot.tiku.repository.ChapterRepository;
import com.springboot.tiku.repository.QuestionRepository;
import com.springboot.tiku.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据导入服务
 * 用于从HTML文件导入马原题库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataImportService {
    
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 从HTML文件导入马原题库
     */
    @Transactional
    public Map<String, Object> importMayuanQuestions(String htmlFilePath) {
        try {
            log.info("开始导入马原题库，文件：{}", htmlFilePath);
            
            // 1. 创建学科
            Subject subject = createOrGetSubject();
            
            // 2. 读取HTML文件
            String html = Files.readString(Path.of(htmlFilePath));
            
            // 3. 提取questions数组
            String questionsJson = extractQuestionsJson(html);
            
            // 4. 解析题目数据
            JSONArray questionsArray = JSON.parseArray(questionsJson);
            
            // 5. 统计章节
            Map<String, Chapter> chapterMap = createChapters(subject, questionsArray);
            
            // 6. 导入题目
            int importedCount = importQuestions(subject, chapterMap, questionsArray);
            
            // 7. 更新统计
            updateStatistics(subject);
            
            Map<String, Object> result = new HashMap<>();
            result.put("subject", subject.getName());
            result.put("totalQuestions", importedCount);
            result.put("chapters", chapterMap.size());
            
            log.info("马原题库导入完成，共导入{}道题目，{}个章节", importedCount, chapterMap.size());
            return result;
            
        } catch (Exception e) {
            log.error("导入题库失败", e);
            throw new RuntimeException("导入题库失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 创建或获取学科
     */
    private Subject createOrGetSubject() {
        return subjectRepository.findByCode("mayuan").orElseGet(() -> {
            Subject subject = new Subject();
            subject.setName("马克思主义基本原理");
            subject.setCode("mayuan");
            subject.setDescription("马克思主义基本原理学习题库，包含单选题和简答题");
            subject.setSortOrder(1);
            subject.setStatus(1);
            subject.setQuestionCount(0);
            return subjectRepository.save(subject);
        });
    }
    
    /**
     * 从HTML中提取questions数组的JSON
     */
    private String extractQuestionsJson(String html) {
        // 查找 const questions = [ ... ];
        Pattern pattern = Pattern.compile("const questions = \\[(.*?)\\];", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        
        if (matcher.find()) {
            String questionsContent = matcher.group(1);
            // 处理JavaScript对象为JSON格式
            questionsContent = processJavaScriptToJson(questionsContent);
            return "[" + questionsContent + "]";
        }
        
        throw new RuntimeException("未找到题目数据");
    }
    
    /**
     * 将JavaScript对象转换为JSON格式
     */
    private String processJavaScriptToJson(String js) {
        // 移除注释
        js = js.replaceAll("//[^\\n]*", "");
        
        // 将单引号替换为双引号（但要注意字符串内部的单引号）
        // 将 key: value 转换为 "key": value
        js = js.replaceAll("(\\w+):", "\"$1\":");
        
        // 处理尾部逗号
        js = js.replaceAll(",\\s*}", "}");
        js = js.replaceAll(",\\s*]", "]");
        
        return js;
    }
    
    /**
     * 创建章节
     */
    private Map<String, Chapter> createChapters(Subject subject, JSONArray questionsArray) {
        Map<String, Chapter> chapterMap = new HashMap<>();
        Set<String> unitNames = new HashSet<>();
        
        // 收集所有章节名称
        for (int i = 0; i < questionsArray.size(); i++) {
            JSONObject question = questionsArray.getJSONObject(i);
            String unit = question.getString("unit");
            if (unit != null && !unit.isEmpty()) {
                unitNames.add(unit);
            }
        }
        
        // 创建章节
        int sortOrder = 1;
        for (String unitName : unitNames) {
            Chapter chapter = chapterRepository.findBySubjectIdAndName(subject.getId(), unitName)
                    .orElseGet(() -> {
                        Chapter ch = new Chapter();
                        ch.setSubjectId(subject.getId());
                        ch.setName(unitName);
                        ch.setParentId(0L);
                        ch.setLevel(1);
                        ch.setSortOrder(sortOrder);
                        ch.setQuestionCount(0);
                        return chapterRepository.save(ch);
                    });
            chapterMap.put(unitName, chapter);
        }
        
        return chapterMap;
    }
    
    /**
     * 导入题目
     */
    private int importQuestions(Subject subject, Map<String, Chapter> chapterMap, JSONArray questionsArray) {
        int count = 0;
        
        for (int i = 0; i < questionsArray.size(); i++) {
            JSONObject questionData = null;
            try {
                questionData = questionsArray.getJSONObject(i);
                Question question = convertToQuestion(subject, chapterMap, questionData);
                questionRepository.save(question);
                count++;
                
                if (count % 100 == 0) {
                    log.info("已导入{}道题目", count);
                }
            } catch (Exception e) {
                log.error("导入题目失败：{}", questionData != null ? questionData.toJSONString() : "null", e);
            }
        }
        
        return count;
    }
    
    /**
     * 转换为Question实体
     */
    private Question convertToQuestion(Subject subject, Map<String, Chapter> chapterMap, JSONObject data) {
        Question question = new Question();
        
        // 基本信息
        question.setSubjectId(subject.getId());
        
        // 章节
        String unit = data.getString("unit");
        if (unit != null && chapterMap.containsKey(unit)) {
            Chapter chapter = chapterMap.get(unit);
            question.setChapterId(chapter.getId());
            
            // 自动计算序号：获取该章节当前最大序号+1
            Integer maxSerialNumber = questionRepository.findMaxSerialNumberByChapterId(chapter.getId());
            question.setSerialNumber(maxSerialNumber != null ? maxSerialNumber + 1 : 1);
        }
        
        // 题型
        String type = data.getString("type");
        if ("single".equals(type)) {
            question.setType(Question.QuestionType.SINGLE);
        } else if ("short".equals(type)) {
            question.setType(Question.QuestionType.SHORT_ANSWER);
            question.setAiGradingEnabled(true); // 简答题启用AI判题
        }
        
        // 题目内容
        question.setTitle(data.getString("question"));
        
        // 难度（默认中等）
        question.setDifficulty(Question.Difficulty.MEDIUM);
        
        // 分值（从answer中提取，如果有的话）
        String answerText = data.getString("answer");
        BigDecimal score = extractScore(answerText);
        question.setScore(score);
        
        // 选项（JSON格式存储）
        JSONArray options = data.getJSONArray("options");
        if (options != null && !options.isEmpty()) {
            try {
                question.setOptions(objectMapper.writeValueAsString(options));
            } catch (JsonProcessingException e) {
                log.error("序列化选项失败", e);
            }
        }
        
        // 答案（JSON格式存储）
        Map<String, Object> answerMap = new HashMap<>();
        if (question.getType() == Question.QuestionType.SINGLE) {
            answerMap.put("answer", data.getString("answer"));
        } else if (question.getType() == Question.QuestionType.SHORT_ANSWER) {
            // 清理答案文本（移除分值标记）
            String cleanAnswer = cleanAnswerText(answerText);
            answerMap.put("answer", cleanAnswer);
            
            // 提取关键词（简单处理）
            List<String> keywords = extractKeywords(cleanAnswer);
            answerMap.put("keywords", keywords);
        }
        try {
            question.setAnswer(objectMapper.writeValueAsString(answerMap));
        } catch (JsonProcessingException e) {
            log.error("序列化答案失败", e);
        }
        
        // 解析
        question.setAnswerAnalysis(data.getString("explanation"));
        
        // 状态
        question.setStatus(1);
        
        // 标签（根据章节添加）
        if (unit != null) {
            question.setTags(unit);
        }
        
        return question;
    }
    
    /**
     * 从答案文本中提取分值
     */
    private BigDecimal extractScore(String answerText) {
        if (answerText == null) {
            return BigDecimal.valueOf(5.0);
        }
        
        // 匹配（分值X）
        Pattern pattern = Pattern.compile("（分值\\s*(\\d+)\\s*）");
        Matcher matcher = pattern.matcher(answerText);
        
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        
        // 单选题默认2分，简答题默认5分
        return answerText.length() > 50 ? BigDecimal.valueOf(5.0) : BigDecimal.valueOf(2.0);
    }
    
    /**
     * 清理答案文本
     */
    private String cleanAnswerText(String answerText) {
        if (answerText == null) {
            return "";
        }
        // 移除分值标记
        return answerText.replaceAll("（分值\\s*\\d+\\s*）", "").trim();
    }
    
    /**
     * 提取关键词（简单实现）
     */
    private List<String> extractKeywords(String text) {
        List<String> keywords = new ArrayList<>();
        
        // 简单处理：找出常见的关键概念
        String[] commonKeywords = {
            "马克思主义", "唯物主义", "辩证法", "实践", "认识", "矛盾", 
            "生产力", "生产关系", "社会主义", "共产主义", "资本主义",
            "阶级", "革命", "改革", "发展", "人民"
        };
        
        for (String keyword : commonKeywords) {
            if (text.contains(keyword)) {
                keywords.add(keyword);
            }
        }
        
        return keywords;
    }
    
    /**
     * 更新统计信息
     */
    private void updateStatistics(Subject subject) {
        // 更新学科题目数
        long questionCount = questionRepository.countBySubjectIdAndStatus(subject.getId(), 1);
        subject.setQuestionCount((int) questionCount);
        subjectRepository.save(subject);
        
        // 更新每个章节的题目数
        List<Chapter> chapters = chapterRepository.findBySubjectIdOrderBySortOrderAsc(subject.getId());
        for (Chapter chapter : chapters) {
            long count = questionRepository.countByChapterIdAndStatus(chapter.getId(), 1);
            chapter.setQuestionCount((int) count);
            chapterRepository.save(chapter);
        }
    }
    
    /**
     * 智能批量导入（根据Excel中的章节信息自动归类）
     */
    @Transactional
    public ImportResultDTO smartImport(SmartImportRequest request) {
        ImportResultDTO result = new ImportResultDTO();
        result.setTotalCount(request.getQuestions().size());
        
        try {
            // 1. 获取学科
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new RuntimeException("学科不存在"));
            
            // 2. 章节缓存（章节名称 -> Chapter实体）
            Map<String, Chapter> chapterCache = new HashMap<>();
            
            // 3. 按章节分组统计
            Map<String, Integer> chapterQuestionCount = new HashMap<>();
            
            // 4. 遍历题目进行导入
            for (int i = 0; i < request.getQuestions().size(); i++) {
                SmartImportRequest.QuestionImportItem item = request.getQuestions().get(i);
                
                try {
                    // 获取或创建章节
                    Chapter chapter = getOrCreateChapter(subject, item, chapterCache);
                    
                    // 创建题目
                    Question question = convertToQuestion(subject, chapter, item);
                    questionRepository.save(question);
                    
                    result.incrementSuccess();
                    
                    // 统计
                    String chapterName = chapter.getName();
                    chapterQuestionCount.put(chapterName, 
                        chapterQuestionCount.getOrDefault(chapterName, 0) + 1);
                    
                } catch (Exception e) {
                    result.incrementFail();
                    result.addError(String.format("第%d行导入失败: %s", i + 1, e.getMessage()));
                    log.error("导入第{}行题目失败", i + 1, e);
                }
            }
            
            // 5. 设置章节统计
            result.setChapterStats(chapterQuestionCount);
            result.setCreatedChapters(chapterCache.size());
            
            // 6. 更新统计信息
            updateStatistics(subject);
            
            log.info("智能导入完成：成功{}，失败{}，创建章节{}", 
                    result.getSuccessCount(), result.getFailCount(), result.getCreatedChapters());
            
        } catch (Exception e) {
            log.error("智能导入失败", e);
            throw new RuntimeException("导入失败：" + e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 获取或创建章节
     */
    private Chapter getOrCreateChapter(Subject subject, SmartImportRequest.QuestionImportItem item, 
                                       Map<String, Chapter> chapterCache) {
        String chapterName = item.getChapterName();
        final String chapterOrder = item.getChapterOrder();
        
        // 如果没有章节信息，使用默认章节
        if ((chapterName == null || chapterName.trim().isEmpty()) && 
            (chapterOrder == null || chapterOrder.trim().isEmpty())) {
            chapterName = "未分类";
        }
        
        // 优先使用章节名称
        if (chapterName == null || chapterName.trim().isEmpty()) {
            chapterName = "第" + chapterOrder + "章";
        }
        
        // 从缓存获取
        if (chapterCache.containsKey(chapterName)) {
            return chapterCache.get(chapterName);
        }
        
        final String finalChapterName = chapterName;
        
        // 从数据库查找或创建
        Chapter chapter = chapterRepository.findBySubjectIdAndName(subject.getId(), finalChapterName)
                .orElseGet(() -> {
                    Chapter newChapter = new Chapter();
                    newChapter.setSubjectId(subject.getId());
                    newChapter.setName(finalChapterName);
                    newChapter.setParentId(0L);
                    newChapter.setLevel(1);
                    
                    // 设置排序号
                    if (chapterOrder != null && !chapterOrder.trim().isEmpty()) {
                        try {
                            newChapter.setSortOrder(Integer.parseInt(chapterOrder));
                        } catch (NumberFormatException e) {
                            // 如果不是数字，使用当前章节数量+1
                            int count = chapterRepository.findBySubjectIdOrderBySortOrderAsc(subject.getId()).size();
                            newChapter.setSortOrder(count + 1);
                        }
                    } else {
                        // 使用当前章节数量+1
                        int count = chapterRepository.findBySubjectIdOrderBySortOrderAsc(subject.getId()).size();
                        newChapter.setSortOrder(count + 1);
                    }
                    
                    newChapter.setQuestionCount(0);
                    return chapterRepository.save(newChapter);
                });
        
        chapterCache.put(finalChapterName, chapter);
        return chapter;
    }
    
    /**
     * 转换导入项为Question实体
     */
    private Question convertToQuestion(Subject subject, Chapter chapter, SmartImportRequest.QuestionImportItem item) {
        Question question = new Question();
        
        question.setSubjectId(subject.getId());
        question.setChapterId(chapter.getId());
        
        // 自动计算序号：获取该章节当前最大序号+1（每个章节独立编号）
        Integer maxSerialNumber = questionRepository.findMaxSerialNumberByChapterId(chapter.getId());
        question.setSerialNumber(maxSerialNumber != null ? maxSerialNumber + 1 : 1);
        
        // 题目内容
        question.setTitle(item.getContent());
        question.setContent(item.getContent());
        
        // 题型转换
        question.setType(convertQuestionType(item.getType()));
        
        // 选项
        if (item.getOptions() != null && !item.getOptions().isEmpty()) {
            List<Map<String, String>> options = new ArrayList<>();
            for (SmartImportRequest.OptionItem option : item.getOptions()) {
                Map<String, String> opt = new HashMap<>();
                opt.put("content", option.getContent());
                options.add(opt);
            }
            try {
                question.setOptions(objectMapper.writeValueAsString(options));
            } catch (JsonProcessingException e) {
                log.error("序列化选项失败", e);
            }
        }
        
        // 答案
        Map<String, Object> answerMap = new HashMap<>();
        answerMap.put("answer", item.getCorrectAnswer());
        try {
            question.setAnswer(objectMapper.writeValueAsString(answerMap));
        } catch (JsonProcessingException e) {
            log.error("序列化答案失败", e);
        }
        
        // 解析
        question.setAnswerAnalysis(item.getExplanation());
        
        // 难度
        if (item.getDifficulty() != null) {
            question.setDifficulty(convertDifficulty(item.getDifficulty()));
        } else {
            question.setDifficulty(Question.Difficulty.MEDIUM);
        }
        
        // 分值
        if (item.getScore() != null) {
            question.setScore(BigDecimal.valueOf(item.getScore()));
        } else {
            // 根据题型设置默认分值
            if (question.getType() == Question.QuestionType.SINGLE || 
                question.getType() == Question.QuestionType.MULTIPLE ||
                question.getType() == Question.QuestionType.JUDGE) {
                question.setScore(BigDecimal.valueOf(2.0));
            } else {
                question.setScore(BigDecimal.valueOf(5.0));
            }
        }
        
        // 状态
        question.setStatus(1);
        
        // 标签
        question.setTags(chapter.getName());
        
        return question;
    }
    
    /**
     * 转换题型
     */
    private Question.QuestionType convertQuestionType(String typeStr) {
        if (typeStr == null) {
            return Question.QuestionType.SINGLE;
        }
        
        switch (typeStr.toUpperCase()) {
            case "SINGLE":
            case "SINGLE_CHOICE":
            case "单选题":
                return Question.QuestionType.SINGLE;
            case "MULTIPLE":
            case "MULTIPLE_CHOICE":
            case "多选题":
                return Question.QuestionType.MULTIPLE;
            case "JUDGE":
            case "TRUE_FALSE":
            case "判断题":
                return Question.QuestionType.JUDGE;
            case "FILL":
            case "FILL_BLANK":
            case "填空题":
                return Question.QuestionType.FILL;
            case "SHORT_ANSWER":
            case "简答题":
                return Question.QuestionType.SHORT_ANSWER;
            case "ESSAY":
            case "论述题":
                return Question.QuestionType.ESSAY;
            default:
                return Question.QuestionType.SINGLE;
        }
    }
    
    /**
     * 转换难度
     */
    private Question.Difficulty convertDifficulty(Integer difficulty) {
        if (difficulty == null || difficulty < 1 || difficulty > 5) {
            return Question.Difficulty.MEDIUM;
        }
        
        if (difficulty <= 2) {
            return Question.Difficulty.EASY;
        } else if (difficulty >= 4) {
            return Question.Difficulty.HARD;
        } else {
            return Question.Difficulty.MEDIUM;
        }
    }
}

