import { LoginApiError, requestWithAuth } from '@/lib/auth-api'

export type AdminProfileResponse = {
  memberId: number
  loginId: string
  name: string
  role: 'ADMIN'
}

async function readJson(response: Response) {
  return response.json().catch(() => null)
}

export async function readAdminProfile(): Promise<AdminProfileResponse> {
  const response = await requestWithAuth('/api/admin/me')
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '관리자 정보를 확인하지 못했습니다.')
  }

  return body as AdminProfileResponse
}
