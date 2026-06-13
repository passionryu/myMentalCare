'use client'

import { ArrowLeft, ArrowRight, BookOpen, HeartHandshake, Home, MessageCircle, SendHorizontal, ShieldCheck, UserRound } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { useEffect, useRef, useState } from 'react'

type ThemeTone = 'sunset' | 'cream' | 'wood'

const THEME_TONE_STORAGE_KEY = 'myMentalCare.themeTone'
const LOGIN_MODAL_REQUEST_KEY = 'myMentalCare.openLoginModal'

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

export default function ServicePage() {
  const router = useRouter()
  const storyRailRef = useRef<HTMLDivElement | null>(null)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
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
      <nav className="top-nav service-top-nav" aria-label="서비스 소개 메뉴">
        <div className="brand-mark">
          <span className="brand-icon">
            <HeartHandshake size={20} aria-hidden="true" />
          </span>
          <span>Haru Mind</span>
        </div>
        <div className="nav-actions">
          <button className="ghost-button nav-outline-button" type="button" onClick={() => router.push('/')}>
            <ArrowLeft size={18} aria-hidden="true" />
            홈으로
          </button>
          <button className="primary-button" type="button" onClick={handleOpenAiChat}>
            대화 시작
            <ArrowRight size={18} aria-hidden="true" />
          </button>
        </div>
      </nav>

      <section className="service-intro-section" aria-labelledby="service-intro-heading">
        <p className="eyebrow">서비스 소개</p>
        <h1 id="service-intro-heading">Haru Mind가 마음을 정리하는 방식</h1>
        <p>메인에서는 바로 시작하고, 이곳에서는 서비스가 어떤 범위와 방식으로 도움을 주는지 확인합니다.</p>
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
