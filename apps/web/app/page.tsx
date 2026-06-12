'use client'

import {
  ArrowRight,
  CheckCircle2,
  ClipboardList,
  Eye,
  EyeOff,
  HeartHandshake,
  Home,
  LogOut,
  MessageCircle,
  Settings,
  ShieldCheck,
  Sparkles,
  UserRound,
  X,
} from 'lucide-react'
import { useRouter } from 'next/navigation'
import { FormEvent, useEffect, useRef, useState } from 'react'
import type { ReactNode } from 'react'
import { LoginApiError, MyProfileResponse, loginMember, readMyProfile, signupMember } from '@/lib/auth-api'
import { CHECK_IN_TEMPLATES, PENDING_CHECK_IN_SELECTOR_STORAGE_KEY, PENDING_CHECK_IN_TEMPLATE_STORAGE_KEY } from '@/lib/check-in-templates'
import type { CheckInTemplateDefinition } from '@/lib/check-in-templates'

type AuthMode = 'signup' | 'login'
type ThemeTone = 'sunset' | 'cream' | 'wood'
const THEME_TONE_STORAGE_KEY = 'myMentalCare.themeTone'

const trustMessages = ['개인 대화 공간', '대화 흐름 저장 가능', '언제든 종료 가능']
const storyMessages = [
  { speaker: 'user', message: '머릿속이 계속 복잡해서 어디서부터 말해야 할지 모르겠어요.' },
  { speaker: 'ai', message: '가장 크게 남아 있는 한 문장만 골라볼게요. 지금 제일 먼저 떠오르는 건 무엇인가요?' },
  { speaker: 'user', message: '해야 할 일은 많은데 계속 미루고 있어요.' },
  { speaker: 'ai', message: '해야 하는 일과 지금 부담스러운 감정을 나눠서 정리해볼 수 있어요.' },
  { speaker: 'user', message: '그렇게 나누면 조금 덜 막막할 것 같아요.' },
  { speaker: 'ai', message: '좋아요. 오늘 꼭 필요한 일 하나와 나중으로 미뤄도 되는 일을 구분해볼게요.' },
] as const
const exampleConversationMessages = [
  { id: 1, speaker: 'ai', message: '오늘은 어떤 이야기를 먼저 꺼내보고 싶나요?' },
  { id: 2, speaker: 'user', message: '요즘 계속 피곤한데, 이유를 잘 모르겠어요.' },
  { id: 3, speaker: 'ai', message: '몸의 피로와 마음의 긴장을 나눠서 살펴볼게요. 최근에 유독 부담이 커진 일이 있었나요?' },
  { id: 4, speaker: 'user', message: '할 일은 많은데 시작을 못 하고 있어요.' },
  { id: 5, speaker: 'ai', message: '시작하지 못하는 마음 뒤에 있는 부담을 먼저 작게 나눠볼게요.' },
  { id: 6, speaker: 'user', message: '실패할까 봐 자꾸 미루는 것 같아요.' },
  { id: 7, speaker: 'ai', message: '그 마음은 자연스러워요. 오늘은 성공보다 시작 가능한 크기로 줄여보면 어떨까요?' },
  { id: 8, speaker: 'user', message: '그럼 10분만 해보는 건 가능할 것 같아요.' },
  { id: 9, speaker: 'ai', message: '좋아요. 10분 뒤에 멈춰도 괜찮다는 조건으로 시작해볼 수 있어요.' },
  { id: 10, speaker: 'user', message: '지금 정리된 한 문장은 "작게 시작하면 부담이 줄어든다"에 가까워 보여요.' },
] as const
const safetyGuides = [
  '의료 진단이나 치료를 대신하지 않습니다.',
  '위급 상황 알림이나 의료 대응을 대신하지 않습니다.',
  '대화는 언제든 멈출 수 있습니다.',
]

