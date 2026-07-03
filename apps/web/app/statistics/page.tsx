'use client'

import {
  ArrowLeft,
  BarChart3,
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  HeartPulse,
  Home,
  Loader2,
  MessageCircle,
  Sparkles,
} from 'lucide-react'
import { useRouter } from 'next/navigation'
import { useEffect, useMemo, useState } from 'react'
import { LoginApiError } from '@/lib/auth-api'
import {
  MindStatisticsCalendar,
  MindStatisticsCalendarDay,
  MindStatisticsDayDetail,
  MindStatisticsOverview,
  readMindStatisticsCalendar,
  readMindStatisticsDayDetail,
  readMindStatisticsOverview,
} from '@/lib/mind-statistics-api'
import { DEFAULT_THEME_TONE, readStoredThemeTone, ThemeTone } from '@/lib/theme-tone'

const LOGIN_MODAL_REQUEST_KEY = 'myMentalCare.openLoginModal'

export default function StatisticsPage() {
  const router = useRouter()
  const [themeTone, setThemeTone] = useState<ThemeTone>(DEFAULT_THEME_TONE)
  const [month, setMonth] = useState(() => toYearMonth(new Date()))
  const [selectedDate, setSelectedDate] = useState(() => toDateKey(new Date()))
  const [calendar, setCalendar] = useState<MindStatisticsCalendar | null>(null)
  const [overview, setOverview] = useState<MindStatisticsOverview | null>(null)
  const [dayDetail, setDayDetail] = useState<MindStatisticsDayDetail | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isDayLoading, setIsDayLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    setThemeTone(readStoredThemeTone())

    if (!localStorage.getItem('myMentalCare.accessToken')) {
      sessionStorage.setItem(LOGIN_MODAL_REQUEST_KEY, '1')
      router.replace('/')
      return
    }
  }, [router])

  useEffect(() => {
    setIsLoading(true)
    setErrorMessage('')

    Promise.all([readMindStatisticsCalendar(month), readMindStatisticsOverview(4)])
      .then(([calendarResponse, overviewResponse]) => {
        setCalendar(calendarResponse)
        setOverview(overviewResponse)
      })
      .catch((error) => {
        setErrorMessage(error instanceof LoginApiError ? error.message : '마음 통계를 불러오지 못했습니다.')
      })
      .finally(() => setIsLoading(false))
  }, [month])

  useEffect(() => {
    setIsDayLoading(true)

    readMindStatisticsDayDetail(selectedDate)
      .then(setDayDetail)
      .catch((error) => {
        setErrorMessage(error instanceof LoginApiError ? error.message : '선택한 날짜의 마음 기록을 불러오지 못했습니다.')
      })
      .finally(() => setIsDayLoading(false))
  }, [selectedDate])

  const selectedDay = useMemo(() => {
    return calendar?.days.find((day) => day.date === selectedDate) ?? null
  }, [calendar?.days, selectedDate])

  const monthLabel = useMemo(() => {
    const [year, monthValue] = month.split('-')
    return `${year}년 ${Number(monthValue)}월`
  }, [month])

  const calendarCells = useMemo(() => buildCalendarCells(calendar?.days ?? [], month), [calendar?.days, month])

  const handleMoveMonth = (direction: -1 | 1) => {
    const [year, monthValue] = month.split('-').map(Number)
    const nextDate = new Date(year, monthValue - 1 + direction, 1)
    const nextMonth = toYearMonth(nextDate)
    setMonth(nextMonth)
    setSelectedDate(toDateKey(nextDate))
  }

  return (
    <main className="page-shell statistics-page-shell" data-theme-tone={themeTone}>
      <nav className="statistics-top-nav" aria-label="내 마음 통계 메뉴">
        <button className="ghost-button nav-outline-button" type="button" onClick={() => router.push('/')}>
          <ArrowLeft size={17} aria-hidden="true" />
          홈으로
        </button>
        <button className="soft-button" type="button" onClick={() => router.push('/chat')}>
          <MessageCircle size={17} aria-hidden="true" />
          AI 마음대화
        </button>
      </nav>

      <section className="statistics-hero" aria-labelledby="statistics-title">
        <p className="eyebrow">내 마음 통계</p>
        <h1 id="statistics-title">마음 달력과 감정 흐름</h1>
        <p>하루하루 쌓인 AI 마음대화와 리포트를 달력, 분포, 흐름으로 정리합니다.</p>
      </section>

      {errorMessage && (
        <div className="statistics-alert" role="alert">
          <HeartPulse size={18} aria-hidden="true" />
          <span>{errorMessage}</span>
        </div>
      )}

      {isLoading ? (
        <div className="statistics-loading">
          <Loader2 size={22} aria-hidden="true" />
          <span>마음 통계를 불러오고 있습니다.</span>
        </div>
      ) : (
        <div className="statistics-layout">
          <section className="statistics-panel calendar-panel" aria-labelledby="calendar-title">
            <div className="statistics-panel-header">
              <div>
                <p className="eyebrow">마음 달력</p>
                <h2 id="calendar-title">{monthLabel}</h2>
              </div>
              <div className="calendar-control">
                <button type="button" aria-label="이전 달" onClick={() => handleMoveMonth(-1)}>
                  <ChevronLeft size={18} aria-hidden="true" />
                </button>
                <button type="button" aria-label="다음 달" onClick={() => handleMoveMonth(1)}>
                  <ChevronRight size={18} aria-hidden="true" />
                </button>
              </div>
            </div>

            <div className="calendar-summary-row">
              <span>대화한 날 {calendar?.totalConversationDays ?? 0}일</span>
              <span>리포트 {calendar?.totalReportDays ?? 0}개</span>
            </div>

            <div className="mind-calendar-grid" role="grid" aria-label={`${monthLabel} 마음 달력`}>
              {['일', '월', '화', '수', '목', '금', '토'].map((weekday) => (
                <span className="calendar-weekday" key={weekday}>{weekday}</span>
              ))}
              {calendarCells.map((cell, index) => (
                cell ? (
                  <button
                    className={`calendar-day ${cell.date === selectedDate ? 'is-selected' : ''} ${cell.hasConversation ? 'has-conversation' : ''} ${cell.hasReport ? 'has-report' : ''}`}
                    type="button"
                    key={cell.date}
                    aria-label={`${cell.date} 마음 기록 보기`}
                    onClick={() => setSelectedDate(cell.date)}
                  >
                    <strong>{Number(cell.date.slice(-2))}</strong>
                    {cell.primaryEmotion && <span>{cell.primaryEmotion}</span>}
                  </button>
                ) : (
                  <span className="calendar-day is-empty" key={`empty-${index}`} aria-hidden="true" />
                )
              ))}
            </div>
          </section>

          <section className="statistics-panel day-detail-panel" aria-labelledby="day-detail-title">
            <div className="statistics-panel-header">
              <div>
                <p className="eyebrow">날짜 상세</p>
                <h2 id="day-detail-title">{selectedDate}</h2>
              </div>
              <CalendarDays size={24} aria-hidden="true" />
            </div>

            {isDayLoading ? (
              <div className="statistics-empty-state">
                <Loader2 size={20} aria-hidden="true" />
                <span>선택한 날짜를 확인하고 있습니다.</span>
              </div>
            ) : !dayDetail?.hasConversation && !dayDetail?.report ? (
              <div className="statistics-empty-state">
                <Sparkles size={20} aria-hidden="true" />
                <strong>이 날의 마음 기록이 없습니다</strong>
                <span>AI 마음대화를 나누거나 리포트를 만들면 이곳에 정리됩니다.</span>
              </div>
            ) : (
              <div className="day-detail-content">
                {dayDetail.report && (
                  <article className="day-report-card">
                    <span>대표 감정</span>
                    <strong>{dayDetail.report.primaryEmotion}</strong>
                    <p>{dayDetail.report.todaySentence}</p>
                    <small>강도 {dayDetail.report.emotionIntensity ?? '-'} · 원인 {dayDetail.report.mainCause}</small>
                  </article>
                )}
                <div className="day-message-list">
                  {dayDetail.messages.slice(0, 5).map((message) => (
                    <article className={`day-message ${message.senderType === 'USER' ? 'is-user' : 'is-ai'}`} key={message.messageId}>
                      <span>{message.senderType === 'USER' ? '나' : '마음이'}</span>
                      <p>{message.contentPreview}</p>
                    </article>
                  ))}
                </div>
              </div>
            )}
          </section>

          <section className="statistics-panel overview-panel" aria-labelledby="overview-title">
            <div className="statistics-panel-header">
              <div>
                <p className="eyebrow">최근 4주</p>
                <h2 id="overview-title">감정 분포</h2>
              </div>
              <BarChart3 size={24} aria-hidden="true" />
            </div>

            <div className="overview-metric-grid">
              <MetricCard label="대화한 날" value={`${overview?.totalConversationDays ?? 0}일`} />
              <MetricCard label="메시지" value={`${overview?.totalMessages ?? 0}개`} />
              <MetricCard label="리포트" value={`${overview?.totalReports ?? 0}개`} />
            </div>

            <div className="emotion-distribution-list">
              {overview?.emotionDistribution.length ? (
                overview.emotionDistribution.map((emotion) => (
                  <div className="emotion-bar-row" key={emotion.emotion}>
                    <span>{emotion.emotion}</span>
                    <div className="emotion-bar-track" aria-hidden="true">
                      <i style={{ width: `${Math.round(emotion.ratio * 100)}%` }} />
                    </div>
                    <strong>{emotion.count}</strong>
                  </div>
                ))
              ) : (
                <div className="statistics-empty-state">
                  <span>아직 감정 리포트가 없습니다.</span>
                </div>
              )}
            </div>
          </section>

          <section className="statistics-panel trend-panel" aria-labelledby="trend-title">
            <div className="statistics-panel-header">
              <div>
                <p className="eyebrow">감정 흐름</p>
                <h2 id="trend-title">주간 변화</h2>
              </div>
            </div>
            <TrendChart overview={overview} />
          </section>
        </div>
      )}
    </main>
  )
}

