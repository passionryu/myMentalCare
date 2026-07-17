import { LoginApiError, requestWithAuth } from '@/lib/auth-api'

export type AdminInquiryStatus = 'RECEIVED' | 'IN_PROGRESS' | 'DONE'

export type AdminInquirySummary = {
  id: number
  memberId: number
  category: string
  status: AdminInquiryStatus
  createdAt: string
}

export type AdminInquiryDetail = AdminInquirySummary & {
  content: string
  adminMemo?: string | null
  handledByMemberId?: number | null
  handledAt?: string | null
  updatedAt: string
}

export type AdminInquiryPageResponse = {
  inquiries: AdminInquirySummary[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type AdminInquirySearchParams = {
  keyword?: string
  status?: AdminInquiryStatus | ''
  page?: number
  size?: number
}

async function readJson(response: Response) {
  return response.json().catch(() => null)
}

export async function readAdminInquiries(params: AdminInquirySearchParams = {}): Promise<AdminInquiryPageResponse> {
  const searchParams = new URLSearchParams()

  if (params.keyword?.trim()) {
    searchParams.set('keyword', params.keyword.trim())
  }

  if (params.status) {
    searchParams.set('status', params.status)
  }

  searchParams.set('page', String(params.page ?? 0))
  searchParams.set('size', String(params.size ?? 20))

  const response = await requestWithAuth(`/api/admin/inquiries?${searchParams.toString()}`)
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '문의 목록을 불러오지 못했습니다.')
  }

  return body as AdminInquiryPageResponse
}

export async function readAdminInquiry(inquiryId: number): Promise<AdminInquiryDetail> {
  const response = await requestWithAuth(`/api/admin/inquiries/${inquiryId}`)
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '문의 상세를 불러오지 못했습니다.')
  }

  return body as AdminInquiryDetail
}

export async function changeAdminInquiryStatus(
  inquiryId: number,
  status: AdminInquiryStatus,
  adminMemo?: string,
): Promise<AdminInquiryDetail> {
  const response = await requestWithAuth(`/api/admin/inquiries/${inquiryId}/status`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ status, adminMemo }),
  })
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '문의 상태를 변경하지 못했습니다.')
  }

  return body as AdminInquiryDetail
}

export async function updateAdminInquiryMemo(inquiryId: number, adminMemo?: string): Promise<AdminInquiryDetail> {
  const response = await requestWithAuth(`/api/admin/inquiries/${inquiryId}/memo`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ adminMemo }),
  })
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '운영 메모를 저장하지 못했습니다.')
  }

  return body as AdminInquiryDetail
}
