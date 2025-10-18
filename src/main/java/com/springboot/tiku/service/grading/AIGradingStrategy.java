package com.springboot.tiku.service.grading;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.springboot.tiku.dto.grading.GradingResult;
import com.springboot.tiku.entity.Question;
import com.springboot.tiku.service.ai.SiliconFlowAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * AI判题策略（主观题）
 * 支持：简答题、论述题、案例分析题、材料分析题
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AIGradingStrategy implements GradingStrategy {
    
    private final SiliconFlowAIService aiService;
    
    @Value("${ai.grading.confidence-threshold:0.75}")
    private Double confidenceThreshold;
    
    private static final Set<Question.QuestionType> SUPPORTED_TYPES = Set.of(
            Question.QuestionType.SHORT_ANSWER,
            Question.QuestionType.ESSAY,
            Question.QuestionType.CASE_ANALYSIS,
            Question.QuestionType.MATERIAL_ANALYSIS
    );
    
    @Override
    public GradingResult grade(Question question, Object userAnswer) {
        try {
            // 提取学生答案
            String studentAnswer = extractStudentAnswer(userAnswer);
            
            if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
                return buildZeroScoreResult(question);
            }
            
            // 提取参考答案
            String referenceAnswer = extractReferenceAnswer(question.getAnswer());
            
            // 构建Prompt
            String prompt = buildPrompt(question, referenceAnswer, studentAnswer);
            
            // 调用AI判题
            String aiResponse = aiService.chat(getSystemPrompt(question), prompt);
            
            // 解析AI响应
            GradingResult result = parseAIResponse(aiResponse, question);
            
            // 判断是否需要人工复核
            if (result.getAiFeedback() != null) {
                BigDecimal confidence = result.getAiFeedback().getConfidence();
                if (confidence != null && confidence.doubleValue() < confidenceThreshold) {
                    result.setNeedManualReview(true);
                }
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("AI判题失败：questionId={}, error={}", question.getId(), e.getMessage(), e);
            // AI判题失败，标记为需要人工复核
            return GradingResult.builder()
                    .isCorrect(false)
                    .score(BigDecimal.ZERO)
                    .gradingType("AI")
                    .needManualReview(true)
                    .totalScore(question.getScore())
                    .build();
        }
    }
    
    @Override
    public boolean supports(Question.QuestionType questionType) {
        return SUPPORTED_TYPES.contains(questionType);
    }
    
    /**
     * 获取系统提示词
     */
    private String getSystemPrompt(Question question) {
        String subjectType = getSubjectTypeName(question.getType());
        return String.format("""
                你是一位经验丰富的教师，专门负责批改学生的%s答案。
                请你客观、公正地评分，并给出详细的评语。
                你的评分应该准确、合理，评语应该具有建设性。
                请严格按照JSON格式返回评分结果。
                """, subjectType);
    }
    
    /**
     * 构建判题Prompt
     */
    private String buildPrompt(Question question, String referenceAnswer, String studentAnswer) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("【题目】\n").append(question.getTitle()).append("\n\n");
        
        if (question.getContent() != null && !question.getContent().isEmpty()) {
            prompt.append("【题目内容/材料】\n").append(question.getContent()).append("\n\n");
        }
        
        prompt.append("【参考答案】\n").append(referenceAnswer).append("\n\n");
        
        prompt.append("【学生答案】\n").append(studentAnswer).append("\n\n");
        
        prompt.append("【评分标准】\n");
        prompt.append("总分：").append(question.getScore()).append("分\n");
        
        if (question.getScoringCriteria() != null) {
            prompt.append(parseScoringCriteria(question.getScoringCriteria())).append("\n");
        } else {
            // 默认评分标准
            BigDecimal score = question.getScore();
            prompt.append("1. 要点完整性（").append(score.multiply(BigDecimal.valueOf(0.4)).setScale(1, RoundingMode.HALF_UP)).append("分）\n");
            prompt.append("2. 准确性（").append(score.multiply(BigDecimal.valueOf(0.3)).setScale(1, RoundingMode.HALF_UP)).append("分）\n");
            prompt.append("3. 逻辑性（").append(score.multiply(BigDecimal.valueOf(0.2)).setScale(1, RoundingMode.HALF_UP)).append("分）\n");
            prompt.append("4. 表达规范性（").append(score.multiply(BigDecimal.valueOf(0.1)).setScale(1, RoundingMode.HALF_UP)).append("分）\n\n");
        }
        
        prompt.append("【任务要求】\n");
        prompt.append("请按照以下JSON格式返回评分结果：\n");
        prompt.append("""
                {
                  "score": 实际得分（数字）,
                  "totalScore": 总分,
                  "confidence": 置信度（0-1之间的小数）,
                  "scoreDetails": [
                    {
                      "dimension": "维度名称",
                      "score": 得分,
                      "maxScore": 满分,
                      "reason": "评分理由"
                    }
                  ],
                  "strengths": ["优点1", "优点2"],
                  "weaknesses": ["不足1", "不足2"],
                  "suggestions": "改进建议",
                  "comment": "总体评语"
                }
                
                请直接返回JSON，不要包含其他文字。
                """);
        
        return prompt.toString();
    }
    
    /**
     * 解析AI响应
     */
    private GradingResult parseAIResponse(String aiResponse, Question question) {
        try {
            // 提取JSON部分（AI可能返回带解释的文本）
            String jsonStr = extractJSON(aiResponse);
            JSONObject json = JSON.parseObject(jsonStr);
            
            // 解析分项得分
            List<GradingResult.ScoreDetail> scoreDetails = new ArrayList<>();
            JSONArray detailsArray = json.getJSONArray("scoreDetails");
            if (detailsArray != null) {
                for (int i = 0; i < detailsArray.size(); i++) {
                    JSONObject detail = detailsArray.getJSONObject(i);
                    scoreDetails.add(GradingResult.ScoreDetail.builder()
                            .dimension(detail.getString("dimension"))
                            .score(detail.getBigDecimal("score"))
                            .maxScore(detail.getBigDecimal("maxScore"))
                            .reason(detail.getString("reason"))
                            .build());
                }
            }
            
            // 解析优点和不足
            List<String> strengths = json.getJSONArray("strengths") != null 
                    ? json.getJSONArray("strengths").toList(String.class) 
                    : new ArrayList<>();
            List<String> weaknesses = json.getJSONArray("weaknesses") != null 
                    ? json.getJSONArray("weaknesses").toList(String.class) 
                    : new ArrayList<>();
            
            // 构建AI反馈
            GradingResult.AIFeedback aiFeedback = GradingResult.AIFeedback.builder()
                    .model("SiliconFlow")
                    .confidence(json.getBigDecimal("confidence"))
                    .scoreDetails(scoreDetails)
                    .strengths(strengths)
                    .weaknesses(weaknesses)
                    .suggestions(json.getString("suggestions"))
                    .comment(json.getString("comment"))
                    .build();
            
            // 计算得分率，判断是否"正确"（主观题得分率 >= 50% 算正确，即一半以上）
            BigDecimal actualScore = json.getBigDecimal("score");
            BigDecimal totalScore = question.getScore();
            boolean isCorrect = false;
            if (totalScore != null && totalScore.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal scoreRate = actualScore.divide(totalScore, 4, RoundingMode.HALF_UP);
                isCorrect = scoreRate.compareTo(BigDecimal.valueOf(0.5)) >= 0; // 50%及格线，一半以上算正确
            }
            
            return GradingResult.builder()
                    .isCorrect(isCorrect)
                    .score(actualScore)
                    .totalScore(totalScore)
                    .gradingType("AI")
                    .correctAnswer(question.getAnswer())
                    .answerAnalysis(question.getAnswerAnalysis())
                    .aiFeedback(aiFeedback)
                    .needManualReview(false)
                    .build();
            
        } catch (Exception e) {
            log.error("解析AI响应失败：{}", aiResponse, e);
            throw new RuntimeException("AI响应解析失败");
        }
    }
    
    /**
     * 提取学生答案
     */
    private String extractStudentAnswer(Object userAnswer) {
        if (userAnswer instanceof String) {
            return (String) userAnswer;
        }
        
        JSONObject json = parseJSON(userAnswer);
        return json.getString("answer");
    }
    
    /**
     * 提取参考答案
     */
    private String extractReferenceAnswer(Object answer) {
        JSONObject json = parseJSON(answer);
        return json.getString("answer");
    }
    
    /**
     * 解析评分标准
     */
    private String parseScoringCriteria(Object criteria) {
        try {
            JSONArray array = JSON.parseArray(JSON.toJSONString(criteria));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.size(); i++) {
                JSONObject item = array.getJSONObject(i);
                sb.append(i + 1).append(". ")
                        .append(item.getString("dimension"))
                        .append("（").append(item.get("score")).append("分）：")
                        .append(item.getString("description"))
                        .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * 提取JSON字符串
     */
    private String extractJSON(String text) {
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }
    
    /**
     * 解析JSON
     */
    private JSONObject parseJSON(Object obj) {
        if (obj == null) {
            return new JSONObject();
        }
        
        if (obj instanceof String) {
            String str = (String) obj;
            // 如果是简单字符串（如 "A", "B"），包装成 {"answer": "A"} 格式
            if (!str.trim().startsWith("{") && !str.trim().startsWith("[")) {
                JSONObject result = new JSONObject();
                result.put("answer", str);
                return result;
            }
            return JSON.parseObject(str);
        } else if (obj instanceof JSONObject) {
            return (JSONObject) obj;
        } else {
            return JSON.parseObject(JSON.toJSONString(obj));
        }
    }
    
    /**
     * 获取题型名称
     */
    private String getSubjectTypeName(Question.QuestionType type) {
        return switch (type) {
            case SHORT_ANSWER -> "简答题";
            case ESSAY -> "论述题";
            case CASE_ANALYSIS -> "案例分析题";
            case MATERIAL_ANALYSIS -> "材料分析题";
            default -> "主观题";
        };
    }
    
    /**
     * 构建零分结果
     */
    private GradingResult buildZeroScoreResult(Question question) {
        return GradingResult.builder()
                .isCorrect(false)
                .score(BigDecimal.ZERO)
                .totalScore(question.getScore())
                .gradingType("AI")
                .correctAnswer(question.getAnswer())
                .answerAnalysis(question.getAnswerAnalysis())
                .aiFeedback(GradingResult.AIFeedback.builder()
                        .comment("未作答")
                        .confidence(BigDecimal.ONE)
                        .build())
                .build();
    }
}

