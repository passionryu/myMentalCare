import { LoginApiError, requestWithAuth } from '@/lib/auth-api'

export type AdminReportSummary = {
  id: number
  roomId: number
  memberId: number
  conversationDate: string
  primaryEmotion: string
  emotionScore?: number | null
  mainCause: string
  createdAt?: string | null
}

export type AdminReportSong = {
  title: string
  artist: string
  reason: string
  youtubeUrl: string
}

export type AdminReportEmotionPoint = {
  label: string
  score: number
  reason: string
}

export type AdminReportDetail = AdminReportSummary & {
  reportType: string
  summary: string
  emotionIntensity?: number | null
  emotionalFlow: string
  todaySentence: string
  songs: AdminReportSong[]
  emotionTimeline: AdminReportEmotionPoint[]
}

export type AdminReportPageResponse = {
  reports: AdminReportSummary[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type AdminChatMessage = {
  id: number
  roomId: number
  senderType: 'USER' | 'AI' | string
  content: string
  messageOrder: number
  createdAt?: string | null
}

export type AdminReportSearchParams = {
  memberId?: string
  date?: string
  keyword?: string
  page?: number
  size?: number
}

async function readJson(response: Response) {
  return response.json().catch(() => null)
}

export async function readAdminReports(params: AdminReportSearchParams = {}): Promise<AdminReportPageResponse> {
  const searchParams = new URLSearchParams()

  if (params.memberId?.trim()) {
    searchParams.set('memberId', params.memberId.trim())
  }

  if (params.date) {
    searchParams.set('date', params.date)
  }

  if (params.keyword?.trim()) {
    searchParams.set('keyword', params.keyword.trim())
  }

  searchParams.set('page', String(params.page ?? 0))
  searchParams.set('size', String(params.size ?? 20))

  const response = await requestWithAuth(`/api/admin/reports?${searchParams.toString()}`)
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '리포트 목록을 불러오지 못했습니다.')
  }

  return body as AdminReportPageResponse
}

export async function readAdminReport(reportId: number): Promise<AdminReportDetail> {
  const response = await requestWithAuth(`/api/admin/reports/${reportId}`)
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '리포트 상세를 불러오지 못했습니다.')
  }

  return body as AdminReportDetail
}

export async function readAdminChatMessages(roomId: number, reason: string): Promise<AdminChatMessage[]> {
  const searchParams = new URLSearchParams()
  searchParams.set('reason', reason)

  const response = await requestWithAuth(`/api/admin/chat-rooms/${roomId}/messages?${searchParams.toString()}`)
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '원문 대화를 불러오지 못했습니다.')
  }

  return body as AdminChatMessage[]
}
