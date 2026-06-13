'use client'

import { ArrowRight, BookOpen, ClipboardCheck, Home, MessageCircle, SendHorizontal, ShieldCheck, UserRound } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'

type ThemeTone = 'sunset' | 'cream' | 'wood'

const THEME_TONE_STORAGE_KEY = 'myMentalCare.themeTone'
const LOGIN_MODAL_REQUEST_KEY = 'myMentalCare.openLoginModal'

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

const flowSteps = [
  {
    step: '1',
    title: '체크인으로 시작',
    description: '지금 상태를 가볍게 고르고 대화의 출발점을 만듭니다.',
    meta: ['기본 감정형', '대화 시작형', '컨디션', '하루 회고'],
  },
  {
    step: '2',
    title: '오늘의 대화 이어가기',
    description: '마음이는 오늘의 흐름 안에서 생각과 감정을 차분히 정리합니다.',
    meta: ['오늘의 대화', '새 주제', '마음이 답변'],
  },
  {
    step: '3',
    title: '마음 리포트로 남기기',
    description: '대화가 충분하면 확인된 내용을 바탕으로 오늘의 기록을 남깁니다.',
    meta: ['대화 요약', '마음 흐름', '추천 노래'],
  },
] as const

export default function ServicePage() {
  const router = useRouter()
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [themeTone, setThemeTone] = useState<ThemeTone>('sunset')
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
    if (reducedMotion) {
      setExampleWindowIndex(0)
      return
    }

    const intervalId = window.setInterval(() => {
      setExampleWindowIndex((currentIndex) => (currentIndex + 1) % exampleConversationMessages.length)
    }, 3000)

    return () => window.clearInterval(intervalId)
  }, [reducedMotion])

  const handleOpenAiChat = () => {
    if (!isAuthenticated) {
      sessionStorage.setItem(LOGIN_MODAL_REQUEST_KEY, '1')
      router.push('/')
      return
    }

    router.push('/chat')
  }

  const handleOpenMyPage = () => {
    if (!isAuthenticated) {
      sessionStorage.setItem(LOGIN_MODAL_REQUEST_KEY, '1')
      router.push('/')
      return
    }

    router.push('/mypage')
  }

  return (
    <main className="page-shell service-page-shell" data-theme-tone={themeTone}>
      <div className="service-page-frame">
        <nav className="top-nav service-top-nav" aria-label="서비스 소개 메뉴">
          <div className="brand-mark service-text-brand">
            <span>Haru Mind</span>
          </div>
          <div className="nav-actions">
            <button className="ghost-button nav-outline-button" type="button" onClick={() => router.push('/')}>
              <Home size={18} aria-hidden="true" />
              홈으로
            </button>
            <button className="ghost-button nav-outline-button" type="button" onClick={handleOpenMyPage}>
              <UserRound size={18} aria-hidden="true" />
              마이페이지
            </button>
          </div>
        </nav>

        <section className="service-intro-section service-brand-intro" aria-labelledby="service-intro-heading">
          <div className="service-brand-copy">
            <p className="eyebrow">서비스 소개</p>
            <h1 className="hero-brand-logo service-hero-logo" id="service-intro-heading">Haru Mind</h1>
            <p>체크인으로 지금 상태를 가볍게 고르고, 마음이는 오늘의 대화를 이어가며 생각과 감정을 정리해줍니다.</p>
          </div>
          <div className="service-mascot-card" aria-hidden="true">
            <img className="service-mascot-illustration" src="/maeumi-service-hero.svg" alt="" />
          </div>
        </section>

        <section className="dialogue-section" aria-labelledby="dialogue-heading">
          <div className="section-heading">
            <h2 id="dialogue-heading">오늘 마음이 정리되는 흐름</h2>
          </div>
          <div className="flow-grid" aria-label="마음 정리 흐름">
            {flowSteps.map((flow, index) => (
              <div className="flow-step-group" key={flow.step}>
                <article className={`flow-card flow-card-${flow.step}`}>
                  <div className="flow-card-heading">
                    <span className="flow-card-index">{flow.step}</span>
                    <div>
                      <strong>{flow.title}</strong>
                      <p>{flow.description}</p>
                    </div>
                  </div>
                  {flow.step === '1' && (
                    <div className="flow-card-tags" aria-hidden="true">
                      {flow.meta.map((item) => (
                        <span key={item}>{item}</span>
                      ))}
                    </div>
                  )}
                  {flow.step === '2' && (
                    <div className="flow-chat-preview" aria-hidden="true">
                      <span className="flow-avatar">
                        <img src="/maeumi-avatar.svg" alt="" />
                      </span>
                      <span className="flow-ai-bubble">무슨 일이 있었는지 조금 더 이야기해볼까요?</span>
                      <span className="flow-user-bubble">오늘 일이 많아서 많이 지쳤어요.</span>
                    </div>
                  )}
                  {flow.step === '3' && (
                    <div className="flow-report-preview" aria-hidden="true">
                      <div>
                        <strong>오늘 마음 요약</strong>
                        <p>오늘은 업무 과부하로 지쳤지만, 하루를 마무리하려는 의지가 보였어요.</p>
                      </div>
                      <span>
                        <ClipboardCheck size={42} aria-hidden="true" />
                      </span>
                    </div>
                  )}
                </article>
                {index < flowSteps.length - 1 && (
                  <span className="flow-arrow" aria-hidden="true">
                    <ArrowRight size={28} />
                  </span>
                )}
              </div>
            ))}
          </div>
        </section>

        <div className="service-detail-grid">
          <section className="example-section" aria-labelledby="example-heading">
            <div className="section-heading">
              <h2 id="example-heading">마음이와 함께 마음을 정리하세요</h2>
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
                <span>업무/학교</span>
                <span>인간관계</span>
                <span>가족</span>
                <span>건강</span>
                <span>기타</span>
              </div>
              <div className="decorative-chat-input" aria-hidden="true">
                <span>지금 마음에 떠오르는 말을 적어보세요.</span>
                <span className="decorative-send-button">
                  <SendHorizontal size={18} aria-hidden="true" />
                </span>
              </div>
            </div>
          </section>

          <section className="safety-section" aria-labelledby="safety-heading">
            <div>
              <h2 id="safety-heading">안전 안내</h2>
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
        </div>

        <section className="cta-band" aria-labelledby="cta-heading">
          <div>
            <h2 id="cta-heading">마음이와 대화 시작하기</h2>
            <p>길게 설명하지 않아도 됩니다. 떠오르는 문장부터 시작하면 됩니다.</p>
          </div>
          <button className="primary-button large" type="button" onClick={handleOpenAiChat}>
            AI 마음대화 시작
            <ArrowRight size={18} aria-hidden="true" />
          </button>
        </section>
      </div>

      <nav className="mobile-bottom-nav" aria-label="모바일 주요 메뉴">
        <button className="mobile-tab-button" type="button" onClick={() => router.push('/')}>
          <Home size={18} aria-hidden="true" />
          <span>홈</span>
        </button>
        <button className="mobile-tab-button" type="button" onClick={handleOpenAiChat}>
          <MessageCircle size={18} aria-hidden="true" />
          <span>대화</span>
        </button>
        <button className="mobile-tab-button is-active" type="button" aria-current="page">
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
