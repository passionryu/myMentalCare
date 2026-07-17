'use client'

import { FormEvent, useEffect, useState } from 'react'
import {
  AdminMemberDetail,
  AdminMemberPageResponse,
  AdminMemberStatus,
  changeAdminMemberStatus,
  readAdminMember,
  readAdminMembers,
} from '@/lib/admin-users-api'
import { LoginApiError } from '@/lib/auth-api'

const statusOptions: Array<{ value: AdminMemberStatus | ''; label: string }> = [
  { value: '', label: '전체' },
  { value: 'ACTIVE', label: '활성' },
  { value: 'SUSPENDED', label: '정지' },
  { value: 'WITHDRAWN', label: '탈퇴' },
]

const statusLabels: Record<AdminMemberStatus, string> = {
  ACTIVE: '활성',
  SUSPENDED: '정지',
  WITHDRAWN: '탈퇴',
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('ko-KR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

export default function AdminUsersPageClient() {
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState<AdminMemberStatus | ''>('')
  const [membersPage, setMembersPage] = useState<AdminMemberPageResponse | null>(null)
  const [selectedMember, setSelectedMember] = useState<AdminMemberDetail | null>(null)
  const [nextStatus, setNextStatus] = useState<AdminMemberStatus>('SUSPENDED')
  const [reason, setReason] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isDetailLoading, setIsDetailLoading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [message, setMessage] = useState('')
  const [toast, setToast] = useState('')

  useEffect(() => {
    loadMembers()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function loadMembers(page = 0) {
    setIsLoading(true)
    setMessage('')

    readAdminMembers({ keyword, status, page, size: 20 })
      .then((nextPage) => {
        setMembersPage(nextPage)
      })
      .catch((error) => {
        setMessage(error instanceof LoginApiError ? error.message : '회원 목록을 불러오지 못했습니다.')
      })
      .finally(() => {
        setIsLoading(false)
      })
  }

  function loadMemberDetail(memberId: number) {
    setIsDetailLoading(true)
    setMessage('')

    readAdminMember(memberId)
      .then((member) => {
        setSelectedMember(member)
        setNextStatus(member.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE')
        setReason('')
      })
      .catch((error) => {
        setMessage(error instanceof LoginApiError ? error.message : '회원 상세 정보를 불러오지 못했습니다.')
      })
      .finally(() => {
        setIsDetailLoading(false)
      })
  }

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    loadMembers()
  }

  function handleStatusChange(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selectedMember) {
      return
    }

    setIsSaving(true)
    setMessage('')
    setToast('')

    changeAdminMemberStatus(selectedMember.id, { status: nextStatus, reason })
      .then((member) => {
        setSelectedMember(member)
        setNextStatus(member.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE')
        setReason('')
        setToast('회원 상태를 변경했습니다.')
        loadMembers(membersPage?.page ?? 0)
      })
      .catch((error) => {
        setMessage(error instanceof LoginApiError ? error.message : '회원 상태를 변경하지 못했습니다.')
      })
      .finally(() => {
        setIsSaving(false)
      })
  }

  const canChangeStatus = selectedMember?.role !== 'ADMIN' && selectedMember?.status !== 'WITHDRAWN'

  return (
    <section className="admin-page-grid">
      <article className="admin-panel">
        <div className="admin-panel-heading-row">
          <div>
            <span className="admin-eyebrow">Members</span>
            <h2>회원 목록</h2>
          </div>
          {toast && <span className="admin-success-chip">{toast}</span>}
        </div>

        <form className="admin-filter-bar" onSubmit={handleSearch}>
          <label>
            검색어
            <input value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="ID, 이름, 이메일" />
          </label>
          <label>
            상태
            <select value={status} onChange={(event) => setStatus(event.target.value as AdminMemberStatus | '')}>
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
        {isLoading && <p className="admin-state-message">회원 목록을 불러오는 중입니다.</p>}

        {!isLoading && !message && membersPage?.members.length === 0 && (
          <p className="admin-state-message">조건에 맞는 회원이 없습니다.</p>
        )}

        {!isLoading && Boolean(membersPage?.members.length) && (
          <div className="admin-table-scroll">
            <table className="admin-data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>이름</th>
                  <th>로그인 ID</th>
                  <th>이메일</th>
                  <th>상태</th>
                  <th>가입일</th>
                </tr>
              </thead>
              <tbody>
                {membersPage?.members.map((member) => (
                  <tr key={member.id} onClick={() => loadMemberDetail(member.id)}>
                    <td>{member.id}</td>
                    <td>{member.name}</td>
                    <td>{member.loginId}</td>
                    <td>{member.email ?? '-'}</td>
                    <td>
                      <span className={`admin-status-badge status-${member.status.toLowerCase()}`}>{statusLabels[member.status]}</span>
                    </td>
                    <td>{formatDate(member.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {membersPage && membersPage.totalPages > 1 && (
          <div className="admin-pagination">
            <button type="button" onClick={() => loadMembers(Math.max(membersPage.page - 1, 0))} disabled={membersPage.page === 0}>
              이전
            </button>
            <span>
              {membersPage.page + 1} / {membersPage.totalPages}
            </span>
            <button
              type="button"
              onClick={() => loadMembers(Math.min(membersPage.page + 1, membersPage.totalPages - 1))}
              disabled={membersPage.page >= membersPage.totalPages - 1}
            >
              다음
            </button>
          </div>
        )}
      </article>

      <aside className="admin-panel admin-detail-panel">
        <span className="admin-eyebrow">Detail</span>
        <h2>회원 상세</h2>
        {isDetailLoading && <p className="admin-state-message">회원 상세를 불러오는 중입니다.</p>}
        {!selectedMember && !isDetailLoading && <p className="admin-state-message">목록에서 회원을 선택하세요.</p>}
        {selectedMember && (
          <>
            <dl className="admin-detail-list">
              <div>
                <dt>회원 ID</dt>
                <dd>{selectedMember.id}</dd>
              </div>
              <div>
                <dt>이름</dt>
                <dd>{selectedMember.name}</dd>
              </div>
              <div>
                <dt>로그인 ID</dt>
                <dd>{selectedMember.loginId}</dd>
              </div>
              <div>
                <dt>이메일</dt>
                <dd>{selectedMember.email ?? '-'}</dd>
              </div>
              <div>
                <dt>전화번호</dt>
                <dd>{selectedMember.phone ?? '-'}</dd>
              </div>
              <div>
                <dt>권한</dt>
                <dd>{selectedMember.role}</dd>
              </div>
              <div>
                <dt>상태</dt>
                <dd>
                  <span className={`admin-status-badge status-${selectedMember.status.toLowerCase()}`}>
                    {statusLabels[selectedMember.status]}
                  </span>
                </dd>
              </div>
              <div>
                <dt>수정일</dt>
                <dd>{formatDate(selectedMember.updatedAt)}</dd>
              </div>
            </dl>

            <form className="admin-status-form" onSubmit={handleStatusChange}>
              <label>
                변경 상태
                <select value={nextStatus} onChange={(event) => setNextStatus(event.target.value as AdminMemberStatus)} disabled={!canChangeStatus}>
                  <option value="ACTIVE">활성</option>
                  <option value="SUSPENDED">정지</option>
                  <option value="WITHDRAWN">탈퇴</option>
                </select>
              </label>
              <label>
                변경 사유
                <textarea value={reason} onChange={(event) => setReason(event.target.value)} placeholder="운영 기록에 남길 사유를 입력하세요." />
              </label>
              <button type="submit" className="admin-primary-button" disabled={!canChangeStatus || isSaving}>
                {isSaving ? '변경 중' : '상태 변경'}
              </button>
              {!canChangeStatus && <p>관리자 계정 또는 탈퇴 회원은 상태를 변경할 수 없습니다.</p>}
            </form>
          </>
        )}
      </aside>
    </section>
  )
}
