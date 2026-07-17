'use client'

import { ArrowLeft, LogIn, ShieldCheck } from 'lucide-react'
import Link from 'next/link'
import { FormEvent, useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { readAdminProfile } from '@/lib/admin-api'
import { LoginApiError, clearLoginTokens, loginMember, storeLoginTokens } from '@/lib/auth-api'

export default function AdminLoginPage() {
  const router = useRouter()
  const [identifier, setIdentifier] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')
  const [isCheckingSession, setIsCheckingSession] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    readAdminProfile()
      .then(() => router.replace('/admin'))
      .catch(() => setIsCheckingSession(false))
  }, [router])

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const trimmedIdentifier = identifier.trim()
    if (!trimmedIdentifier || !password || isSubmitting) {
      return
    }

    setIsSubmitting(true)
    setMessage('')

    try {
      const tokens = await loginMember({ identifier: trimmedIdentifier, password })
      storeLoginTokens(tokens)

      try {
        await readAdminProfile()
      } catch {
        clearLoginTokens()
        setMessage('관리자 권한이 없는 계정입니다.')
        return
      }

      router.replace('/admin')
    } catch (error) {
      clearLoginTokens()
      setMessage(error instanceof LoginApiError ? error.message : '관리자 로그인 처리 중 문제가 발생했습니다.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="admin-login-shell">
      <section className="admin-login-card" aria-labelledby="admin-login-title">
        <Link className="admin-login-back-link" href="/">
          <ArrowLeft aria-hidden="true" />
          홈으로
        </Link>

        <div className="admin-login-icon">
          <ShieldCheck aria-hidden="true" />
        </div>

        <header>
          <span>Haru Mind Admin</span>
          <h1 id="admin-login-title">관리자 로그인</h1>
          <p>DB에서 관리자 권한이 부여된 계정만 콘솔에 접근할 수 있습니다.</p>
        </header>

        <form className="admin-login-form" onSubmit={handleSubmit}>
          <label>
            <span>아이디 또는 이메일</span>
            <input
              type="text"
              value={identifier}
              onChange={(event) => setIdentifier(event.target.value)}
              autoComplete="username"
              placeholder="관리자 계정"
              disabled={isCheckingSession || isSubmitting}
            />
          </label>

          <label>
            <span>비밀번호</span>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              placeholder="비밀번호"
              disabled={isCheckingSession || isSubmitting}
            />
          </label>

          {message ? <p className="admin-login-error">{message}</p> : null}

          <button className="admin-primary-button admin-login-submit" type="submit" disabled={isCheckingSession || isSubmitting || !identifier.trim() || !password}>
            <LogIn aria-hidden="true" />
            {isCheckingSession ? '세션 확인 중' : isSubmitting ? '로그인 중' : '관리자 로그인'}
          </button>
        </form>
      </section>
    </main>
  )
}
