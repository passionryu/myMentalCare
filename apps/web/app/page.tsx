'use client'

import { Bell, Brain, HeartHandshake, MessageCircle, Moon, Sparkles, X } from 'lucide-react'
import { FormEvent, useState } from 'react'

type AuthMode = 'signup' | 'login'

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

  return (
    <main className="page-shell">
      <section className="hero-section" aria-labelledby="main-heading">
        <nav className="top-nav" aria-label="주요 메뉴">
          <div className="brand-mark">
            <span className="brand-icon">
              <HeartHandshake size={20} aria-hidden="true" />
            </span>
            <span>myMentalCare</span>
          </div>
          <div className="nav-actions">
            <button className="ghost-button" type="button" onClick={() => setAuthMode('login')}>
              로그인
            </button>
            <button className="primary-button" type="button" onClick={() => setAuthMode('signup')}>
              회원가입
            </button>
          </div>
        </nav>

        <div className="hero-grid">
          <div className="hero-copy">
            <p className="eyebrow">따뜻한 개인 멘탈 케어</p>
            <h1 id="main-heading">오늘의 마음을 조용히 돌보는 나만의 케어 공간</h1>
            <p className="hero-description">
              myMentalCare는 감정 기록, AI 대화, 리마인드 알림을 통해 하루의 마음을 차분히 살피는
              서비스를 목표로 합니다. 먼저 편안한 첫 화면과 인증 흐름부터 준비합니다.
            </p>
            <div className="hero-actions">
              <button className="primary-button large" type="button" onClick={() => setAuthMode('signup')}>
                마음 기록 시작하기
              </button>
              <button className="soft-button large" type="button" onClick={() => setAuthMode('login')}>
                이미 계정이 있어요
              </button>
            </div>
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

      <section className="cta-band" aria-label="회원가입 안내">
        <div>
          <p className="eyebrow">첫 번째 단계</p>
          <h2>아직은 화면 구현 단계입니다.</h2>
          <p>회원가입과 로그인은 API 연동 전까지 안전한 안내 메시지만 보여줍니다.</p>
        </div>
        <button className="primary-button large" type="button" onClick={() => setAuthMode('signup')}>
          회원가입 모달 열기
        </button>
      </section>

      {authMode && <AuthModal mode={authMode} onClose={() => setAuthMode(null)} onModeChange={setAuthMode} />}
    </main>
  )
}

function AuthModal({
  mode,
  onClose,
  onModeChange,
}: {
  mode: AuthMode
  onClose: () => void
  onModeChange: (mode: AuthMode) => void
}) {
  const [message, setMessage] = useState('')
  const isSignup = mode === 'signup'

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setMessage(
      isSignup
        ? '회원가입 API 연동 전입니다. 입력 흐름만 안전하게 확인했습니다.'
        : '로그인 API 연동 전입니다. 입력 흐름만 안전하게 확인했습니다.',
    )
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
          <label>
            비밀번호
            <input name="password" type="password" placeholder="비밀번호를 입력하세요" required />
          </label>
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
              {isSignup ? '회원가입' : '로그인'}
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