function MetricCard({ label, value }: { label: string; value: string }) {
  return (
    <article className="overview-metric-card">
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  )
}

function TrendChart({ overview }: { overview: MindStatisticsOverview | null }) {
  const points = overview?.emotionTrend ?? []
  const polylinePoints = points
    .map((point, index) => {
      const x = points.length <= 1 ? 50 : 12 + (index * 76) / (points.length - 1)
      const intensity = point.averageEmotionIntensity ?? 0
      const y = 86 - Math.max(0, Math.min(10, intensity)) * 7
      return `${x},${y}`
    })
    .join(' ')

  return (
    <div className="trend-chart">
      {points.some((point) => point.averageEmotionIntensity != null) ? (
        <svg viewBox="0 0 100 100" role="img" aria-label="최근 4주 감정 강도 추세">
          <polyline points={polylinePoints} fill="none" stroke="currentColor" strokeWidth="3.5" strokeLinecap="round" strokeLinejoin="round" />
          {points.map((point, index) => {
            const x = points.length <= 1 ? 50 : 12 + (index * 76) / (points.length - 1)
            const y = 86 - Math.max(0, Math.min(10, point.averageEmotionIntensity ?? 0)) * 7
            return <circle key={`${point.weekStartDate}-${point.weekEndDate}`} cx={x} cy={y} r="3.8" />
          })}
        </svg>
      ) : (
        <div className="statistics-empty-state">
          <span>추세를 그릴 리포트가 아직 부족합니다.</span>
        </div>
      )}
      <div className="trend-label-row">
        {points.map((point) => (
          <span key={`${point.weekStartDate}-${point.weekEndDate}`}>{point.weekStartDate.slice(5)}</span>
        ))}
      </div>
    </div>
  )
}

function buildCalendarCells(days: MindStatisticsCalendarDay[], month: string) {
  const [year, monthValue] = month.split('-').map(Number)
  const firstDate = new Date(year, monthValue - 1, 1)
  const emptyCellCount = firstDate.getDay()
  return [...Array.from({ length: emptyCellCount }, () => null), ...days]
}

function toYearMonth(date: Date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
}

function toDateKey(date: Date) {
  return `${toYearMonth(date)}-${String(date.getDate()).padStart(2, '0')}`
}
