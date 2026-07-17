'use client'

import { RefreshCw, Search, ShieldCheck } from 'lucide-react'
import { FormEvent, useEffect, useState } from 'react'
import {
  AdminAuditLog,
  AdminAuditLogPageResponse,
  AdminAuditLogTargetType,
  readAdminAuditLogs,
} from '@/lib/admin-audit-api'
import { LoginApiError } from '@/lib/auth-api'

const targetOptions: Array<{ value: AdminAuditLogTargetType | ''; label: string }> = [
  { value: '', label: '전체' },
  { value: 'ADMIN', label: '관리자' },
  { value: 'MEMBER', label: '회원' },
  { value: 'INQUIRY', label: '문의' },
  { value: 'AI_CHAT_ROOM', label: '대화방' },
  { value: 'AI_CHAT_REPORT', label: '리포트' },
  { value: 'SYSTEM', label: '시스템' },
]

const actionLabels: Record<string, string> = {
  ADMIN_CONSOLE_ACCESS: '관리자 콘솔 접근',
  AUDIT_LOG_VIEW: '감사 로그 조회',
  MEMBER_VIEW: '회원 정보 조회',
  MEMBER_STATUS_CHANGE: '회원 상태 변경',
  INQUIRY_VIEW: '문의 조회',
  INQUIRY_STATUS_CHANGE: '문의 상태 변경',
  AI_CHAT_HISTORY_VIEW: '대화 이력 조회',
  AI_CHAT_REPORT_VIEW: '리포트 조회',
  SYSTEM_LOG_VIEW: '시스템 로그 조회',
}

function formatDateTime(value?: string | null) {
  if (!value) {
    return '-'
  }

  return new Intl.DateTimeFormat('ko-KR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

function renderTarget(log: AdminAuditLog) {
  if (!log.targetType) {
    return '대상 없음'
  }

  return log.targetId ? `${log.targetType} #${log.targetId}` : log.targetType
}

export default function AdminAuditLogPanel() {
  const [logsPage, setLogsPage] = useState<AdminAuditLogPageResponse | null>(null)
  const [targetType, setTargetType] = useState<AdminAuditLogTargetType | ''>('')
  const [targetId, setTargetId] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [message, setMessage] = useState('')

  useEffect(() => {
    loadLogs()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function loadLogs(page = 0) {
    setIsLoading(true)
    setMessage('')

    readAdminAuditLogs({ targetType, targetId: targetId.trim(), page, size: 20 })
      .then((nextPage) => {
        setLogsPage(nextPage)
      })
      .catch((error) => {
        setMessage(error instanceof LoginApiError ? error.message : '감사 로그를 불러오지 못했습니다.')
      })
      .finally(() => {
        setIsLoading(false)
      })
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    loadLogs()
  }

  return (
    <article className="admin-panel admin-audit-panel">
      <div className="admin-panel-heading-row">
        <div>
          <span className="admin-eyebrow">Audit Trail</span>
          <h2>관리자 감사 로그</h2>
        </div>
        <button type="button" className="admin-soft-button" onClick={() => loadLogs(logsPage?.page ?? 0)} disabled={isLoading}>
          <RefreshCw aria-hidden="true" />
          새로고침
        </button>
      </div>

      <form className="admin-filter-bar" onSubmit={handleSubmit}>
        <label>
          대상 유형
          <select value={targetType} onChange={(event) => setTargetType(event.target.value as AdminAuditLogTargetType | '')}>
            {targetOptions.map((option) => (
              <option key={option.value || 'ALL'} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </label>
        <label>
          대상 ID
          <input inputMode="numeric" value={targetId} onChange={(event) => setTargetId(event.target.value)} placeholder="예: 12" />
        </label>
        <button type="submit" className="admin-primary-button">
          <Search aria-hidden="true" />
          조회
        </button>
      </form>

      {message && <p className="admin-state-message">{message}</p>}
      {isLoading && <p className="admin-state-message">감사 로그를 불러오는 중입니다.</p>}

      {!isLoading && !message && logsPage?.logs.length === 0 && (
        <div className="admin-empty-state">
          <ShieldCheck aria-hidden="true" />
          <strong>아직 기록된 감사 로그가 없습니다.</strong>
          <p>관리자 콘솔 접근 또는 후속 관리 액션이 발생하면 이곳에 기록됩니다.</p>
        </div>
      )}

      {!isLoading && !message && Boolean(logsPage?.logs.length) && (
        <div className="admin-audit-list">
          {logsPage?.logs.map((log) => (
            <section className="admin-audit-item" key={log.id}>
              <div>
                <strong>{actionLabels[log.action] ?? log.action}</strong>
                <span>{formatDateTime(log.createdAt)}</span>
              </div>
              <p>{log.reason ?? '사유 기록 없음'}</p>
              <dl>
                <div>
                  <dt>관리자</dt>
                  <dd>{log.adminLoginId}</dd>
                </div>
                <div>
                  <dt>대상</dt>
                  <dd>{renderTarget(log)}</dd>
                </div>
              </dl>
            </section>
          ))}
        </div>
      )}

      {logsPage && logsPage.totalPages > 1 && (
        <div className="admin-pagination">
          <button type="button" onClick={() => loadLogs(Math.max(logsPage.page - 1, 0))} disabled={isLoading || logsPage.page === 0}>
            이전
          </button>
          <span>
            {logsPage.page + 1} / {logsPage.totalPages}
          </span>
          <button
            type="button"
            onClick={() => loadLogs(Math.min(logsPage.page + 1, logsPage.totalPages - 1))}
            disabled={isLoading || logsPage.page >= logsPage.totalPages - 1}
          >
            다음
          </button>
        </div>
      )}
    </article>
  )
}
