'use client'

import { AlertTriangle, BarChart3, ClipboardList, FileText, Home, Inbox, LogOut, MessageSquareText, ShieldCheck, UsersRound } from 'lucide-react'
import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import { ReactNode, useEffect, useMemo, useState } from 'react'
import { AdminProfileResponse, readAdminProfile } from '@/lib/admin-api'
import { LoginApiError, clearLoginTokens } from '@/lib/auth-api'

type AdminShellProps = {
  children: ReactNode
}

const adminNavigation = [
  { href: '/admin', label: '대시보드', icon: BarChart3 },
  { href: '/admin/users', label: '회원 관리', icon: UsersRound },
  { href: '/admin/inquiries', label: '문의 관리', icon: Inbox },
  { href: '/admin/conversations', label: '대화/리포트', icon: MessageSquareText },
  { href: '/admin/logs', label: '감사/장애 로그', icon: FileText },
]

function isActiveNavigation(pathname: string, href: string) {
  if (href === '/admin') {
    return pathname === href
  }

  return pathname.startsWith(href)
}

export default function AdminShell({ children }: AdminShellProps) {
  const router = useRouter()
  const pathname = usePathname()
  const [profile, setProfile] = useState<AdminProfileResponse | null>(null)
  const [status, setStatus] = useState<'loading' | 'ready' | 'blocked'>('loading')
  const [message, setMessage] = useState('')

  const currentPageLabel = useMemo(() => {
    return adminNavigation.find((item) => isActiveNavigation(pathname, item.href))?.label ?? '관리자'
  }, [pathname])

  useEffect(() => {
    readAdminProfile()
      .then((nextProfile) => {
        setProfile(nextProfile)
        setStatus('ready')
        setMessage('')
      })
      .catch((error) => {
        setStatus('blocked')
        setMessage(error instanceof LoginApiError ? error.message : '관리자 권한을 확인하지 못했습니다.')
      })
  }, [])

  function handleLogout() {
    clearLoginTokens()
    router.replace('/')
  }

  if (status === 'loading') {
    return (
      <main className="admin-shell admin-gate-shell">
        <section className="admin-gate-card">
          <ShieldCheck aria-hidden="true" />
          <p>관리자 권한을 확인하는 중입니다.</p>
        </section>
      </main>
    )
  }

  if (status === 'blocked') {
    return (
      <main className="admin-shell admin-gate-shell">
        <section className="admin-gate-card">
          <AlertTriangle aria-hidden="true" />
          <h1>관리자 접근이 필요합니다</h1>
          <p>{message}</p>
          <div className="admin-gate-actions">
            <Link href="/">홈으로</Link>
            <Link href="/mypage">마이페이지</Link>
          </div>
        </section>
      </main>
    )
  }

  return (
    <main className="admin-shell">
      <aside className="admin-sidebar" aria-label="관리자 메뉴">
        <div className="admin-brand">
          <span>Haru Mind</span>
          <strong>Admin</strong>
        </div>
        <nav className="admin-navigation">
          {adminNavigation.map((item) => {
            const Icon = item.icon
            const active = isActiveNavigation(pathname, item.href)

            return (
              <Link key={item.href} className={active ? 'active' : ''} href={item.href} aria-current={active ? 'page' : undefined}>
                <Icon aria-hidden="true" />
                <span>{item.label}</span>
              </Link>
            )
          })}
        </nav>
        <Link className="admin-home-link" href="/">
          <Home aria-hidden="true" />
          서비스 홈
        </Link>
      </aside>

      <section className="admin-main">
        <header className="admin-topbar">
          <div>
            <span className="admin-eyebrow">관리자 콘솔</span>
            <h1>{currentPageLabel}</h1>
          </div>
          <div className="admin-profile-chip">
            <ClipboardList aria-hidden="true" />
            <div>
              <strong>{profile?.name ?? '관리자'}</strong>
              <span>{profile?.loginId}</span>
            </div>
            <button type="button" onClick={handleLogout} aria-label="관리자 로그아웃">
              <LogOut aria-hidden="true" />
            </button>
          </div>
        </header>
        {children}
      </section>
    </main>
  )
}
