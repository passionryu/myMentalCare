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

export type UpdateMyProfileRequest = {
  name: string
  email?: string | null
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

export type ReissueTokenRequest = {
  refreshToken: string
}

export type KakaoExchangeRequest = {
  code: string
}

export class LoginApiError extends Error {
  constructor(message: string) {
    super(message)
    this.name = 'LoginApiError'
  }
}

const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? process.env.NEXT_PUBLIC_TARGET_API_BASE_URL ?? 'http://localhost:3001'
const accessTokenKey = 'myMentalCare.accessToken'
const refreshTokenKey = 'myMentalCare.refreshToken'
let tokenReissueRequest: { refreshToken: string; promise: Promise<LoginResponse> } | null = null

function readStoredAccessToken(): string | null {
  return localStorage.getItem(accessTokenKey)
}

function readStoredRefreshToken(): string | null {
  return localStorage.getItem(refreshTokenKey)
}

function storeLoginTokens(tokens: LoginResponse) {
  localStorage.setItem(accessTokenKey, tokens.accessToken)
  localStorage.setItem(refreshTokenKey, tokens.refreshToken)
}

function clearLoginTokens() {
  localStorage.removeItem(accessTokenKey)
  localStorage.removeItem(refreshTokenKey)
}

async function readJson(response: Response) {
  return response.json().catch(() => null)
}

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

export function buildKakaoLoginUrl(redirectTo = '/') {
  const params = new URLSearchParams()
  params.set('redirectTo', redirectTo)
  return `${apiBaseUrl}/api/auth/kakao/login?${params.toString()}`
}

export async function exchangeKakaoLoginCode(request: KakaoExchangeRequest): Promise<LoginResponse> {
  const response = await fetch(`${apiBaseUrl}/api/auth/kakao/exchange`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })

  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '카카오 로그인 처리 중 문제가 발생했습니다.')
  }

  return body as LoginResponse
}

export async function reissueToken(request: ReissueTokenRequest): Promise<LoginResponse> {
  const response = await fetch(`${apiBaseUrl}/api/auth/reissue`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })

  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '로그인 상태를 다시 확인해주세요.')
  }

  return body as LoginResponse
}

async function reissueStoredLoginTokens(refreshToken: string): Promise<LoginResponse> {
  if (!tokenReissueRequest || tokenReissueRequest.refreshToken !== refreshToken) {
    const promise = reissueToken({ refreshToken })
      .then((tokens) => {
        storeLoginTokens(tokens)
        return tokens
      })
      .finally(() => {
        if (tokenReissueRequest?.refreshToken === refreshToken) {
          tokenReissueRequest = null
        }
      })

    tokenReissueRequest = { refreshToken, promise }
  }

  return tokenReissueRequest.promise
}

async function retryRequestWithLatestToken(path: string, init: RequestInit): Promise<Response> {
  const retryAccessToken = readStoredAccessToken()
  const retryResponse = await requestWithAuth(path, init, false)
  if (retryResponse.status === 401 && readStoredAccessToken() === retryAccessToken) {
    clearLoginTokens()
  }

  return retryResponse
}

export async function requestWithAuth(path: string, init: RequestInit = {}, retryOnUnauthorized = true): Promise<Response> {
  const accessToken = readStoredAccessToken()
  if (!accessToken) {
    throw new LoginApiError('로그인이 필요합니다.')
  }

  const headers = new Headers(init.headers)
  headers.set('Authorization', `Bearer ${accessToken}`)

  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...init,
    headers,
  })

  if (response.status !== 401 || !retryOnUnauthorized) {
    return response
  }

  const latestAccessToken = readStoredAccessToken()
  if (latestAccessToken && latestAccessToken !== accessToken) {
    return retryRequestWithLatestToken(path, init)
  }

  const refreshToken = readStoredRefreshToken()
  if (!refreshToken) {
    clearLoginTokens()
    throw new LoginApiError('로그인 시간이 만료되었습니다. 다시 로그인해주세요.')
  }

  try {
    await reissueStoredLoginTokens(refreshToken)
  } catch (error) {
    if (readStoredRefreshToken() !== refreshToken) {
      return retryRequestWithLatestToken(path, init)
    }

    clearLoginTokens()
    if (error instanceof LoginApiError) {
      throw error
    }
    throw new LoginApiError('로그인 시간이 만료되었습니다. 다시 로그인해주세요.')
  }

  return retryRequestWithLatestToken(path, init)
}

export async function readMyProfile(accessToken?: string): Promise<MyProfileResponse> {
  if (accessToken && !readStoredAccessToken()) {
    localStorage.setItem(accessTokenKey, accessToken)
  }

  const response = await requestWithAuth('/api/members/me')
  const body = await readJson(response)

  if (!response.ok) {
    if (response.status === 401) {
      clearLoginTokens()
    }
    throw new LoginApiError(body?.message ?? '프로필 정보를 불러오지 못했습니다.')
  }

  return body as MyProfileResponse
}

export async function updateMyProfile(request: UpdateMyProfileRequest): Promise<MyProfileResponse> {
  const response = await requestWithAuth('/api/members/me', {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })
  const body = await readJson(response)

  if (!response.ok) {
    if (response.status === 401) {
      clearLoginTokens()
    }
    throw new LoginApiError(body?.message ?? '개인정보를 저장하지 못했습니다.')
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
