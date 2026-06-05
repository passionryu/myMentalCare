'use client'

import { Bell, Brain, Eye, EyeOff, HeartHandshake, LogOut, MessageCircle, Moon, Settings, Sparkles, UserRound, X } from 'lucide-react'
import { FormEvent, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { LoginApiError, MyProfileResponse, loginMember, readMyProfile, signupMember } from '@/lib/auth-api'

type AuthMode = 'signup' | 'login'
type ThemeTone = 'sunset' | 'cream' | 'rose'

const careFeatures = [
  {
    title: 'AI 마음 대화',
    description: '혼자 감정을 정리하기 어려운 순간에 차분한 질문으로 생각을 풀어냅니다.',
    icon: MessageCircle,
  },
  {
    title: '감정 기록',
    description: '오늘의 컨디션, 감정, 회고를 부담 없이 남기고 흐름을 돌아봅니다.',
    icon: Brain,
  },
  {
    title: '따뜻한 리마인드',
    description: '쉬어야 할 때, 기록해야 할 때, 나를 돌볼 시간을 부드럽게 알려줍니다.',
    icon: Bell,
  },
]

const routineItems = ['아침 마음 체크', '점심 호흡 알림', '잠들기 전 회고']

export default function Page() {
  const [authMode, setAuthMode] = useState<AuthMode | null>(null)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [profile, setProfile] = useState<MyProfileResponse | null>(null)
  const [profileMessage, setProfileMessage] = useState('')
  const [settingsOpen, setSettingsOpen] = useState(false)
  const [serviceGuideOpen, setServiceGuideOpen] = useState(false)
  const [accountGuideOpen, setAccountGuideOpen] = useState(false)
  const [notificationEnabled, setNotificationEnabled] = useState(false)
  const [themeTone, setThemeTone] = useState<ThemeTone>('sunset')

  useEffect(() => {
    setIsAuthenticated(Boolean(localStorage.getItem('myMentalCare.accessToken')))
  }, [])

  const handleLogout = () => {
    localStorage.removeItem('myMentalCare.accessToken')
    localStorage.removeItem('myMentalCare.refreshToken')
    setIsAuthenticated(false)
    setAuthMode(null)
    setProfile(null)
    setProfileMessage('')
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

  return (
    <main className={`page-shell theme-${themeTone}`}>
      <section className="hero-section" aria-labelledby="main-heading">
        <nav className="top-nav" aria-label="주요 메뉴">
          <div className="brand-mark">
            <span className="brand-icon">
              <HeartHandshake size={20} aria-hidden="true" />
            </span>
            <span>myMentalCare</span>
          </div>
          {isAuthenticated ? (
            <div className="nav-actions">
              <button className="soft-button profile-button" type="button" aria-label="내 프로필" onClick={handleOpenProfile}>
                <UserRound size={18} aria-hidden="true" />
                프로필
              </button>
              <button className="ghost-button logout-button" type="button" onClick={handleLogout}>
                <LogOut size={18} aria-hidden="true" />
                로그아웃
              </button>
              <SettingsButton onClick={() => setSettingsOpen(true)} />
            </div>
          ) : (
            <div className="nav-actions">
              <SettingsButton onClick={() => setSettingsOpen(true)} />
              <button className="ghost-button" type="button" onClick={() => setAuthMode('login')}>
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
            <p className="eyebrow">따뜻한 개인 멘탈 케어</p>
            <h1 id="main-heading">오늘의 마음을 조용히 돌보는 나만의 케어 공간</h1>
            <p className="hero-description">
              myMentalCare는 감정 기록, AI 대화, 리마인드 알림을 통해 하루의 마음을 차분히 살피는
              서비스를 목표로 합니다. 먼저 편안한 첫 화면과 인증 흐름부터 준비합니다.
            </p>
          </div>

          <aside className="care-panel" aria-label="오늘의 마음 케어 미리보기">
            <div className="panel-header">
              <span className="panel-icon">
                <Moon size={18} aria-hidden="true" />
              </span>
              <span>오늘의 케어 루틴</span>
            </div>
            <div className="mood-card">
              <p>지금 마음은 어떤 온도인가요?</p>
              <strong>차분함을 회복하는 중</strong>
            </div>
            <ul className="routine-list">
              {routineItems.map((item) => (
                <li key={item}>
                  <Sparkles size={16} aria-hidden="true" />
                  <span>{item}</span>
                </li>
              ))}
            </ul>
          </aside>
        </div>
      </section>

      <section className="feature-section" aria-labelledby="feature-heading">
        <div className="section-heading">
          <p className="eyebrow">서비스 방향</p>
          <h2 id="feature-heading">기록하고, 대화하고, 다시 나를 챙깁니다.</h2>
        </div>
        <div className="feature-grid">
          {careFeatures.map(({ title, description, icon: Icon }) => (
            <article className="feature-card" key={title}>
              <span className="feature-icon">
                <Icon size={22} aria-hidden="true" />
              </span>
              <h3>{title}</h3>
              <p>{description}</p>
            </article>
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
          }}
          onModeChange={setAuthMode}
        />
      )}
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
      {settingsOpen && (
        <SettingsModal
          notificationEnabled={notificationEnabled}
          themeTone={themeTone}
          onClose={() => setSettingsOpen(false)}
          onNotificationChange={setNotificationEnabled}
          onThemeChange={setThemeTone}
          onOpenAccountGuide={() => setAccountGuideOpen(true)}
          onOpenServiceGuide={() => setServiceGuideOpen(true)}
        />
      )}
      {accountGuideOpen && <AccountGuideModal onClose={() => setAccountGuideOpen(false)} />}
      {serviceGuideOpen && <ServiceGuideModal onClose={() => setServiceGuideOpen(false)} />}
    </main>
  )
}

function SettingsButton({ onClick }: { onClick: () => void }) {
  return (
    <button className="settings-button" type="button" aria-label="설정 열기" onClick={onClick}>
      <Settings size={19} aria-hidden="true" />
    </button>
  )
}

function SettingsModal({
  notificationEnabled,
  themeTone,
  onClose,
  onNotificationChange,
  onThemeChange,
  onOpenAccountGuide,
  onOpenServiceGuide,
}: {
  notificationEnabled: boolean
  themeTone: ThemeTone
  onClose: () => void
  onNotificationChange: (enabled: boolean) => void
  onThemeChange: (theme: ThemeTone) => void
  onOpenAccountGuide: () => void
  onOpenServiceGuide: () => void
}) {
  const themes: Array<{ value: ThemeTone; label: string; description: string }> = [
    { value: 'sunset', label: '노을빛', description: '차분한 살구색과 세이지 톤' },
    { value: 'cream', label: '크림빛', description: '밝고 편안한 아이보리 톤' },
    { value: 'rose', label: '장밋빛', description: '부드러운 로즈와 베이지 톤' },
  ]

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section
        className="auth-modal settings-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="settings-modal-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button className="icon-button" type="button" aria-label="설정 닫기" onClick={onClose}>
          <X size={20} aria-hidden="true" />
        </button>
        <p className="eyebrow">내 공간 설정</p>
        <h2 id="settings-modal-title">설정</h2>
        <p className="modal-description">나에게 편안한 방식으로 알림과 화면 분위기를 조정합니다.</p>

        <div className="settings-list">
          <div className="settings-row">
            <div>
              <strong>알림 설정</strong>
              <span>오늘의 마음 체크 알림을 받을지 선택합니다.</span>
            </div>
            <button
              className={`toggle-button ${notificationEnabled ? 'is-on' : ''}`}
              type="button"
              role="switch"
              aria-checked={notificationEnabled}
              onClick={() => onNotificationChange(!notificationEnabled)}
            >
              <span />
            </button>
          </div>

          <div className="settings-group">
            <strong>화면 분위기</strong>
            <span>모두 따뜻한 색감 안에서 원하는 톤을 고릅니다.</span>
            <div className="theme-options">
              {themes.map((theme) => (
                <button
                  className={`theme-option theme-option-${theme.value} ${themeTone === theme.value ? 'is-selected' : ''}`}
                  type="button"
                  key={theme.value}
                  aria-pressed={themeTone === theme.value}
                  onClick={() => onThemeChange(theme.value)}
                >
                  <span className="theme-swatch" aria-hidden="true" />
                  <span>
                    <strong>{theme.label}</strong>
                    <small>{theme.description}</small>
                  </span>
                </button>
              ))}
            </div>
          </div>

          <button className="settings-action" type="button" onClick={onOpenAccountGuide}>
            <strong>계정</strong>
            <span>회원 정보 수정과 탈퇴 프로세스를 확인합니다.</span>
          </button>

          <button className="settings-action" type="button" onClick={onOpenServiceGuide}>
            <strong>서비스 안내</strong>
            <span>myMentalCare가 제공하는 도움의 범위를 확인합니다.</span>
          </button>
        </div>
      </section>
    </div>
  )
}

function AccountGuideModal({ onClose }: { onClose: () => void }) {
  return (
    <GuideModal title="계정 관리 안내" eyebrow="계정" onClose={onClose}>
      <p>회원 정보 수정과 탈퇴는 안전한 본인 확인 과정을 거쳐 제공될 예정입니다.</p>
      <ul className="guide-list">
        <li>회원 정보 수정: 이름, 이메일, 전화번호 변경</li>
        <li>탈퇴 프로세스: 데이터 보관 기간 안내 후 최종 확인</li>
        <li>민감한 계정 변경은 비밀번호 재확인을 요구합니다.</li>
      </ul>
    </GuideModal>
  )
}

function ServiceGuideModal({ onClose }: { onClose: () => void }) {
  return (
    <GuideModal title="서비스 안내" eyebrow="안내" onClose={onClose}>
      <p>myMentalCare는 진단이나 치료를 대신하지 않고, 일상적인 마음 기록과 자기 돌봄을 돕는 서비스입니다.</p>
      <ul className="guide-list">
        <li>AI 대화는 감정 정리를 돕는 보조 도구입니다.</li>
        <li>위기 상황이나 치료가 필요한 경우 전문 기관의 도움을 받아야 합니다.</li>
        <li>알림과 기록은 사용자가 스스로를 돌볼 수 있게 돕는 방향으로 제공됩니다.</li>
      </ul>
    </GuideModal>
  )
}

function GuideModal({
  title,
  eyebrow,
  children,
  onClose,
}: {
  title: string
  eyebrow: string
  children: ReactNode
  onClose: () => void
}) {
  return (
    <div className="modal-backdrop stacked" role="presentation" onMouseDown={onClose}>
      <section
        className="auth-modal guide-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="guide-modal-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button className="icon-button" type="button" aria-label="안내 닫기" onClick={onClose}>
          <X size={20} aria-hidden="true" />
        </button>
        <p className="eyebrow">{eyebrow}</p>
        <h2 id="guide-modal-title">{title}</h2>
        <div className="guide-content">{children}</div>
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
