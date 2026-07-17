import { LoginApiError, requestWithAuth } from '@/lib/auth-api'

export type AdminMemberStatus = 'ACTIVE' | 'SUSPENDED' | 'WITHDRAWN'
export type AdminMemberRole = 'USER' | 'ADMIN'

export type AdminMemberSummary = {
  id: number
  loginId: string
  email?: string | null
  name: string
  status: AdminMemberStatus
  role: AdminMemberRole
  createdAt: string
}

export type AdminMemberDetail = AdminMemberSummary & {
  phone?: string | null
  updatedAt: string
  deletedAt?: string | null
}

export type AdminMemberPageResponse = {
  members: AdminMemberSummary[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type AdminMemberSearchParams = {
  keyword?: string
  status?: AdminMemberStatus | ''
  page?: number
  size?: number
}

export type ChangeAdminMemberStatusRequest = {
  status: AdminMemberStatus
  reason: string
}

async function readJson(response: Response) {
  return response.json().catch(() => null)
}

export async function readAdminMembers(params: AdminMemberSearchParams = {}): Promise<AdminMemberPageResponse> {
  const searchParams = new URLSearchParams()

  if (params.keyword?.trim()) {
    searchParams.set('keyword', params.keyword.trim())
  }

  if (params.status) {
    searchParams.set('status', params.status)
  }

  searchParams.set('page', String(params.page ?? 0))
  searchParams.set('size', String(params.size ?? 20))

  const response = await requestWithAuth(`/api/admin/users?${searchParams.toString()}`)
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '회원 목록을 불러오지 못했습니다.')
  }

  return body as AdminMemberPageResponse
}

export async function readAdminMember(memberId: number): Promise<AdminMemberDetail> {
  const response = await requestWithAuth(`/api/admin/users/${memberId}`)
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '회원 상세 정보를 불러오지 못했습니다.')
  }

  return body as AdminMemberDetail
}

export async function changeAdminMemberStatus(memberId: number, request: ChangeAdminMemberStatusRequest): Promise<AdminMemberDetail> {
  const response = await requestWithAuth(`/api/admin/users/${memberId}/status`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '회원 상태를 변경하지 못했습니다.')
  }

  return body as AdminMemberDetail
}
