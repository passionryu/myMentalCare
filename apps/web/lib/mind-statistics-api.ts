import { LoginApiError, requestWithAuth } from '@/lib/auth-api'

export type MindStatisticsCalendarDay = {
  date: string
  hasConversation: boolean
  hasReport: boolean
  messageCount: number
  primaryEmotion?: string | null
  emotionIntensity?: number | null
  todaySentence?: string | null
}

export type MindStatisticsCalendar = {
  month: string
  totalConversationDays: number
  totalReportDays: number
  days: MindStatisticsCalendarDay[]
}

export type MindStatisticsMessage = {
  messageId: number
  senderType: 'USER' | 'ASSISTANT' | 'SYSTEM' | string
  contentPreview: string
  messageOrder: number
  isCrisisDetected: boolean
  createdAt?: string | null
}

export type MindStatisticsReport = {
  reportId: number
  reportType: string
  primaryEmotion: string
  emotionIntensity?: number | null
  mainCause: string
  summary: string
  emotionalFlow: string
  todaySentence: string
  createdAt?: string | null
}

export type MindStatisticsDayDetail = {
  date: string
  roomId?: number | null
  hasConversation: boolean
  messageCount: number
  messages: MindStatisticsMessage[]
  report?: MindStatisticsReport | null
}

export type MindStatisticsEmotionDistribution = {
  emotion: string
  count: number
  ratio: number
}

export type MindStatisticsEmotionTrend = {
  weekStartDate: string
  weekEndDate: string
  averageEmotionIntensity?: number | null
  reportCount: number
}

export type MindStatisticsOverview = {
  rangeWeeks: number
  startDate: string
  endDate: string
  totalConversationDays: number
  totalMessages: number
  totalReports: number
  emotionDistribution: MindStatisticsEmotionDistribution[]
  emotionTrend: MindStatisticsEmotionTrend[]
}

async function readJson(response: Response) {
  return response.json().catch(() => null)
}

export async function readMindStatisticsCalendar(month: string): Promise<MindStatisticsCalendar> {
  const response = await requestWithAuth(`/api/mind-statistics/calendar?month=${encodeURIComponent(month)}`)
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '마음 달력을 불러오지 못했습니다.')
  }

  return body as MindStatisticsCalendar
}

export async function readMindStatisticsDayDetail(date: string): Promise<MindStatisticsDayDetail> {
  const response = await requestWithAuth(`/api/mind-statistics/days/${encodeURIComponent(date)}`)
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '선택한 날짜의 마음 기록을 불러오지 못했습니다.')
  }

  return body as MindStatisticsDayDetail
}

export async function readMindStatisticsOverview(rangeWeeks = 4): Promise<MindStatisticsOverview> {
  const response = await requestWithAuth(`/api/mind-statistics/overview?rangeWeeks=${rangeWeeks}`)
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '감정 통계를 불러오지 못했습니다.')
  }

  return body as MindStatisticsOverview
}
