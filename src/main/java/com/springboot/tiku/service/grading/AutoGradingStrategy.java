package com.springboot.tiku.service.grading;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.springboot.tiku.dto.grading.GradingResult;
import com.springboot.tiku.entity.Question;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自动判题策略（客观题）
 * 支持：单选题、多选题、判断题、填空题、排序题、匹配题
 */
@Slf4j
@Component
public class AutoGradingStrategy implements GradingStrategy {
    
    private static final Set<Question.QuestionType> SUPPORTED_TYPES = Set.of(
            Question.QuestionType.SINGLE,
            Question.QuestionType.MULTIPLE,
            Question.QuestionType.JUDGE,
            Question.QuestionType.FILL,
            Question.QuestionType.ORDERING,
            Question.QuestionType.MATCHING
    );
    
    @Override
    public GradingResult grade(Question question, Object userAnswer) {
        try {
            boolean isCorrect = false;
            BigDecimal score = BigDecimal.ZERO;
            
            switch (question.getType()) {
                case SINGLE:
                    isCorrect = gradeSingleChoice(question, userAnswer);
                    break;
                case MULTIPLE:
                    isCorrect = gradeMultipleChoice(question, userAnswer);
                    break;
                case JUDGE:
                    isCorrect = gradeJudge(question, userAnswer);
                    break;
                case FILL:
                    isCorrect = gradeFillInBlank(question, userAnswer);
                    break;
                case ORDERING:
                    isCorrect = gradeOrdering(question, userAnswer);
                    break;
                case MATCHING:
                    isCorrect = gradeMatching(question, userAnswer);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的题型：" + question.getType());
            }
            
            // 如果正确，得满分；否则0分
            if (isCorrect) {
                score = question.getScore();
            }
            
            return GradingResult.builder()
                    .isCorrect(isCorrect)
                    .score(score)
                    .totalScore(question.getScore())
                    .gradingType("AUTO")
                    .correctAnswer(question.getAnswer())
                    .answerAnalysis(question.getAnswerAnalysis())
                    .build();
            
        } catch (Exception e) {
            log.error("自动判题失败：questionId={}, error={}", question.getId(), e.getMessage(), e);
            throw new RuntimeException("判题失败：" + e.getMessage());
        }
    }
    
    @Override
    public boolean supports(Question.QuestionType questionType) {
        return SUPPORTED_TYPES.contains(questionType);
    }
    
    /**
     * 判断单选题
     */
    private boolean gradeSingleChoice(Question question, Object userAnswer) {
        JSONObject correctAnswer = parseAnswer(question.getAnswer());
        JSONObject userAnswerObj = parseAnswer(userAnswer);
        
        String correct = correctAnswer.getString("answer");
        String user = userAnswerObj.getString("answer");
        
        return correct != null && correct.equalsIgnoreCase(user);
    }
    
    /**
     * 判断多选题
     */
    private boolean gradeMultipleChoice(Question question, Object userAnswer) {
        JSONObject correctAnswer = parseAnswer(question.getAnswer());
        JSONObject userAnswerObj = parseAnswer(userAnswer);
        
        Set<String> correctSet = new HashSet<>(correctAnswer.getJSONArray("answer").toList(String.class));
        Set<String> userSet = new HashSet<>(userAnswerObj.getJSONArray("answer").toList(String.class));
        
        return correctSet.equals(userSet);
    }
    
    /**
     * 判断判断题
     */
    private boolean gradeJudge(Question question, Object userAnswer) {
        JSONObject correctAnswer = parseAnswer(question.getAnswer());
        JSONObject userAnswerObj = parseAnswer(userAnswer);
        
        Boolean correct = correctAnswer.getBoolean("answer");
        Boolean user = userAnswerObj.getBoolean("answer");
        
        return correct != null && correct.equals(user);
    }
    
    /**
     * 判断填空题
     */
    private boolean gradeFillInBlank(Question question, Object userAnswer) {
        JSONObject correctAnswer = parseAnswer(question.getAnswer());
        JSONObject userAnswerObj = parseAnswer(userAnswer);
        
        JSONArray correctArray = correctAnswer.getJSONArray("answer");
        JSONArray userArray = userAnswerObj.getJSONArray("answer");
        
        if (correctArray.size() != userArray.size()) {
            return false;
        }
        
        // 每个空都要匹配
        for (int i = 0; i < correctArray.size(); i++) {
            String correct = correctArray.getString(i);
            String user = userArray.getString(i);
            
            // 支持多个正确答案（用|分隔）
            String[] correctAnswers = correct.split("\\|");
            boolean matched = false;
            for (String ca : correctAnswers) {
                if (normalizeString(ca).equals(normalizeString(user))) {
                    matched = true;
                    break;
                }
            }
            
            if (!matched) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 判断排序题
     */
    private boolean gradeOrdering(Question question, Object userAnswer) {
        JSONObject correctAnswer = parseAnswer(question.getAnswer());
        JSONObject userAnswerObj = parseAnswer(userAnswer);
        
        JSONArray correctArray = correctAnswer.getJSONArray("answer");
        JSONArray userArray = userAnswerObj.getJSONArray("answer");
        
        return correctArray.equals(userArray);
    }
    
    /**
     * 判断匹配题
     */
    private boolean gradeMatching(Question question, Object userAnswer) {
        JSONObject correctAnswer = parseAnswer(question.getAnswer());
        JSONObject userAnswerObj = parseAnswer(userAnswer);
        
        JSONObject correctMatching = correctAnswer.getJSONObject("answer");
        JSONObject userMatching = userAnswerObj.getJSONObject("answer");
        
        return correctMatching.equals(userMatching);
    }
    
    /**
     * 解析答案（统一处理JSON格式）
     */
    private JSONObject parseAnswer(Object answer) {
        if (answer == null) {
            return new JSONObject();
        }
        
        if (answer instanceof String) {
            String answerStr = (String) answer;
            // 如果是简单字符串（如 "A", "B"），包装成 {"answer": "A"} 格式
            if (!answerStr.trim().startsWith("{")) {
                JSONObject obj = new JSONObject();
                obj.put("answer", answerStr);
                return obj;
            }
            return JSON.parseObject(answerStr);
        } else if (answer instanceof JSONObject) {
            return (JSONObject) answer;
        } else {
            return JSON.parseObject(JSON.toJSONString(answer));
        }
    }
    
    /**
     * 标准化字符串（去除空格、转小写）
     */
    private String normalizeString(String str) {
        if (str == null) {
            return "";
        }
        return str.trim().toLowerCase();
    }
}



