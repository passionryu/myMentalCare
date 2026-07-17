'use client'

import { FormEvent, useEffect, useState } from 'react'
import {
  AdminChatMessage,
  AdminReportDetail,
  AdminReportPageResponse,
  readAdminChatMessages,
  readAdminReport,
  readAdminReports,
} from '@/lib/admin-reports-api'
import { LoginApiError } from '@/lib/auth-api'

function formatDate(value?: string | null) {
  if (!value) {
    return '-'
  }

  return new Intl.DateTimeFormat('ko-KR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

export default function AdminReportsPageClient() {
  const [memberId, setMemberId] = useState('')
  const [date, setDate] = useState('')
  const [keyword, setKeyword] = useState('')
  const [reportsPage, setReportsPage] = useState<AdminReportPageResponse | null>(null)
  const [selectedReport, setSelectedReport] = useState<AdminReportDetail | null>(null)
  const [messages, setMessages] = useState<AdminChatMessage[]>([])
  const [reason, setReason] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isDetailLoading, setIsDetailLoading] = useState(false)
  const [isMessagesLoading, setIsMessagesLoading] = useState(false)
  const [message, setMessage] = useState('')

  useEffect(() => {
    loadReports()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function loadReports(page = 0) {
    setIsLoading(true)
    setMessage('')

    readAdminReports({ memberId, date, keyword, page, size: 20 })
      .then((nextPage) => {
        setReportsPage(nextPage)
      })
      .catch((error) => {
        setMessage(error instanceof LoginApiError ? error.message : '리포트 목록을 불러오지 못했습니다.')
      })
      .finally(() => {
        setIsLoading(false)
      })
  }

  function loadReportDetail(reportId: number) {
    setIsDetailLoading(true)
    setMessage('')
    setMessages([])
    setReason('')

    readAdminReport(reportId)
      .then((report) => {
        setSelectedReport(report)
      })
      .catch((error) => {
        setMessage(error instanceof LoginApiError ? error.message : '리포트 상세를 불러오지 못했습니다.')
      })
      .finally(() => {
        setIsDetailLoading(false)
      })
  }

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    loadReports()
  }

  function handleMessagesLoad(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selectedReport) {
      return
    }

    setIsMessagesLoading(true)
    setMessage('')

    readAdminChatMessages(selectedReport.roomId, reason)
      .then((nextMessages) => {
        setMessages(nextMessages)
      })
      .catch((error) => {
        setMessage(error instanceof LoginApiError ? error.message : '원문 대화를 불러오지 못했습니다.')
      })
      .finally(() => {
        setIsMessagesLoading(false)
      })
  }

  return (
    <section className="admin-page-grid">
      <article className="admin-panel">
        <div>
          <span className="admin-eyebrow">Reports</span>
          <h2>대화/리포트 조회</h2>
        </div>

        <form className="admin-filter-bar" onSubmit={handleSearch}>
          <label>
            회원 ID
            <input inputMode="numeric" value={memberId} onChange={(event) => setMemberId(event.target.value)} placeholder="예: 12" />
          </label>
          <label>
            날짜
            <input type="date" value={date} onChange={(event) => setDate(event.target.value)} />
          </label>
          <label>
            키워드
            <input value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="감정, 원인, 문장" />
          </label>
          <button type="submit" className="admin-primary-button">
            조회
          </button>
        </form>

        {message && <p className="admin-state-message">{message}</p>}
        {isLoading && <p className="admin-state-message">리포트 목록을 불러오는 중입니다.</p>}
        {!isLoading && !message && reportsPage?.reports.length === 0 && <p className="admin-state-message">조건에 맞는 리포트가 없습니다.</p>}

        {!isLoading && Boolean(reportsPage?.reports.length) && (
          <div className="admin-table-scroll">
            <table className="admin-data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>회원 ID</th>
                  <th>날짜</th>
                  <th>주요 감정</th>
                  <th>감정 점수</th>
                  <th>주요 원인</th>
                </tr>
              </thead>
              <tbody>
                {reportsPage?.reports.map((report) => (
                  <tr key={report.id} onClick={() => loadReportDetail(report.id)}>
                    <td>{report.id}</td>
                    <td>{report.memberId}</td>
                    <td>{report.conversationDate}</td>
                    <td>{report.primaryEmotion}</td>
                    <td>{report.emotionScore ?? '-'}</td>
                    <td>{report.mainCause}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {reportsPage && reportsPage.totalPages > 1 && (
          <div className="admin-pagination">
            <button type="button" onClick={() => loadReports(Math.max(reportsPage.page - 1, 0))} disabled={reportsPage.page === 0}>
              이전
            </button>
            <span>
              {reportsPage.page + 1} / {reportsPage.totalPages}
            </span>
            <button
              type="button"
              onClick={() => loadReports(Math.min(reportsPage.page + 1, reportsPage.totalPages - 1))}
              disabled={reportsPage.page >= reportsPage.totalPages - 1}
            >
              다음
            </button>
          </div>
        )}
      </article>

      <aside className="admin-panel admin-detail-panel">
        <span className="admin-eyebrow">Sensitive</span>
        <h2>리포트 상세</h2>
        {isDetailLoading && <p className="admin-state-message">리포트 상세를 불러오는 중입니다.</p>}
        {!selectedReport && !isDetailLoading && <p className="admin-state-message">목록에서 리포트를 선택하세요.</p>}

        {selectedReport && (
          <>
            <dl className="admin-detail-list">
              <div>
                <dt>리포트 ID</dt>
                <dd>{selectedReport.id}</dd>
              </div>
              <div>
                <dt>회원 ID</dt>
                <dd>{selectedReport.memberId}</dd>
              </div>
              <div>
                <dt>대화방 ID</dt>
                <dd>{selectedReport.roomId}</dd>
              </div>
              <div>
                <dt>생성일</dt>
                <dd>{formatDate(selectedReport.createdAt)}</dd>
              </div>
              <div>
                <dt>주요 감정</dt>
                <dd>{selectedReport.primaryEmotion}</dd>
              </div>
              <div>
                <dt>감정 점수</dt>
                <dd>{selectedReport.emotionScore ?? '-'}</dd>
              </div>
            </dl>

            <section className="admin-inquiry-content">
              <strong>오늘 마음 요약</strong>
              <p>{selectedReport.summary}</p>
            </section>
            <section className="admin-inquiry-content">
              <strong>마음 흐름</strong>
              <p>{selectedReport.emotionalFlow}</p>
            </section>
            <section className="admin-inquiry-content">
              <strong>추천곡</strong>
              <p>{selectedReport.songs.map((song) => `${song.title} - ${song.artist}`).join('\n') || '추천곡 없음'}</p>
            </section>

            <form className="admin-status-form" onSubmit={handleMessagesLoad}>
              <label>
                원문 대화 조회 사유
                <textarea value={reason} onChange={(event) => setReason(event.target.value)} placeholder="원문 대화 조회 사유를 구체적으로 입력하세요." />
              </label>
              <button type="submit" className="admin-primary-button" disabled={isMessagesLoading}>
                {isMessagesLoading ? '불러오는 중' : '원문 대화 조회'}
              </button>
            </form>

            {messages.length > 0 && (
              <div className="admin-message-list">
                {messages.map((chatMessage) => (
                  <section className={`admin-message-item sender-${chatMessage.senderType.toLowerCase()}`} key={chatMessage.id}>
                    <strong>{chatMessage.senderType}</strong>
                    <p>{chatMessage.content}</p>
                  </section>
                ))}
              </div>
            )}
          </>
        )}
      </aside>
    </section>
  )
}
