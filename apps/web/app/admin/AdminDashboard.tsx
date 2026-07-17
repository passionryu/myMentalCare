'use client'

import { Activity, AlertCircle, FileText, MessageSquareText, RefreshCw, UserPlus, UsersRound } from 'lucide-react'
import Link from 'next/link'
import { useEffect, useMemo, useState } from 'react'
import {
  AdminDashboardAlert,
  AdminDashboardDailyMetric,
  AdminDashboardSummary,
  readAdminDashboardAlerts,
  readAdminDashboardDailyMetrics,
  readAdminDashboardSummary,
} from '@/lib/admin-dashboard-api'
import { LoginApiError } from '@/lib/auth-api'

const summaryCardMeta = [
  { key: 'totalMembers', label: '전체 회원 수', description: '활성 회원 기준', icon: UsersRound },
  { key: 'todaySignups', label: '오늘 가입자 수', description: 'Asia/Seoul 기준', icon: UserPlus },
  { key: 'todayConversations', label: '오늘 대화 수', description: '오늘 생성된 대화방', icon: MessageSquareText },
  { key: 'todayReports', label: '오늘 리포트 생성 수', description: '오늘 저장된 마음 리포트', icon: FileText },
] as const

function formatDate(value: string) {
  return new Intl.DateTimeFormat('ko-KR', { month: 'numeric', day: 'numeric' }).format(new Date(value))
}

function formatMetricValue(value: number) {
  return new Intl.NumberFormat('ko-KR').format(value)
}

function severityClass(alert: AdminDashboardAlert) {
  return `admin-alert-item severity-${alert.severity.toLowerCase()}`
}

export default function AdminDashboard() {
  const [summary, setSummary] = useState<AdminDashboardSummary | null>(null)
  const [dailyMetrics, setDailyMetrics] = useState<AdminDashboardDailyMetric[]>([])
  const [alerts, setAlerts] = useState<AdminDashboardAlert[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [message, setMessage] = useState('')

  const maxMetricValue = useMemo(() => {
    return Math.max(
      1,
      ...dailyMetrics.flatMap((metric) => [metric.signups, metric.conversations, metric.reports]),
    )
  }, [dailyMetrics])

  useEffect(() => {
    loadDashboard()
  }, [])

  function loadDashboard() {
    setIsLoading(true)
    setMessage('')

    Promise.all([
      readAdminDashboardSummary(),
      readAdminDashboardDailyMetrics(7),
      readAdminDashboardAlerts(),
    ])
      .then(([nextSummary, nextDailyMetrics, nextAlerts]) => {
        setSummary(nextSummary)
        setDailyMetrics(nextDailyMetrics)
        setAlerts(nextAlerts)
      })
      .catch((error) => {
        setMessage(error instanceof LoginApiError ? error.message : '관리자 대시보드를 불러오지 못했습니다.')
      })
      .finally(() => {
        setIsLoading(false)
      })
  }

  if (isLoading) {
    return (
      <section className="admin-page-grid">
        <p className="admin-state-message">운영 지표를 불러오는 중입니다.</p>
      </section>
    )
  }

  if (message) {
    return (
      <section className="admin-page-grid">
        <article className="admin-panel">
          <AlertCircle aria-hidden="true" />
          <h2>대시보드를 불러오지 못했습니다</h2>
          <p>{message}</p>
          <button type="button" className="admin-primary-button" onClick={loadDashboard}>
            <RefreshCw aria-hidden="true" />
            다시 시도
          </button>
        </article>
      </section>
    )
  }

  return (
    <section className="admin-page-grid">
      <div className="admin-dashboard-grid">
        {summaryCardMeta.map((card) => {
          const Icon = card.icon
          const value = summary?.[card.key] ?? 0

          return (
            <article className="admin-stat-card" key={card.key}>
              <Icon aria-hidden="true" />
              <span>{card.label}</span>
              <strong>{formatMetricValue(value)}</strong>
              <p>{card.description}</p>
            </article>
          )
        })}
      </div>

      <div className="admin-dashboard-content-grid">
        <article className="admin-panel admin-chart-panel">
          <div className="admin-panel-heading-row">
            <div>
              <span className="admin-eyebrow">최근 7일</span>
              <h2>핵심 지표 흐름</h2>
            </div>
            <span className="admin-base-date">기준일 {summary?.baseDate}</span>
          </div>

          <div className="admin-chart-legend">
            <span className="metric-signups">가입</span>
            <span className="metric-conversations">대화</span>
            <span className="metric-reports">리포트</span>
          </div>

          <div className="admin-weekly-chart" aria-label="최근 7일 관리자 핵심 지표 그래프">
            {dailyMetrics.map((metric) => (
              <div className="admin-chart-day" key={metric.date}>
                <div className="admin-chart-bars">
                  <span className="metric-signups" style={{ height: `${Math.max(8, (metric.signups / maxMetricValue) * 100)}%` }} />
                  <span
                    className="metric-conversations"
                    style={{ height: `${Math.max(8, (metric.conversations / maxMetricValue) * 100)}%` }}
                  />
                  <span className="metric-reports" style={{ height: `${Math.max(8, (metric.reports / maxMetricValue) * 100)}%` }} />
                </div>
                <strong>{formatDate(metric.date)}</strong>
              </div>
            ))}
          </div>
        </article>

        <article className="admin-panel admin-alert-panel">
          <div>
            <span className="admin-eyebrow">운영 알림</span>
            <h2>먼저 볼 항목</h2>
          </div>

          <div className="admin-alert-list">
            {alerts.map((alert) => (
              <section className={severityClass(alert)} key={`${alert.type}-${alert.title}`}>
                <Activity aria-hidden="true" />
                <div>
                  <strong>{alert.title}</strong>
                  <p>{alert.description}</p>
                  {alert.targetPath && <Link href={alert.targetPath}>관련 화면 보기</Link>}
                </div>
              </section>
            ))}
          </div>
        </article>
      </div>
    </section>
  )
}
