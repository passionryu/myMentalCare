'use client'

import {
  ArrowRight,
  BookOpen,
  CheckCircle2,
  Eye,
  EyeOff,
  HeartHandshake,
  Home,
  LogOut,
  MessageCircle,
  Sparkles,
  UserRound,
  X,
} from 'lucide-react'
import { useRouter } from 'next/navigation'
import { FormEvent, useEffect, useState } from 'react'
import { LoginApiError, MyProfileResponse, loginMember, readMyProfile, signupMember } from '@/lib/auth-api'
import { CHECK_IN_TEMPLATES, PENDING_CHECK_IN_TEMPLATE_STORAGE_KEY } from '@/lib/check-in-templates'
import type { CheckInTemplateDefinition } from '@/lib/check-in-templates'

type AuthMode = 'signup' | 'login'
type ThemeTone = 'sunset' | 'cream' | 'wood'
type AuthNotice = {
  eyebrow: string
  title: string
  description: string
}
const THEME_TONE_STORAGE_KEY = 'myMentalCare.themeTone'
const LOGIN_MODAL_REQUEST_KEY = 'myMentalCare.openLoginModal'

const trustMessages = ['개인 대화 공간', '대화 흐름 저장 가능', '언제든 종료 가능']

export default function Page() {
  const router = useRouter()
  const [authMode, setAuthMode] = useState<AuthMode | null>(null)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [profile, setProfile] = useState<MyProfileResponse | null>(null)
  const [profileMessage, setProfileMessage] = useState('')
  const [themeTone, setThemeTone] = useState<ThemeTone>('sunset')
  const [authNotice, setAuthNotice] = useState<AuthNotice | null>(null)

  useEffect(() => {
    setIsAuthenticated(Boolean(localStorage.getItem('myMentalCare.accessToken')))
    if (sessionStorage.getItem(LOGIN_MODAL_REQUEST_KEY) === '1') {
      sessionStorage.removeItem(LOGIN_MODAL_REQUEST_KEY)
      setAuthMode('login')
    }
    const savedThemeTone = localStorage.getItem(THEME_TONE_STORAGE_KEY)
    if (savedThemeTone === 'rose') {
      setThemeTone('wood')
      localStorage.setItem(THEME_TONE_STORAGE_KEY, 'wood')
      return
    }
    if (savedThemeTone === 'sunset' || savedThemeTone === 'cream' || savedThemeTone === 'wood') {
      setThemeTone(savedThemeTone)
    }
  }, [])

  const handleLogout = () => {
    localStorage.removeItem('myMentalCare.accessToken')
    localStorage.removeItem('myMentalCare.refreshToken')
    setIsAuthenticated(false)
    setAuthMode(null)
    setProfile(null)
    setProfileMessage('')
    setAuthNotice({
      eyebrow: '로그아웃 완료',
      title: '로그아웃되었습니다',
      description: '다시 이용하려면 상단의 로그인 버튼을 눌러주세요.',
    })
  }

  const handleOpenProfile = async () => {
    const accessToken = localStorage.getItem('myMentalCare.accessToken')
    if (!accessToken) {
      setIsAuthenticated(false)
      setAuthMode('login')
      return
    }

    try {
      setProfileMessage('')
      setProfile(await readMyProfile(accessToken))
    } catch (error) {
      setProfileMessage(error instanceof LoginApiError ? error.message : '프로필 정보를 불러오지 못했습니다.')
    }
  }

  const handleOpenMyPage = () => {
    if (!isAuthenticated) {
      setAuthMode('login')
      return
    }

    router.push('/mypage')
  }

  const handleOpenAiChat = () => {
    if (!isAuthenticated) {
      setAuthMode('login')
      return
    }

    router.push('/chat')
  }

  const handleStartCheckIn = (template: CheckInTemplateDefinition) => {
    if (!isAuthenticated) {
      setAuthMode('login')
      return
    }

    sessionStorage.setItem(PENDING_CHECK_IN_TEMPLATE_STORAGE_KEY, template.type)
    router.push(`/chat?checkInTemplate=${template.type}`)
  }

  return (
    <main className="page-shell" data-theme-tone={themeTone}>
      <section className="hero-section" aria-labelledby="main-heading">
        <nav className="top-nav" aria-label="주요 메뉴">
          <div className="brand-mark">
            <span className="brand-icon">
              <HeartHandshake size={20} aria-hidden="true" />
            </span>
            <span>Haru Mind</span>
          </div>
          {isAuthenticated ? (
            <div className="nav-actions">
              <button className="ghost-button service-nav-button" type="button" onClick={() => router.push('/service')}>
                <BookOpen size={18} aria-hidden="true" />
                서비스 소개
              </button>
              <button className="soft-button profile-button" type="button" aria-label="마이페이지" onClick={handleOpenMyPage}>
                <UserRound size={18} aria-hidden="true" />
                마이페이지
              </button>
              <button className="ghost-button logout-button" type="button" onClick={handleLogout}>
                <LogOut size={18} aria-hidden="true" />
                로그아웃
              </button>
            </div>
          ) : (
            <div className="nav-actions">
              <button className="ghost-button service-nav-button" type="button" onClick={() => router.push('/service')}>
                <BookOpen size={18} aria-hidden="true" />
                서비스 소개
              </button>
              <button className="ghost-button mobile-login-button" type="button" onClick={() => setAuthMode('login')}>
                로그인
              </button>
              <button className="primary-button" type="button" onClick={() => setAuthMode('signup')}>
                회원가입
              </button>
            </div>
          )}
        </nav>

        <div className="hero-grid">
          <div className="hero-copy">
            <p className="eyebrow">AI 마음대화 · 따뜻한 개인 멘탈 케어</p>
            <h1 id="main-heading">말하기 어려운 마음부터 천천히 시작합니다</h1>
            <p className="hero-description">
              대화는 진단이나 치료가 아니라, 내 생각을 안전하게 정리하는 개인 공간입니다. 필요한 만큼 짧게 말해도 됩니다.
            </p>
            <ul className="trust-list" aria-label="AI 마음대화 신뢰 안내">
              {trustMessages.map((message) => (
                <li key={message}>
                  <CheckCircle2 size={17} aria-hidden="true" />
                  <span>{message}</span>
                </li>
              ))}
            </ul>
            <div className="chat-start-box">
              <span>지금 떠오르는 문장을 한 줄만 적어보세요</span>
              <button className="primary-button" type="button" onClick={handleOpenAiChat}>
                AI와 대화하기
                <ArrowRight size={18} aria-hidden="true" />
              </button>
            </div>
          </div>

        </div>
      </section>

      <section className="prompt-section" aria-labelledby="prompt-heading">
        <div className="section-heading">
          <p className="eyebrow">체크인으로 시작하기</p>
          <h2 id="prompt-heading">지금 필요한 방식으로 바로 시작하세요</h2>
          <p>기존 체크인 모달로 이어져 짧게 상태를 고른 뒤, 같은 흐름에서 AI 마음대화를 시작합니다.</p>
        </div>
        <div className="prompt-grid">
          {CHECK_IN_TEMPLATES.map((template) => (
            <button className="prompt-card checkin-prompt-card" type="button" key={template.type} onClick={() => handleStartCheckIn(template)}>
              <Sparkles size={18} aria-hidden="true" />
              <span>
                <strong>{template.title}</strong>
                <small>{template.description}</small>
              </span>
            </button>
          ))}
        </div>
      </section>

      {authMode && (
        <AuthModal
          mode={authMode}
          onClose={() => setAuthMode(null)}
          onLoginSuccess={() => {
            setIsAuthenticated(true)
            setAuthMode(null)
            setAuthNotice({
              eyebrow: '로그인 완료',
              title: '로그인되었습니다',
              description: '이제 AI 마음대화와 마이페이지를 이용할 수 있습니다.',
            })
          }}
          onModeChange={setAuthMode}
        />
      )}
      {authNotice && <StatusNoticeModal notice={authNotice} onClose={() => setAuthNotice(null)} />}
      {(profile || profileMessage) && (
        <ProfileModal
          profile={profile}
          message={profileMessage}
          onClose={() => {
            setProfile(null)
            setProfileMessage('')
          }}
        />
      )}
      <nav className="mobile-bottom-nav" aria-label="모바일 주요 메뉴">
        <button className="mobile-tab-button is-active" type="button" aria-current="page">
          <Home size={18} aria-hidden="true" />
          <span>홈</span>
        </button>
        <button className="mobile-tab-button" type="button" onClick={handleOpenAiChat}>
          <MessageCircle size={18} aria-hidden="true" />
          <span>대화</span>
        </button>
        <button className="mobile-tab-button" type="button" onClick={() => router.push('/service')}>
          <BookOpen size={18} aria-hidden="true" />
          <span>소개</span>
        </button>
        <button className="mobile-tab-button" type="button" onClick={handleOpenMyPage}>
          <UserRound size={18} aria-hidden="true" />
          <span>나</span>
        </button>
      </nav>
    </main>
  )
}

