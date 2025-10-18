/**
 * 通用类型定义
 */

// API响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页参数
export interface PageParams {
  page: number
  size: number
}

// 分页响应
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// 用户角色
export enum UserRole {
  STUDENT = 'STUDENT',
  TEACHER = 'TEACHER',
  ADMIN = 'ADMIN'
}

// 用户信息
export interface User {
  id: number
  username: string
  email: string
  realName: string
  role: UserRole
  avatar?: string
  phone?: string
  bio?: string
  status: number
  createdAt: string
}

// 登录请求
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应
export interface LoginResponse {
  token: string
  tokenType: string
  userId: number
  username: string
  role: string
  realName: string
  avatar?: string
}

// 注册请求
export interface RegisterRequest {
  username: string
  password: string
  email: string
  realName: string
}

// 学科
export interface Subject {
  id: number
  name: string
  code: string
  description?: string
  icon?: string
  questionCount: number
  chapterCount?: number
  studentCount?: number
  sortOrder: number
  status: number
  createdAt?: string
}

// 章节
export interface Chapter {
  id: number
  subjectId: number
  parentId: number | null
  name: string
  description?: string
  level: number
  sortOrder: number
  questionCount: number
  createdAt?: string
  children?: Chapter[]
}

// 题型
export enum QuestionType {
  SINGLE = 'SINGLE',
  MULTIPLE = 'MULTIPLE',
  JUDGE = 'JUDGE',
  FILL = 'FILL',
  SHORT_ANSWER = 'SHORT_ANSWER',
  ESSAY = 'ESSAY',
  CASE_ANALYSIS = 'CASE_ANALYSIS',
  MATERIAL_ANALYSIS = 'MATERIAL_ANALYSIS',
  CALCULATION = 'CALCULATION',
  ORDERING = 'ORDERING',
  MATCHING = 'MATCHING',
  COMPOSITE = 'COMPOSITE',
  PROGRAMMING = 'PROGRAMMING'
}

// 难度
export enum Difficulty {
  EASY = 'EASY',
  MEDIUM = 'MEDIUM',
  HARD = 'HARD'
}

// 题目
export interface Question {
  id: number
  subjectId: number
  subjectName?: string
  chapterId?: number
  chapterName?: string
  type: QuestionType | string
  title: string
  content?: string
  options?: any
  answer: any
  answerAnalysis?: string
  difficulty: Difficulty | string | number
  score: number
  tags?: string
  knowledgePoints?: string
  aiGradingEnabled?: boolean
  aiGradingConfig?: any
  scoringCriteria?: any
  referenceKeywords?: any
  useCount?: number
  correctCount?: number
  wrongCount?: number
  creatorId?: number
  creatorName?: string
  status: number
  createdAt?: string
  updatedAt?: string
}

// 刷题模式
export enum PracticeMode {
  SEQUENTIAL = 'SEQUENTIAL',
  RANDOM = 'RANDOM',
  CHAPTER = 'CHAPTER',
  EXAM = 'EXAM',
  WRONG_QUESTION = 'WRONG_QUESTION',
  FAVORITE = 'FAVORITE',
  CHALLENGE = 'CHALLENGE',
  TIMED = 'TIMED',
  SMART_RECOMMEND = 'SMART_RECOMMEND'
}

// 刷题请求
export interface PracticeRequest {
  mode: PracticeMode
  subjectId: number
  chapterId?: number
  questionType?: QuestionType
  difficulty?: Difficulty
  count?: number
  continueProgress?: boolean
  timePerQuestion?: number
  examDuration?: number
  challengeLevel?: number
}

// 刷题会话
export interface PracticeSession {
  sessionId: string
  mode: PracticeMode
  subjectId: number
  subjectName: string
  chapterId?: number
  chapterName?: string
  questions: Question[]
  totalCount: number
  currentProgress: number
  startTime: string
  endTime?: string
  timePerQuestion?: number
  examDuration?: number
  challengeLevel?: number
  passRequiredCorrect?: number
  tip?: string
}

