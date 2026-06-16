import { LoginApiError, requestWithAuth } from './auth-api'

export type CreateInquiryRequest = {
  category: string
  content: string
}

export type CreateInquiryResponse = {
  inquiryId: number
  createdAt: string
  status: string
}

async function readJson(response: Response) {
  return response.json().catch(() => null)
}

export async function createInquiry(request: CreateInquiryRequest): Promise<CreateInquiryResponse> {
  const response = await requestWithAuth('/api/inquiries', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '문의 접수 중 문제가 발생했습니다.')
  }

  return body as CreateInquiryResponse
}