export default function Page() {
  const router = useRouter()
  const storyRailRef = useRef<HTMLDivElement | null>(null)
  const [authMode, setAuthMode] = useState<AuthMode | null>(null)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [profile, setProfile] = useState<MyProfileResponse | null>(null)
  const [profileMessage, setProfileMessage] = useState('')
  const [settingsOpen, setSettingsOpen] = useState(false)
  const [serviceGuideOpen, setServiceGuideOpen] = useState(false)
  const [accountGuideOpen, setAccountGuideOpen] = useState(false)
  const [notificationEnabled, setNotificationEnabled] = useState(false)
  const [themeTone, setThemeTone] = useState<ThemeTone>('sunset')
  const [storyInView, setStoryInView] = useState(false)
  const [reducedMotion, setReducedMotion] = useState(false)
  const [exampleWindowIndex, setExampleWindowIndex] = useState(0)

  useEffect(() => {
    setIsAuthenticated(Boolean(localStorage.getItem('myMentalCare.accessToken')))
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

  useEffect(() => {
    const motionQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
    const syncMotionPreference = () => setReducedMotion(motionQuery.matches)
    syncMotionPreference()
    motionQuery.addEventListener('change', syncMotionPreference)

    return () => motionQuery.removeEventListener('change', syncMotionPreference)
  }, [])

  useEffect(() => {
    const target = storyRailRef.current
    if (!target || reducedMotion) {
      setStoryInView(true)
      return
    }

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setStoryInView(true)
          observer.disconnect()
        }
      },
      { rootMargin: '0px 0px -16% 0px', threshold: 0.24 },
    )

    observer.observe(target)
    return () => observer.disconnect()
  }, [reducedMotion])

  useEffect(() => {
    if (reducedMotion) {
      setExampleWindowIndex(0)
      return
    }

    const intervalId = window.setInterval(() => {
      setExampleWindowIndex((currentIndex) => (currentIndex + 1) % exampleConversationMessages.length)
    }, 3000)

    return () => window.clearInterval(intervalId)
  }, [reducedMotion])

  const handleThemeChange = (nextThemeTone: ThemeTone) => {
    setThemeTone(nextThemeTone)
    localStorage.setItem(THEME_TONE_STORAGE_KEY, nextThemeTone)
  }

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

  const handleOpenCheckInSelector = () => {
    if (!isAuthenticated) {
      setAuthMode('login')
      return
    }

    sessionStorage.setItem(PENDING_CHECK_IN_SELECTOR_STORAGE_KEY, '1')
    router.push('/chat?checkInSelector=1')
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

      <section className="dialogue-section" aria-labelledby="dialogue-heading">
        <div className="section-heading">
          <p className="eyebrow">서비스 설명</p>
          <h2 id="dialogue-heading">마음속 문장이 천천히 선명해집니다</h2>
          <p>복잡한 감정을 기능 카드로 나열하지 않고, 실제 대화가 정리로 바뀌는 흐름을 보여줍니다.</p>
        </div>
        <div className={`story-rail ${storyInView ? 'is-visible' : ''}`} ref={storyRailRef}>
          {storyMessages.map((story) => (
            <div className={`story-bubble ${story.speaker}`} key={story.message}>
              "{story.message}"
            </div>
          ))}
        </div>
      </section>

      <section className="example-section" aria-labelledby="example-heading">
        <div className="section-heading">
          <p className="eyebrow">대화 예시</p>
          <h2 id="example-heading">짧게 말해도 흐름을 잡아줍니다</h2>
        </div>
        <div className="example-card">
          <div className="example-bubble-window" aria-label="AI 마음대화 예시">
            {[0, 1, 2].map((offset) => {
              const message = exampleConversationMessages[(exampleWindowIndex + offset) % exampleConversationMessages.length]
              return (
                <div
                  className={`preview-bubble ${message.speaker === 'ai' ? 'is-ai' : 'is-user'}`}
                  key={`${exampleWindowIndex}-${message.id}`}
                >
                  {message.message}
                </div>
              )
            })}
          </div>
          <div className="topic-tags" aria-label="추천 대화 주제">
            <span>생각 정리하기</span>
            <span>불안 낮추기</span>
            <span>잠들기 전 대화</span>
          </div>
          <button className="primary-button" type="button" onClick={handleOpenAiChat}>
            AI와 대화하기
            <ArrowRight size={18} aria-hidden="true" />
          </button>
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

      <section className="safety-section" aria-labelledby="safety-heading">
        <div>
          <p className="eyebrow">안전 안내</p>
          <h2 id="safety-heading">AI 마음대화가 도울 수 있는 범위를 분명히 합니다</h2>
        </div>
        <ul className="safety-list">
          {safetyGuides.map((guide) => (
            <li key={guide}>
              <ShieldCheck size={18} aria-hidden="true" />
              <span>{guide}</span>
            </li>
          ))}
        </ul>
      </section>

      <section className="cta-band" aria-labelledby="cta-heading">
        <div>
          <p className="eyebrow">지금 시작</p>
          <h2 id="cta-heading">지금 한 문장으로 마음을 정리해보세요</h2>
          <p>길게 설명하지 않아도 됩니다. 떠오르는 문장부터 시작하면 됩니다.</p>
        </div>
        <button className="primary-button large" type="button" onClick={handleOpenAiChat}>
          AI 마음대화 시작
          <ArrowRight size={18} aria-hidden="true" />
        </button>
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
          onThemeChange={handleThemeChange}
          onOpenAccountGuide={() => setAccountGuideOpen(true)}
          onOpenServiceGuide={() => setServiceGuideOpen(true)}
        />
      )}
      {accountGuideOpen && <AccountGuideModal onClose={() => setAccountGuideOpen(false)} />}
      {serviceGuideOpen && <ServiceGuideModal onClose={() => setServiceGuideOpen(false)} />}
      <nav className="mobile-bottom-nav" aria-label="모바일 주요 메뉴">
        <button className="mobile-tab-button is-active" type="button" aria-current="page">
          <Home size={18} aria-hidden="true" />
          <span>홈</span>
        </button>
        <button className="mobile-tab-button" type="button" onClick={handleOpenAiChat}>
          <MessageCircle size={18} aria-hidden="true" />
          <span>대화</span>
        </button>
        <button className="mobile-tab-button" type="button" onClick={handleOpenCheckInSelector}>
          <ClipboardList size={18} aria-hidden="true" />
          <span>체크인</span>
        </button>
        <button className="mobile-tab-button" type="button" onClick={handleOpenProfile}>
          <UserRound size={18} aria-hidden="true" />
          <span>나</span>
        </button>
      </nav>
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
    { value: 'wood', label: '우드빛', description: '내추럴 우드와 아이보리 톤' },
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
            <div className="settings-control-text">
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
            <div className="settings-control-text">
              <strong>화면 색상</strong>
              <span>노을빛, 크림빛, 우드빛 중 나에게 편안한 화면 분위기를 선택합니다.</span>
            </div>
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
            <span>Haru Mind가 제공하는 도움의 범위를 확인합니다.</span>
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
      <div className="account-action-grid">
        <button className="settings-action" type="button">
          <strong>회원 정보 수정</strong>
          <span>이름, 이메일, 전화번호를 수정하는 화면으로 연결됩니다.</span>
        </button>
        <button className="settings-action danger" type="button">
          <strong>회원 탈퇴</strong>
          <span>데이터 보관 안내와 비밀번호 재확인 후 탈퇴를 진행합니다.</span>
        </button>
      </div>
    </GuideModal>
  )
}

function ServiceGuideModal({ onClose }: { onClose: () => void }) {
  return (
    <GuideModal title="서비스 안내" eyebrow="안내" onClose={onClose}>
      <p>Haru Mind는 진단이나 치료를 대신하지 않고, 일상적인 마음 기록과 자기 돌봄을 돕는 서비스입니다.</p>
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
