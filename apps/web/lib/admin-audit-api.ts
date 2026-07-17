import { LoginApiError, requestWithAuth } from '@/lib/auth-api'

export type AdminAuditLogTargetType = 'ADMIN' | 'MEMBER' | 'INQUIRY' | 'AI_CHAT_ROOM' | 'AI_CHAT_REPORT' | 'SYSTEM'

export type AdminAuditLog = {
  id: number
  adminMemberId: number
  adminLoginId: string
  action: string
  targetType?: AdminAuditLogTargetType | null
  targetId?: number | null
  reason?: string | null
  createdAt?: string | null
}

export type AdminAuditLogPageResponse = {
  logs: AdminAuditLog[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type AdminAuditLogSearchParams = {
  targetType?: AdminAuditLogTargetType | ''
  targetId?: string
  page?: number
  size?: number
}

async function readJson(response: Response) {
  return response.json().catch(() => null)
}

export async function readAdminAuditLogs(params: AdminAuditLogSearchParams = {}): Promise<AdminAuditLogPageResponse> {
  const searchParams = new URLSearchParams()

  if (params.targetType) {
    searchParams.set('targetType', params.targetType)
  }

  if (params.targetId) {
    searchParams.set('targetId', params.targetId)
  }

  searchParams.set('page', String(params.page ?? 0))
  searchParams.set('size', String(params.size ?? 20))

  const response = await requestWithAuth(`/api/admin/audit-logs?${searchParams.toString()}`)
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '감사 로그를 불러오지 못했습니다.')
  }

  return body as AdminAuditLogPageResponse
}
