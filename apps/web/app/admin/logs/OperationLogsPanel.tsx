'use client'

import { FormEvent, useEffect, useState } from 'react'
import {
  AdminIncidentImpact,
  AdminOperationLogPageResponse,
  AdminOperationLogSummary,
  createAdminIncident,
  readAdminOperationLogSummary,
  readAdminOperationLogs,
} from '@/lib/admin-operation-logs-api'
import { LoginApiError } from '@/lib/auth-api'

const impactOptions: Array<{ value: AdminIncidentImpact | ''; label: string }> = [
  { value: '', label: '전체' },
  { value: 'LOW', label: '낮음' },
  { value: 'MEDIUM', label: '보통' },
  { value: 'HIGH', label: '높음' },
  { value: 'CRITICAL', label: '심각' },
]

function formatDate(value?: string | null) {
  if (!value) {
    return '-'
  }

  return new Intl.DateTimeFormat('ko-KR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

export default function OperationLogsPanel() {
  const [summary, setSummary] = useState<AdminOperationLogSummary | null>(null)
  const [logsPage, setLogsPage] = useState<AdminOperationLogPageResponse | null>(null)
  const [level, setLevel] = useState<AdminIncidentImpact | ''>('')
  const [keyword, setKeyword] = useState('')
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [impact, setImpact] = useState<AdminIncidentImpact>('MEDIUM')
  const [action, setAction] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [message, setMessage] = useState('')
  const [toast, setToast] = useState('')

  useEffect(() => {
    loadLogs()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function loadLogs(page = 0) {
    setIsLoading(true)
    setMessage('')

    Promise.all([
      readAdminOperationLogSummary(),
      readAdminOperationLogs({ level, keyword, page, size: 20 }),
    ])
      .then(([nextSummary, nextLogsPage]) => {
        setSummary(nextSummary)
        setLogsPage(nextLogsPage)
      })
      .catch((error) => {
        setMessage(error instanceof LoginApiError ? error.message : '운영 로그를 불러오지 못했습니다.')
      })
      .finally(() => {
        setIsLoading(false)
      })
  }

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    loadLogs()
  }

  function handleCreateIncident(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSaving(true)
    setMessage('')
    setToast('')

    createAdminIncident({ title, description, impact, action })
      .then(() => {
        setTitle('')
        setDescription('')
        setAction('')
        setImpact('MEDIUM')
        setToast('장애 기록을 생성했습니다.')
        loadLogs()
      })
      .catch((error) => {
        setMessage(error instanceof LoginApiError ? error.message : '장애 기록을 생성하지 못했습니다.')
      })
      .finally(() => {
        setIsSaving(false)
      })
  }

  return (
    <article className="admin-panel admin-operation-panel">
      <div className="admin-panel-heading-row">
        <div>
          <span className="admin-eyebrow">Operation</span>
          <h2>장애/에러 요약</h2>
        </div>
        {toast && <span className="admin-success-chip">{toast}</span>}
      </div>

      {summary && (
        <div className="admin-dashboard-grid">
          <article className="admin-stat-card">
            <span>최근 장애 기록</span>
            <strong>{summary.totalIncidents}</strong>
            <p>{summary.from} ~ {summary.to}</p>
          </article>
          <article className="admin-stat-card">
            <span>조치 필요</span>
            <strong>{summary.actionRequiredIncidents}</strong>
            <p>HIGH/CRITICAL 기준</p>
          </article>
          <article className="admin-stat-card">
            <span>심각</span>
            <strong>{summary.criticalIncidents}</strong>
            <p>즉시 확인 필요</p>
          </article>
          <article className="admin-stat-card">
            <span>최근 기록</span>
            <strong className="admin-small-stat">{formatDate(summary.latestIncidentAt)}</strong>
            <p>마지막 장애 기록 시각</p>
          </article>
        </div>
      )}

      <form className="admin-filter-bar" onSubmit={handleSearch}>
        <label>
          영향도
          <select value={level} onChange={(event) => setLevel(event.target.value as AdminIncidentImpact | '')}>
            {impactOptions.map((option) => (
              <option key={option.value || 'ALL'} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </label>
        <label>
          검색어
          <input value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="ID, 제목, 설명, 조치" />
        </label>
        <button type="submit" className="admin-primary-button">
          조회
        </button>
      </form>

      {message && <p className="admin-state-message">{message}</p>}
      {isLoading && <p className="admin-state-message">운영 로그를 불러오는 중입니다.</p>}
      {!isLoading && !message && logsPage?.logs.length === 0 && <p className="admin-state-message">기록된 장애/에러가 없습니다.</p>}

      {!isLoading && Boolean(logsPage?.logs.length) && (
        <div className="admin-audit-list">
          {logsPage?.logs.map((log) => (
            <section className={`admin-audit-item severity-${log.level.toLowerCase()}`} key={log.id}>
              <div>
                <strong>{log.title}</strong>
                <span>{formatDate(log.createdAt)}</span>
              </div>
              <p>{log.description ?? '상세 설명 없음'}</p>
              <dl>
                <div>
                  <dt>영향도</dt>
                  <dd>{log.level}</dd>
                </div>
                <div>
                  <dt>권장 조치</dt>
                  <dd>{log.action ?? '-'}</dd>
                </div>
              </dl>
            </section>
          ))}
        </div>
      )}

      <form className="admin-status-form" onSubmit={handleCreateIncident}>
        <h3>장애 기록 추가</h3>
        <label>
          제목
          <input value={title} onChange={(event) => setTitle(event.target.value)} placeholder="예: AI 응답 지연 증가" />
        </label>
        <label>
          영향도
          <select value={impact} onChange={(event) => setImpact(event.target.value as AdminIncidentImpact)}>
            <option value="LOW">낮음</option>
            <option value="MEDIUM">보통</option>
            <option value="HIGH">높음</option>
            <option value="CRITICAL">심각</option>
          </select>
        </label>
        <label>
          설명
          <textarea value={description} onChange={(event) => setDescription(event.target.value)} placeholder="영향 범위와 확인한 현상을 적어주세요." />
        </label>
        <label>
          권장 조치
          <textarea value={action} onChange={(event) => setAction(event.target.value)} placeholder="다음 담당자가 바로 볼 조치 내용을 적어주세요." />
        </label>
        <button type="submit" className="admin-primary-button" disabled={isSaving}>
          {isSaving ? '저장 중' : '장애 기록 생성'}
        </button>
      </form>
    </article>
  )
}
