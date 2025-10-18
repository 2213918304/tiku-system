package com.springboot.tiku.service;

import com.springboot.tiku.dto.statistics.*;
import com.springboot.tiku.entity.*;
import com.springboot.tiku.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    
    private final AnswerRecordRepository answerRecordRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final FavoriteRepository favoriteRepository;
    private final NoteRepository noteRepository;
    private final WrongQuestionRepository wrongQuestionRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final DailyCheckInRepository dailyCheckInRepository;
    private final QuestionRepository questionRepository;
    
    /**
     * 获取用户学习统计
     */
    public UserStatistics getUserStatistics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 统计答题数据
        long totalAnswered = answerRecordRepository.countByUserId(userId);
        long correctCount = answerRecordRepository.countByUserIdAndIsCorrect(userId, true);
        long wrongCount = answerRecordRepository.countByUserIdAndIsCorrect(userId, false);
        
        // 计算正确率
        BigDecimal accuracy = BigDecimal.ZERO;
        if (totalAnswered > 0) {
            accuracy = BigDecimal.valueOf(correctCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalAnswered), 2, RoundingMode.HALF_UP);
        }
        
        // 统计学习时长（从答题记录计算）
        Long totalStudyMinutes = calculateStudyMinutes(userId);
        
        // 统计打卡数据
        int continuousDays = calculateContinuousDays(userId);
        int totalCheckInDays = (int) dailyCheckInRepository.countByUserId(userId);
        
        // 统计收藏、笔记、错题
        long favoriteCount = favoriteRepository.countByUserId(userId);
        long noteCount = noteRepository.countByUserId(userId);
        long wrongQuestionCount = wrongQuestionRepository.countByUserIdAndRemoved(userId, false);
        
        // 统计成就（已获得的成就数）
        long achievementCount = userAchievementRepository.countByUserId(userId);
        
        // 计算积分（简单规则：正确题数 * 10）
        int totalPoints = (int) (correctCount * 10);
        
        // 获取最近学习时间
        LocalDateTime lastStudyTime = getLastStudyTime(userId);
        
        return UserStatistics.builder()
                .userId(userId)
                .username(user.getUsername())
                .realName(user.getRealName())
                .totalAnswered(totalAnswered)
                .correctCount(correctCount)
                .wrongCount(wrongCount)
                .accuracy(accuracy)
                .totalStudyMinutes(totalStudyMinutes)
                .continuousDays(continuousDays)
                .totalCheckInDays(totalCheckInDays)
                .favoriteCount(favoriteCount)
                .noteCount(noteCount)
                .wrongQuestionCount(wrongQuestionCount)
                .achievementCount((int) achievementCount)
                .totalPoints(totalPoints)
                .lastStudyTime(lastStudyTime)
                .build();
    }
    
    /**
     * 获取学科学习统计
     */
    public List<SubjectStatistics> getSubjectStatistics(Long userId) {
        List<Subject> subjects = subjectRepository.findByStatusOrderBySortOrderAsc(1);
        
        return subjects.stream()
                .map(subject -> {
                    long answeredCount = answerRecordRepository
                            .countByUserIdAndQuestionSubjectId(userId, subject.getId());
                    long correctCount = answerRecordRepository
                            .countByUserIdAndQuestionSubjectIdAndIsCorrect(userId, subject.getId(), true);
                    
                    BigDecimal accuracy = BigDecimal.ZERO;
                    if (answeredCount > 0) {
                        accuracy = BigDecimal.valueOf(correctCount)
                                .multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(answeredCount), 2, RoundingMode.HALF_UP);
                    }
                    
                    BigDecimal progress = BigDecimal.ZERO;
                    if (subject.getQuestionCount() > 0) {
                        progress = BigDecimal.valueOf(answeredCount)
                                .multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(subject.getQuestionCount()), 2, RoundingMode.HALF_UP);
                    }
                    
                    return SubjectStatistics.builder()
                            .subjectId(subject.getId())
                            .subjectName(subject.getName())
                            .answeredCount(answeredCount)
                            .correctCount(correctCount)
                            .accuracy(accuracy)
                            .totalQuestions(subject.getQuestionCount())
                            .progress(progress)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取章节学习统计
     */
    public List<ChapterStatistics> getChapterStatistics(Long userId, Long subjectId) {
        List<Chapter> chapters = chapterRepository.findBySubjectIdOrderBySortOrderAsc(subjectId);
        
        return chapters.stream()
                .map(chapter -> {
                    long answeredCount = answerRecordRepository
                            .countByUserIdAndQuestionChapterId(userId, chapter.getId());
                    long correctCount = answerRecordRepository
                            .countByUserIdAndQuestionChapterIdAndIsCorrect(userId, chapter.getId(), true);
                    
                    BigDecimal accuracy = BigDecimal.ZERO;
                    if (answeredCount > 0) {
                        accuracy = BigDecimal.valueOf(correctCount)
                                .multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(answeredCount), 2, RoundingMode.HALF_UP);
                    }
                    
                    // 计算掌握程度（正确率 * 答题完整度）
                    int masteryLevel = 0;
                    if (chapter.getQuestionCount() > 0) {
                        double answerRate = (double) answeredCount / chapter.getQuestionCount();
                        masteryLevel = (int) (accuracy.doubleValue() * answerRate / 100);
                    }
                    
                    return ChapterStatistics.builder()
                            .chapterId(chapter.getId())
                            .chapterName(chapter.getName())
                            .answeredCount(answeredCount)
                            .correctCount(correctCount)
                            .accuracy(accuracy)
                            .totalQuestions(chapter.getQuestionCount())
                            .masteryLevel(masteryLevel)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取学习日历
     */
    public StudyCalendar getStudyCalendar(Long userId, Integer year, Integer month) {
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();
        
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        // 获取该月的打卡记录
        List<DailyCheckIn> checkIns = dailyCheckInRepository
                .findByUserIdAndCheckDateBetween(userId, startDate, endDate);
        
        // 获取该月的答题记录
        List<AnswerRecord> records = answerRecordRepository
                .findByUserIdAndAnsweredAtBetween(userId, startDateTime, endDateTime);
        
        // 构建每日学习数据
        Map<String, StudyCalendar.DayStudyData> studyDataMap = new HashMap<>();
        
        // 填充打卡数据
        for (DailyCheckIn checkIn : checkIns) {
            String dateKey = checkIn.getCheckDate().toString();
            StudyCalendar.DayStudyData dayData = StudyCalendar.DayStudyData.builder()
                    .date(checkIn.getCheckDate())
                    .checked(true)
                    .answeredCount(0L)
                    .accuracy(0)
                    .studyMinutes((long) (checkIn.getStudyTime() / 60)) // 秒转分钟
                    .build();
            studyDataMap.put(dateKey, dayData);
        }
        
        // 统计每日答题数据
        Map<LocalDate, List<AnswerRecord>> recordsByDate = records.stream()
                .collect(Collectors.groupingBy(r -> r.getAnsweredAt().toLocalDate()));
        
        for (Map.Entry<LocalDate, List<AnswerRecord>> entry : recordsByDate.entrySet()) {
            String dateKey = entry.getKey().toString();
            List<AnswerRecord> dayRecords = entry.getValue();
            
            long answeredCount = dayRecords.size();
            long correctCount = dayRecords.stream()
                    .filter(r -> Boolean.TRUE.equals(r.getIsCorrect()))
                    .count();
            int accuracy = answeredCount > 0 ? (int) (correctCount * 100 / answeredCount) : 0;
            
            StudyCalendar.DayStudyData dayData = studyDataMap.getOrDefault(dateKey,
                    StudyCalendar.DayStudyData.builder()
                            .date(entry.getKey())
                            .checked(false)
                            .studyMinutes(0L)
                            .build());
            
            dayData.setAnsweredCount(answeredCount);
            dayData.setAccuracy(accuracy);
            studyDataMap.put(dateKey, dayData);
        }
        
        // 计算连续打卡天数
        int continuousDays = calculateContinuousDaysInMonth(checkIns);
        int totalDays = checkIns.size();
        
        return StudyCalendar.builder()
                .year(year)
                .month(month)
                .studyData(studyDataMap)
                .continuousDays(continuousDays)
                .totalDays(totalDays)
                .build();
    }
    
    /**
     * 打卡
     */
    public void checkIn(Long userId) {
        LocalDate today = LocalDate.now();
        
        // 检查今天是否已打卡
        if (dailyCheckInRepository.existsByUserIdAndCheckDate(userId, today)) {
            throw new RuntimeException("今天已经打卡过了");
        }
        
        // 创建打卡记录
        DailyCheckIn checkIn = new DailyCheckIn();
        checkIn.setUserId(userId);
        checkIn.setCheckDate(today);
        checkIn.setStudyTime(0);
        
        dailyCheckInRepository.save(checkIn);
        log.info("用户{}完成打卡", userId);
    }
    
    /**
     * 计算学习时长（分钟）
     */
    private Long calculateStudyMinutes(Long userId) {
        // 简单估算：每道题平均2分钟
        long totalAnswered = answerRecordRepository.countByUserId(userId);
        return totalAnswered * 2;
    }
    
    /**
     * 计算连续打卡天数
     */
    private int calculateContinuousDays(Long userId) {
        LocalDate today = LocalDate.now();
        List<DailyCheckIn> recentCheckIns = dailyCheckInRepository
                .findByUserIdOrderByCheckDateDesc(userId);
        
        if (recentCheckIns.isEmpty()) {
            return 0;
        }
        
        int continuousDays = 0;
        LocalDate checkDate = today;
        
        for (DailyCheckIn checkIn : recentCheckIns) {
            if (checkIn.getCheckDate().equals(checkDate)) {
                continuousDays++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }
        
        return continuousDays;
    }
    
    /**
     * 计算月内连续打卡天数
     */
    private int calculateContinuousDaysInMonth(List<DailyCheckIn> checkIns) {
        if (checkIns.isEmpty()) {
            return 0;
        }
        
        List<LocalDate> dates = checkIns.stream()
                .map(DailyCheckIn::getCheckDate)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        
        int maxContinuous = 1;
        int currentContinuous = 1;
        
        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i).plusDays(1).equals(dates.get(i - 1))) {
                currentContinuous++;
                maxContinuous = Math.max(maxContinuous, currentContinuous);
            } else {
                currentContinuous = 1;
            }
        }
        
        return maxContinuous;
    }
    
    /**
     * 获取最近学习时间
     */
    private LocalDateTime getLastStudyTime(Long userId) {
        return answerRecordRepository
                .findFirstByUserIdOrderByAnsweredAtDesc(userId)
                .map(AnswerRecord::getAnsweredAt)
                .orElse(null);
    }
    
    /**
     * 获取学习趋势（最近N天）
     */
    public StudyTrendDTO getStudyTrend(Long userId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);
        
        List<LocalDate> dates = new ArrayList<>();
        List<Long> answerCounts = new ArrayList<>();
        List<Integer> accuracyList = new ArrayList<>();
        List<Long> studyMinutes = new ArrayList<>();
        
        // 为每一天生成数据
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            dates.add(date);
            
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);
            
            // 查询该天的答题记录
            List<AnswerRecord> dayRecords = answerRecordRepository
                    .findByUserIdAndAnsweredAtBetween(userId, dayStart, dayEnd);
            
            long answerCount = dayRecords.size();
            answerCounts.add(answerCount);
            
            // 计算正确率
            if (answerCount > 0) {
                long correctCount = dayRecords.stream()
                        .filter(r -> Boolean.TRUE.equals(r.getIsCorrect()))
                        .count();
                int accuracy = (int) (correctCount * 100 / answerCount);
                accuracyList.add(accuracy);
            } else {
                accuracyList.add(0);
            }
            
            // 估算学习时长（每题2分钟）
            studyMinutes.add(answerCount * 2);
        }
        
        return StudyTrendDTO.builder()
                .dates(dates)
                .answerCounts(answerCounts)
                .accuracyList(accuracyList)
                .studyMinutes(studyMinutes)
                .build();
    }
    
    /**
     * 获取题型统计
     */
    public List<QuestionTypeStatDTO> getQuestionTypeStatistics(Long userId) {
        List<AnswerRecord> records = answerRecordRepository.findByUserIdOrderByAnsweredAtDesc(userId, PageRequest.of(0, 10000)).getContent();
        
        // 获取所有问题ID并查询题目信息
        List<Long> questionIds = records.stream()
                .map(AnswerRecord::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        
        if (questionIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Question> questions = questionRepository.findAllById(questionIds);
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        
        // 按题型分组统计
        Map<Question.QuestionType, List<AnswerRecord>> recordsByType = records.stream()
                .filter(r -> {
                    Question q = questionMap.get(r.getQuestionId());
                    return q != null && q.getType() != null;
                })
                .collect(Collectors.groupingBy(r -> questionMap.get(r.getQuestionId()).getType()));
        
        return recordsByType.entrySet().stream()
                .map(entry -> {
                    Question.QuestionType type = entry.getKey();
                    List<AnswerRecord> typeRecords = entry.getValue();
                    
                    long count = typeRecords.size();
                    long correctCount = typeRecords.stream()
                            .filter(r -> Boolean.TRUE.equals(r.getIsCorrect()))
                            .count();
                    int accuracy = count > 0 ? (int) (correctCount * 100 / count) : 0;
                    
                    return QuestionTypeStatDTO.builder()
                            .typeName(getTypeName(type))
                            .count(count)
                            .correctCount(correctCount)
                            .accuracy(accuracy)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取题型名称
     */
    private String getTypeName(Question.QuestionType type) {
        switch (type) {
            case SINGLE:
                return "单选题";
            case MULTIPLE:
                return "多选题";
            case JUDGE:
                return "判断题";
            case FILL:
                return "填空题";
            case SHORT_ANSWER:
                return "简答题";
            default:
                return type.name();
        }
    }
}

