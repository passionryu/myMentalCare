export type LoginRequest = {
  identifier: string
  password: string
}

export type LoginResponse = {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresInSeconds: number
}

export type MyProfileResponse = {
  memberId: number
  loginId: string
  email?: string | null
  name: string
  phone?: string | null
}

export type SignupRequest = {
  loginId: string
  email?: string
  password: string
  name: string
  phone?: string
}

export type SignupResponse = {
  memberId: number
  loginId: string
  name: string
}

export class LoginApiError extends Error {
  constructor(message: string) {
    super(message)
    this.name = 'LoginApiError'
  }
}

const apiBaseUrl = process.env.NEXT_PUBLIC_TARGET_API_BASE_URL ?? 'http://localhost:3001'

export async function loginMember(request: LoginRequest): Promise<LoginResponse> {
  const response = await fetch(`${apiBaseUrl}/api/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })

  const body = await response.json().catch(() => null)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '로그인 처리 중 문제가 발생했습니다.')
  }

  return body as LoginResponse
}

export async function readMyProfile(accessToken: string): Promise<MyProfileResponse> {
  const response = await fetch(`${apiBaseUrl}/api/members/me`, {
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  })

  const body = await response.json().catch(() => null)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '프로필 정보를 불러오지 못했습니다.')
  }

  return body as MyProfileResponse
}

export async function signupMember(request: SignupRequest): Promise<SignupResponse> {
  const response = await fetch(`${apiBaseUrl}/api/members/signup`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })

  const body = await response.json().catch(() => null)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '회원가입 처리 중 문제가 발생했습니다.')
  }

  return body as SignupResponse
}
