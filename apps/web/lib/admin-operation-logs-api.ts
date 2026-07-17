import { LoginApiError, requestWithAuth } from '@/lib/auth-api'

export type AdminIncidentImpact = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export type AdminOperationLog = {
  id: number
  level: AdminIncidentImpact
  title: string
  description?: string | null
  action?: string | null
  createdByMemberId: number
  createdAt?: string | null
}

export type AdminOperationLogPageResponse = {
  logs: AdminOperationLog[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type AdminOperationLogSummary = {
  from: string
  to: string
  totalIncidents: number
  highImpactIncidents: number
  criticalIncidents: number
  actionRequiredIncidents: number
  latestIncidentAt?: string | null
}

export type AdminIncidentCreateRequest = {
  title: string
  description?: string
  impact: AdminIncidentImpact
  action?: string
}

export type AdminIncidentCreateResponse = {
  incidentId: number
  createdAt?: string | null
}

async function readJson(response: Response) {
  return response.json().catch(() => null)
}

export async function readAdminOperationLogs(params: {
  level?: AdminIncidentImpact | ''
  keyword?: string
  page?: number
  size?: number
} = {}): Promise<AdminOperationLogPageResponse> {
  const searchParams = new URLSearchParams()

  if (params.level) {
    searchParams.set('level', params.level)
  }

  if (params.keyword?.trim()) {
    searchParams.set('keyword', params.keyword.trim())
  }

  searchParams.set('page', String(params.page ?? 0))
  searchParams.set('size', String(params.size ?? 20))

  const response = await requestWithAuth(`/api/admin/operation-logs?${searchParams.toString()}`)
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '운영 로그를 불러오지 못했습니다.')
  }

  return body as AdminOperationLogPageResponse
}

export async function readAdminOperationLogSummary(): Promise<AdminOperationLogSummary> {
  const response = await requestWithAuth('/api/admin/operation-logs/summary')
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '운영 로그 요약을 불러오지 못했습니다.')
  }

  return body as AdminOperationLogSummary
}

export async function createAdminIncident(request: AdminIncidentCreateRequest): Promise<AdminIncidentCreateResponse> {
  const response = await requestWithAuth('/api/admin/incidents', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '장애 기록을 생성하지 못했습니다.')
  }

  return body as AdminIncidentCreateResponse
}
