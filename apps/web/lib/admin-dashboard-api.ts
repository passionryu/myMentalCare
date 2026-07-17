import { LoginApiError, requestWithAuth } from '@/lib/auth-api'

export type AdminDashboardSummary = {
  totalMembers: number
  todaySignups: number
  todayConversations: number
  todayReports: number
  failedReports: number
  baseDate: string
}

export type AdminDashboardDailyMetric = {
  date: string
  signups: number
  conversations: number
  reports: number
}

export type AdminDashboardAlert = {
  type: string
  severity: 'OK' | 'INFO' | 'NOTICE' | 'WARNING' | 'ERROR' | string
  title: string
  description: string
  targetPath?: string | null
}

async function readJson(response: Response) {
  return response.json().catch(() => null)
}

export async function readAdminDashboardSummary(): Promise<AdminDashboardSummary> {
  const response = await requestWithAuth('/api/admin/dashboard/summary')
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '대시보드 요약을 불러오지 못했습니다.')
  }

  return body as AdminDashboardSummary
}

export async function readAdminDashboardDailyMetrics(days = 7): Promise<AdminDashboardDailyMetric[]> {
  const response = await requestWithAuth(`/api/admin/dashboard/daily-metrics?days=${days}`)
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '일자별 지표를 불러오지 못했습니다.')
  }

  return body as AdminDashboardDailyMetric[]
}

export async function readAdminDashboardAlerts(): Promise<AdminDashboardAlert[]> {
  const response = await requestWithAuth('/api/admin/dashboard/alerts')
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '운영 알림을 불러오지 못했습니다.')
  }

  return body as AdminDashboardAlert[]
}