// 提交答案请求
export interface SubmitAnswerRequest {
  questionId: number
  userAnswer: any
  timeSpent?: number
}

// 判题结果
export interface GradingResult {
  questionId: number
  answerRecordId?: number
  isCorrect: boolean
  score: number
  fullScore?: number
  totalScore?: number
  gradingType?: string // 'AUTO' | 'AI' | 'MANUAL'
  feedback?: string
  aiAnalysis?: string
  correctAnswer: any
  userAnswer: any
  needManualReview?: boolean
  aiFeedback?: AIFeedback
}

export interface AIFeedback {
  model?: string
  confidence?: number
  scoreDetails?: ScoreDetail[]
  strengths?: string[]
  weaknesses?: string[]
  suggestions?: string
  comment?: string
}

export interface ScoreDetail {
  dimension: string
  score: number
  maxScore: number
  reason: string
}

// 用户统计
export interface UserStatistics {
  userId: number
  username: string
  realName: string
  totalAnswered: number
  correctCount: number
  wrongCount: number
  accuracy: number
  totalStudyMinutes: number
  continuousDays: number
  totalCheckInDays: number
  favoriteCount: number
  noteCount: number
  wrongQuestionCount: number
  achievementCount: number
  totalPoints: number
  ranking?: number
  lastStudyTime?: string
}

// 学科统计
export interface SubjectStatistics {
  subjectId: number
  subjectName: string
  answeredCount: number
  correctCount: number
  accuracy: number
  totalQuestions: number
  progress: number
}

// 章节统计
export interface ChapterStatistics {
  chapterId: number
  chapterName: string
  answeredCount: number
  correctCount: number
  accuracy: number
  totalQuestions: number
  masteryLevel: number
}

// 排行榜项
export interface RankingItem {
  rank: number
  userId: number
  username: string
  realName: string
  avatar?: string
  value: number
  accuracy?: number
  points?: number
  isCurrentUser: boolean
}

// 学习日历
export interface StudyCalendar {
  year: number
  month: number
  studyData: Record<string, DayStudyData>
  continuousDays: number
  totalDays: number
}

export interface DayStudyData {
  date: string
  checked: boolean
  answeredCount: number
  accuracy: number
  studyMinutes: number
}

// 收藏
export interface Favorite {
  id: number
  userId: number
  questionId: number
  category?: string
  remark?: string
  createdAt: string
}

// 笔记
export interface Note {
  id: number
  userId: number
  questionId: number
  title: string
  content: string
  createdAt: string
  updatedAt: string
}

// 错题
export interface WrongQuestion {
  id: number
  userId: number
  questionId: number
  wrongCount: number
  lastWrongAt: string
  status: string
  removed: boolean
}

// 答题记录
export interface AnswerRecord {
  id: number
  userId: number
  username?: string
  questionId: number
  practiceMode?: string
  examId?: number
  userAnswer: any
  isCorrect: boolean
  score: number
  gradingType?: string
  gradingStatus?: string
  timeSpent?: number
  isMarked?: boolean
  note?: string
  answeredAt: string
  gradedAt?: string
  question?: {
    id: number
    title?: string
    content?: string
    answer: any
    answerAnalysis?: string
    score: number
    subject?: {
      id: number
      name: string
      code: string
    }
    chapter?: {
      id: number
      name: string
    }
  }
}

// AI判题记录
export interface AIGradingRecord {
  id: number
  answerRecordId: number
  questionId: number
  userId: number
  username?: string
  questionContent: string
  userAnswer: string
  score: number
  confidence: number
  feedback?: string
  aiModel?: string
  aiFeedback?: any
  manualReview?: boolean
  manualScore?: number
  reviewerId?: number
  reviewComment?: string
  finalScore?: number
  gradingTime?: number
  gradedAt: string
  createdAt: string
}