function StatusNoticeModal({ notice, onClose }: { notice: AuthNotice; onClose: () => void }) {
  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section
        className="auth-modal status-notice-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="status-notice-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button className="icon-button" type="button" aria-label="안내 닫기" onClick={onClose}>
          <X size={20} aria-hidden="true" />
        </button>
        <span className="status-notice-icon" aria-hidden="true">
          <CheckCircle2 size={24} />
        </span>
        <p className="eyebrow">{notice.eyebrow}</p>
        <h2 id="status-notice-title">{notice.title}</h2>
        <p className="modal-description">{notice.description}</p>
        <div className="modal-actions">
          <button className="primary-button" type="button" onClick={onClose}>
            확인
          </button>
        </div>
      </section>
    </div>
  )
}

function ProfileModal({
  profile,
  message,
  onClose,
}: {
  profile: MyProfileResponse | null
  message: string
  onClose: () => void
}) {
  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section
        className="auth-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="profile-modal-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button className="icon-button" type="button" aria-label="모달 닫기" onClick={onClose}>
          <X size={20} aria-hidden="true" />
        </button>
        <p className="eyebrow">내 마음 케어 정보</p>
        <h2 id="profile-modal-title">내 프로필</h2>
        {message && <p className="form-message">{message}</p>}
        {profile && (
          <dl className="profile-detail">
            <div>
              <dt>이름</dt>
              <dd>{profile.name}</dd>
            </div>
            <div>
              <dt>로그인 아이디</dt>
              <dd>{profile.loginId}</dd>
            </div>
            <div>
              <dt>이메일</dt>
              <dd>{profile.email || '등록하지 않음'}</dd>
            </div>
            <div>
              <dt>전화번호</dt>
              <dd>{profile.phone || '등록하지 않음'}</dd>
            </div>
          </dl>
        )}
        <div className="modal-actions">
          <button className="primary-button" type="button" onClick={onClose}>
            확인
          </button>
        </div>
      </section>
    </div>
  )
}

