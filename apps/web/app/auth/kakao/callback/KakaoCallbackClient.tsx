'use client'

import { AlertCircle, CheckCircle2, Loader2 } from 'lucide-react'
import { useRouter, useSearchParams } from 'next/navigation'
import { useEffect, useMemo, useState } from 'react'
import { exchangeKakaoLoginCode, LoginApiError } from '@/lib/auth-api'

type CallbackStatus = 'loading' | 'success' | 'error'

const kakaoErrorMessages: Record<string, string> = {
  KAKAO_AUTH_CANCELLED: '카카오 로그인이 취소되었습니다.',
  KAKAO_AUTH_FAILED: '카카오 로그인 처리 중 문제가 발생했습니다. 다시 시도해주세요.',
  KAKAO_STATE_INVALID: '로그인 요청 시간이 만료되었습니다. 다시 시도해주세요.',
  KAKAO_ACCOUNT_CONFLICT: '이미 같은 이메일로 가입된 계정이 있습니다. 기존 방식으로 로그인해주세요.',
  KAKAO_EXCHANGE_CODE_INVALID: '카카오 로그인 결과를 확인할 수 없습니다. 다시 시도해주세요.',
}

function normalizeRedirectTo(redirectTo: string | null) {
  if (!redirectTo || !redirectTo.startsWith('/') || redirectTo.startsWith('//') || /[\r\n]/.test(redirectTo)) {
    return '/'
  }
  return redirectTo
}

export default function KakaoCallbackClient() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [status, setStatus] = useState<CallbackStatus>('loading')
  const [message, setMessage] = useState('카카오 로그인 결과를 확인하고 있습니다.')
  const redirectTo = useMemo(() => normalizeRedirectTo(searchParams.get('redirectTo')), [searchParams])

  useEffect(() => {
    const error = searchParams.get('error')
    if (error) {
      setStatus('error')
      setMessage(kakaoErrorMessages[error] ?? '카카오 로그인 처리 중 문제가 발생했습니다. 다시 시도해주세요.')
      return
    }

    const code = searchParams.get('code')
    if (!code) {
      setStatus('error')
      setMessage('카카오 로그인 결과를 확인할 수 없습니다. 다시 시도해주세요.')
      return
    }

    let cancelled = false
    exchangeKakaoLoginCode({ code })
      .then((tokens) => {
        if (cancelled) {
          return
        }
        localStorage.setItem('myMentalCare.accessToken', tokens.accessToken)
        localStorage.setItem('myMentalCare.refreshToken', tokens.refreshToken)
        setStatus('success')
        setMessage('카카오 로그인이 완료되었습니다. 잠시 후 이동합니다.')
        window.setTimeout(() => router.replace(redirectTo), 900)
      })
      .catch((error) => {
        if (cancelled) {
          return
        }
        setStatus('error')
        setMessage(error instanceof LoginApiError ? error.message : '카카오 로그인 처리 중 문제가 발생했습니다. 다시 시도해주세요.')
      })

    return () => {
      cancelled = true
    }
  }, [redirectTo, router, searchParams])

  return (
    <main className="page-shell callback-page-shell">
      <section className="auth-callback-card" aria-live="polite">
        <div className={`auth-callback-icon ${status}`}>
          {status === 'loading' && <Loader2 size={28} aria-hidden="true" />}
          {status === 'success' && <CheckCircle2 size={28} aria-hidden="true" />}
          {status === 'error' && <AlertCircle size={28} aria-hidden="true" />}
        </div>
        <p className="eyebrow">Kakao Login</p>
        <h1>카카오 로그인</h1>
        <p>{message}</p>
        {status === 'error' && (
          <div className="callback-actions">
            <button className="primary-button" type="button" onClick={() => router.replace('/')}>
              처음 화면으로 돌아가기
            </button>
          </div>
        )}
      </section>
    </main>
  )
}
