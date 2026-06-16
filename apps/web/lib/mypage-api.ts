import { LoginApiError, requestWithAuth } from './auth-api'

export type MyPageSummaryResponse = {
  hasTodayChat: boolean
  todayMessageCount: number
  recentChatAt?: string | null
  reportCount: number
  latestReportAt?: string | null
  latestReportDate?: string | null
  notificationEnabled: boolean
  notificationTime: string
}

async function readJson(response: Response) {
  return response.json().catch(() => null)
}

export async function readMyPageSummary(): Promise<MyPageSummaryResponse> {
  const response = await requestWithAuth('/api/mypage/summary')
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '마이페이지 요약을 불러오지 못했습니다.')
  }

  return body as MyPageSummaryResponse
}