function AuthModal({
  mode,
  onClose,
  onLoginSuccess,
  onModeChange,
}: {
  mode: AuthMode
  onClose: () => void
  onLoginSuccess: () => void
  onModeChange: (mode: AuthMode) => void
}) {
  const [message, setMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [showPasswordConfirm, setShowPasswordConfirm] = useState(false)
  const isSignup = mode === 'signup'

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setMessage('')

    if (isSignup) {
      const formData = new FormData(event.currentTarget)
      const loginId = String(formData.get('loginId') ?? '').trim()
      const email = String(formData.get('email') ?? '').trim()
      const password = String(formData.get('password') ?? '')
      const passwordConfirm = String(formData.get('passwordConfirm') ?? '')
      const name = String(formData.get('name') ?? '').trim()
      const phone = String(formData.get('phone') ?? '').trim()

      if (password !== passwordConfirm) {
        setMessage('비밀번호가 서로 다릅니다. 다시 확인해주세요.')
        return
      }

      setIsSubmitting(true)
      try {
        await signupMember({
          loginId,
          email: email || undefined,
          password,
          name,
          phone: phone || undefined,
        })
        setMessage('회원가입이 완료되었습니다. 이제 로그인해주세요.')
        setTimeout(() => onModeChange('login'), 700)
      } catch (error) {
        setMessage(error instanceof LoginApiError ? error.message : '회원가입 처리 중 문제가 발생했습니다.')
      } finally {
        setIsSubmitting(false)
      }
      return
    }

    const formData = new FormData(event.currentTarget)
    const identifier = String(formData.get('loginId') ?? '').trim()
    const password = String(formData.get('password') ?? '')

    setIsSubmitting(true)
    try {
      const response = await loginMember({ identifier, password })
      localStorage.setItem('myMentalCare.accessToken', response.accessToken)
      localStorage.setItem('myMentalCare.refreshToken', response.refreshToken)
      setMessage('로그인이 완료되었습니다.')
      onLoginSuccess()
    } catch (error) {
      setMessage(error instanceof LoginApiError ? error.message : '로그인 처리 중 문제가 발생했습니다.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section
        className="auth-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="auth-modal-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button className="icon-button" type="button" aria-label="모달 닫기" onClick={onClose}>
          <X size={20} aria-hidden="true" />
        </button>
        <p className="eyebrow">{isSignup ? '새 계정 만들기' : '다시 만나서 반가워요'}</p>
        <h2 id="auth-modal-title">{isSignup ? '회원가입' : '로그인'}</h2>
        <p className="modal-description">
          {isSignup
            ? '이름과 로그인 정보를 입력해 마음 케어 공간을 준비합니다.'
            : '계정 정보를 입력해 나의 마음 기록 공간으로 돌아갑니다.'}
        </p>

        <form className="auth-form" onSubmit={handleSubmit}>
          {isSignup && (
            <label>
              이름
              <input name="name" type="text" placeholder="예: 류성열" required />
            </label>
          )}
          <label>
            {isSignup ? '로그인 아이디' : '아이디 또는 이메일'}
            <input name="loginId" type="text" placeholder={isSignup ? '예: mentalcare_user' : '아이디 또는 이메일'} required />
          </label>
          {isSignup && (
            <label>
              이메일 <span className="optional">(선택)</span>
              <input name="email" type="email" placeholder="예: care@example.com" />
            </label>
          )}
          {isSignup && (
            <label>
              전화번호 <span className="optional">(선택)</span>
              <input name="phone" type="tel" placeholder="예: 01012345678" />
            </label>
          )}
          <PasswordField
            name="password"
            label="비밀번호"
            placeholder="비밀번호를 입력하세요"
            visible={showPassword}
            onVisibleChange={setShowPassword}
          />
          {isSignup && (
            <PasswordField
              name="passwordConfirm"
              label="비밀번호 확인"
              placeholder="비밀번호를 한 번 더 입력하세요"
              visible={showPasswordConfirm}
              onVisibleChange={setShowPasswordConfirm}
            />
          )}
          {isSignup && (
            <label>
              관심 케어 주제
              <select name="interest" defaultValue="daily">
                <option value="daily">감정 기록과 하루 회고</option>
                <option value="chat">AI 마음 대화</option>
                <option value="reminder">알림과 루틴 관리</option>
              </select>
            </label>
          )}

          {message && <p className="form-message">{message}</p>}

          <div className="modal-actions">
            <button className="soft-button" type="button" onClick={onClose}>
              취소
            </button>
            <button className="primary-button" type="submit">
              {isSubmitting ? '처리 중...' : isSignup ? '회원가입' : '로그인'}
            </button>
          </div>
        </form>

        <button className="switch-mode" type="button" onClick={() => onModeChange(isSignup ? 'login' : 'signup')}>
          {isSignup ? '이미 계정이 있다면 로그인' : '처음이라면 회원가입'}
        </button>
      </section>
    </div>
  )
}

function PasswordField({
  name,
  label,
  placeholder,
  visible,
  onVisibleChange,
}: {
  name: string
  label: string
  placeholder: string
  visible: boolean
  onVisibleChange: (visible: boolean) => void
}) {
  return (
    <label>
      {label}
      <span className="password-field">
        <input name={name} type={visible ? 'text' : 'password'} placeholder={placeholder} required />
        <button
          className="password-toggle"
          type="button"
          aria-label={visible ? `${label} 숨기기` : `${label} 보기`}
          onClick={() => onVisibleChange(!visible)}
        >
          {visible ? <EyeOff size={18} aria-hidden="true" /> : <Eye size={18} aria-hidden="true" />}
        </button>
      </span>
    </label>
  )
}
