'use client'

import { FormEvent, useEffect, useState } from 'react'
import AdminDetailModal from '../AdminDetailModal'
import {
  AdminInquiryDetail,
  AdminInquiryPageResponse,
  AdminInquiryStatus,
  changeAdminInquiryStatus,
  readAdminInquiries,
  readAdminInquiry,
  updateAdminInquiryMemo,
} from '@/lib/admin-inquiries-api'
import { LoginApiError } from '@/lib/auth-api'

const statusOptions: Array<{ value: AdminInquiryStatus | ''; label: string }> = [
  { value: '', label: '전체' },
  { value: 'RECEIVED', label: '접수' },
  { value: 'IN_PROGRESS', label: '처리 중' },
  { value: 'DONE', label: '완료' },
]

const statusLabels: Record<AdminInquiryStatus, string> = {
  RECEIVED: '접수',
  IN_PROGRESS: '처리 중',
  DONE: '완료',
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('ko-KR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

export default function AdminInquiriesPageClient() {
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState<AdminInquiryStatus | ''>('RECEIVED')
  const [inquiriesPage, setInquiriesPage] = useState<AdminInquiryPageResponse | null>(null)
  const [selectedInquiry, setSelectedInquiry] = useState<AdminInquiryDetail | null>(null)
  const [nextStatus, setNextStatus] = useState<AdminInquiryStatus>('IN_PROGRESS')
  const [adminMemo, setAdminMemo] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isDetailLoading, setIsDetailLoading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [message, setMessage] = useState('')
  const [toast, setToast] = useState('')

  useEffect(() => {
    loadInquiries()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function loadInquiries(page = 0) {
    setIsLoading(true)
    setMessage('')

    readAdminInquiries({ keyword, status, page, size: 20 })
      .then((nextPage) => {
        setInquiriesPage(nextPage)
      })
      .catch((error) => {
        setMessage(error instanceof LoginApiError ? error.message : '문의 목록을 불러오지 못했습니다.')
      })
      .finally(() => {
        setIsLoading(false)
      })
  }

  function loadInquiryDetail(inquiryId: number) {
    setIsDetailLoading(true)
    setMessage('')
    setSelectedInquiry(null)

    readAdminInquiry(inquiryId)
      .then((inquiry) => {
        setSelectedInquiry(inquiry)
        setNextStatus(inquiry.status === 'RECEIVED' ? 'IN_PROGRESS' : 'DONE')
        setAdminMemo(inquiry.adminMemo ?? '')
      })
      .catch((error) => {
        setMessage(error instanceof LoginApiError ? error.message : '문의 상세를 불러오지 못했습니다.')
      })
      .finally(() => {
        setIsDetailLoading(false)
      })
  }

  function closeInquiryModal() {
    setSelectedInquiry(null)
    setIsDetailLoading(false)
    setAdminMemo('')
  }

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    loadInquiries()
  }

  function handleStatusChange(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selectedInquiry) {
      return
    }

    setIsSaving(true)
    setMessage('')
    setToast('')

    changeAdminInquiryStatus(selectedInquiry.id, nextStatus, adminMemo)
      .then((inquiry) => {
        setSelectedInquiry(inquiry)
        setAdminMemo(inquiry.adminMemo ?? '')
        setToast('문의 처리 상태를 저장했습니다.')
        loadInquiries(inquiriesPage?.page ?? 0)
      })
      .catch((error) => {
        setMessage(error instanceof LoginApiError ? error.message : '문의 처리 상태를 저장하지 못했습니다.')
      })
      .finally(() => {
        setIsSaving(false)
      })
  }

  function handleMemoSave() {
    if (!selectedInquiry) {
      return
    }

    setIsSaving(true)
    setMessage('')
    setToast('')

    updateAdminInquiryMemo(selectedInquiry.id, adminMemo)
      .then((inquiry) => {
        setSelectedInquiry(inquiry)
        setToast('운영 메모를 저장했습니다.')
      })
      .catch((error) => {
        setMessage(error instanceof LoginApiError ? error.message : '운영 메모를 저장하지 못했습니다.')
      })
      .finally(() => {
        setIsSaving(false)
      })
  }

  return (
    <section className="admin-page-grid">
      <article className="admin-panel">
        <div className="admin-panel-heading-row">
          <div>
            <span className="admin-eyebrow">Inquiries</span>
            <h2>문의 목록</h2>
          </div>
          {toast && <span className="admin-success-chip">{toast}</span>}
        </div>

        <form className="admin-filter-bar" onSubmit={handleSearch}>
          <label>
            검색어
            <input value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="문의 ID, 회원 ID, 내용" />
          </label>
          <label>
            상태
            <select value={status} onChange={(event) => setStatus(event.target.value as AdminInquiryStatus | '')}>
              {statusOptions.map((option) => (
                <option key={option.value || 'ALL'} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>
          <button type="submit" className="admin-primary-button">
            조회
          </button>
        </form>

        {message && <p className="admin-state-message">{message}</p>}
        {isLoading && <p className="admin-state-message">문의 목록을 불러오는 중입니다.</p>}
        {!isLoading && !message && inquiriesPage?.inquiries.length === 0 && (
          <p className="admin-state-message">조건에 맞는 문의가 없습니다.</p>
        )}

        {!isLoading && Boolean(inquiriesPage?.inquiries.length) && (
          <div className="admin-table-scroll">
            <table className="admin-data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>회원 ID</th>
                  <th>분류</th>
                  <th>상태</th>
                  <th>접수일</th>
                </tr>
              </thead>
              <tbody>
                {inquiriesPage?.inquiries.map((inquiry) => (
                  <tr
                    key={inquiry.id}
                    tabIndex={0}
                    onClick={() => loadInquiryDetail(inquiry.id)}
                    onKeyDown={(event) => {
                      if (event.key === 'Enter' || event.key === ' ') {
                        event.preventDefault()
                        loadInquiryDetail(inquiry.id)
                      }
                    }}
                  >
                    <td>{inquiry.id}</td>
                    <td>{inquiry.memberId}</td>
                    <td>{inquiry.category}</td>
                    <td>
                      <span className={`admin-status-badge status-${inquiry.status.toLowerCase().replace('_', '-')}`}>
                        {statusLabels[inquiry.status]}
                      </span>
                    </td>
                    <td>{formatDate(inquiry.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {inquiriesPage && inquiriesPage.totalPages > 1 && (
          <div className="admin-pagination">
            <button type="button" onClick={() => loadInquiries(Math.max(inquiriesPage.page - 1, 0))} disabled={inquiriesPage.page === 0}>
              이전
            </button>
            <span>
              {inquiriesPage.page + 1} / {inquiriesPage.totalPages}
            </span>
            <button
              type="button"
              onClick={() => loadInquiries(Math.min(inquiriesPage.page + 1, inquiriesPage.totalPages - 1))}
              disabled={inquiriesPage.page >= inquiriesPage.totalPages - 1}
            >
              다음
            </button>
          </div>
        )}
      </article>

      {(isDetailLoading || selectedInquiry) && (
        <AdminDetailModal eyebrow="Detail" title="문의 상세" onClose={closeInquiryModal}>
          {isDetailLoading && <p className="admin-state-message">문의 상세를 불러오는 중입니다.</p>}
          {selectedInquiry && (
            <>
              <dl className="admin-detail-list">
                <div>
                  <dt>문의 ID</dt>
                  <dd>{selectedInquiry.id}</dd>
                </div>
                <div>
                  <dt>회원 ID</dt>
                  <dd>{selectedInquiry.memberId}</dd>
                </div>
                <div>
                  <dt>분류</dt>
                  <dd>{selectedInquiry.category}</dd>
                </div>
                <div>
                  <dt>상태</dt>
                  <dd>
                    <span className={`admin-status-badge status-${selectedInquiry.status.toLowerCase().replace('_', '-')}`}>
                      {statusLabels[selectedInquiry.status]}
                    </span>
                  </dd>
                </div>
              </dl>

              <section className="admin-inquiry-content">
                <strong>문의 내용</strong>
                <p>{selectedInquiry.content}</p>
              </section>

              <form className="admin-status-form" onSubmit={handleStatusChange}>
                <label>
                  처리 상태
                  <select value={nextStatus} onChange={(event) => setNextStatus(event.target.value as AdminInquiryStatus)}>
                    <option value="RECEIVED">접수</option>
                    <option value="IN_PROGRESS">처리 중</option>
                    <option value="DONE">완료</option>
                  </select>
                </label>
                <label>
                  운영 메모
                  <textarea value={adminMemo} onChange={(event) => setAdminMemo(event.target.value)} placeholder="처리 내용과 판단 근거를 기록하세요." />
                </label>
                <div className="admin-inline-actions">
                  <button type="submit" className="admin-primary-button" disabled={isSaving}>
                    {isSaving ? '저장 중' : '상태 저장'}
                  </button>
                  <button type="button" className="admin-soft-button" onClick={handleMemoSave} disabled={isSaving}>
                    메모만 저장
                  </button>
                </div>
              </form>
            </>
          )}
        </AdminDetailModal>
      )}
    </section>
  )
}
