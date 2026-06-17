'use client'

import {
  AlertTriangle,
  ArrowLeft,
  Bell,
  BookOpen,
  CheckCircle2,
  ChevronRight,
  ClipboardList,
  FileText,
  HeartHandshake,
  Home,
  KeyRound,
  Link2,
  LogOut,
  Mail,
  MessageCircle,
  Palette,
  ShieldCheck,
  Trash2,
  UserRound,
  X,
} from 'lucide-react'
import { useRouter } from 'next/navigation'
import { FormEvent, useEffect, useMemo, useRef, useState } from 'react'
import {
  AiChatHistoryRoom,
  AiChatHistoryRoomDetail,
  AiChatReport,
  AiChatCheckInHistory,
  DeleteAiChatHistoryTargetType,
  deleteAiChatHistory,
  readAiChatHistoryRoom,
  readAiChatHistoryRooms,
  readAiChatCheckIns,
  readAiChatReport,
  readAiChatReports,
} from '@/lib/ai-chat-api'
import {
  LoginApiError,
  LoginMethodsResponse,
  MyProfileResponse,
  UpdateMyProfileRequest,
  WithdrawMemberRequest,
  changeMyPassword,
  readLoginMethods,
  readMyProfile,
  updateMyProfile,
  withdrawMyAccount,
} from '@/lib/auth-api'
import { CreateInquiryResponse, createInquiry } from '@/lib/inquiry-api'
import { MyPageSummaryResponse, readMyPageSummary } from '@/lib/mypage-api'
import {
  NotificationWeekday,
  readNotificationSetting,
  updateNotificationSetting,
} from '@/lib/notification-settings-api'

type ThemeTone = 'sunset' | 'cream' | 'wood'
type MyPageSection = 'overview' | 'profile' | 'history' | 'settings' | 'support' | 'security'
type DialogType = 'editProfile' | 'deleteHistory' | 'withdraw' | null

const THEME_TONE_STORAGE_KEY = 'myMentalCare.themeTone'
const LOGOUT_NOTICE_REQUEST_KEY = 'myMentalCare.logoutNotice'
const WITHDRAWAL_NOTICE_REQUEST_KEY = 'myMentalCare.withdrawalNotice'
const defaultNotificationWeekdays: NotificationWeekday[] = ['MON', 'TUE', 'WED', 'THU', 'FRI']

const sections: Array<{ id: MyPageSection; label: string; icon: typeof Home }> = [
  { id: 'overview', label: '요약', icon: Home },
  { id: 'profile', label: '프로필', icon: UserRound },
  { id: 'history', label: '내 이력', icon: ClipboardList },
  { id: 'settings', label: '설정', icon: Palette },
  { id: 'support', label: '문의하기', icon: Mail },
  { id: 'security', label: '계정 관리', icon: ShieldCheck },
]

const themes: Array<{ value: ThemeTone; label: string; description: string }> = [
  { value: 'sunset', label: '노을빛', description: '차분한 살구색과 세이지 톤' },
  { value: 'cream', label: '크림빛', description: '밝고 편안한 아이보리 톤' },
  { value: 'wood', label: '우드빛', description: '내추럴 우드와 아이보리 톤' },
]

const notificationWeekdays: Array<{ value: NotificationWeekday; label: string }> = [
  { value: 'MON', label: '월' },
  { value: 'TUE', label: '화' },
  { value: 'WED', label: '수' },
  { value: 'THU', label: '목' },
  { value: 'FRI', label: '금' },
  { value: 'SAT', label: '토' },
  { value: 'SUN', label: '일' },
]

const historyItems = [
  {
    title: '오늘 대화',
    description: '지금 이어갈 수 있는 AI 마음대화 화면으로 이동합니다.',
    meta: '채팅 이력',
    icon: MessageCircle,
    action: '대화 이어가기',
    href: '/chat',
  },
  {
    title: '오늘 마음 리포트',
    description: '대화 마무리 후 저장된 마음 리포트를 확인합니다.',
    meta: '리포트 이력',
    icon: FileText,
    action: '리포트 만들기',
    href: '/chat',
  },
  {
    title: '체크인 기록',
    description: '체크인으로 시작한 감정, 컨디션, 회고 흐름을 모아봅니다.',
    meta: '준비 중',
    icon: BookOpen,
    action: '기록 보기',
  },
]

export default function MyPage() {
  const router = useRouter()
  const [activeSection, setActiveSection] = useState<MyPageSection>('overview')
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [profile, setProfile] = useState<MyProfileResponse | null>(null)
  const [profileMessage, setProfileMessage] = useState('')
  const [themeTone, setThemeTone] = useState<ThemeTone>('sunset')
  const [notificationEnabled, setNotificationEnabled] = useState(false)
  const [notificationTime, setNotificationTime] = useState('21:00')
  const [notificationWeekdayValues, setNotificationWeekdayValues] = useState<NotificationWeekday[]>(defaultNotificationWeekdays)
  const [notificationPermission, setNotificationPermission] = useState<NotificationPermission | 'unsupported'>('unsupported')
  const [isNotificationSaving, setIsNotificationSaving] = useState(false)
  const [notificationMessage, setNotificationMessage] = useState('')
  const [summary, setSummary] = useState<MyPageSummaryResponse | null>(null)
  const [summaryMessage, setSummaryMessage] = useState('')
  const [chatHistoryRooms, setChatHistoryRooms] = useState<AiChatHistoryRoom[]>([])
  const [selectedChatHistory, setSelectedChatHistory] = useState<AiChatHistoryRoomDetail | null>(null)
  const [reports, setReports] = useState<AiChatReport[]>([])
  const [selectedReport, setSelectedReport] = useState<AiChatReport | null>(null)
  const [checkIns, setCheckIns] = useState<AiChatCheckInHistory[]>([])
  const [historyMessage, setHistoryMessage] = useState('')
  const [reportMessage, setReportMessage] = useState('')
  const [checkInMessage, setCheckInMessage] = useState('')
  const [dialogType, setDialogType] = useState<DialogType>(null)
  const [toastMessage, setToastMessage] = useState('')
  const [loginMethods, setLoginMethods] = useState<LoginMethodsResponse | null>(null)
  const [securityMessage, setSecurityMessage] = useState('')
  const [passwordMessage, setPasswordMessage] = useState('')
  const [isPasswordSaving, setIsPasswordSaving] = useState(false)

  useEffect(() => {
    const accessToken = localStorage.getItem('myMentalCare.accessToken')
    setIsAuthenticated(Boolean(accessToken))

    const savedThemeTone = localStorage.getItem(THEME_TONE_STORAGE_KEY)
    if (savedThemeTone === 'rose') {
      setThemeTone('wood')
      localStorage.setItem(THEME_TONE_STORAGE_KEY, 'wood')
    } else if (savedThemeTone === 'sunset' || savedThemeTone === 'cream' || savedThemeTone === 'wood') {
      setThemeTone(savedThemeTone)
    }

    setNotificationPermission(typeof Notification === 'undefined' ? 'unsupported' : Notification.permission)

    if (!accessToken) {
      return
    }

    readMyProfile(accessToken)
      .then((nextProfile) => {
        setProfile(nextProfile)
        setProfileMessage('')
      })
      .catch((error) => {
        setProfileMessage(error instanceof LoginApiError ? error.message : '프로필 정보를 불러오지 못했습니다.')
      })

    readNotificationSetting()
      .then((setting) => {
        setNotificationEnabled(setting.enabled)
        setNotificationTime(setting.notificationTime)
        setNotificationWeekdayValues(setting.weekdays.length > 0 ? setting.weekdays : defaultNotificationWeekdays)
        setNotificationMessage('')
      })
      .catch((error) => {
        setNotificationMessage(error instanceof LoginApiError ? error.message : '알림 설정을 불러오지 못했습니다.')
      })

    readMyPageSummary()
      .then((nextSummary) => {
        setSummary(nextSummary)
        setSummaryMessage('')
      })
      .catch((error) => {
        setSummaryMessage(error instanceof LoginApiError ? error.message : '마이페이지 요약을 불러오지 못했습니다.')
      })

    readAiChatHistoryRooms()
      .then((rooms) => {
        setChatHistoryRooms(rooms)
        setHistoryMessage('')
      })
      .catch((error) => {
        setHistoryMessage(error instanceof LoginApiError ? error.message : '채팅 이력을 불러오지 못했습니다.')
      })

    readAiChatReports()
      .then((nextReports) => {
        setReports(nextReports)
        setReportMessage('')
      })
      .catch((error) => {
        setReportMessage(error instanceof LoginApiError ? error.message : '마음 리포트 보관함을 불러오지 못했습니다.')
      })

    readAiChatCheckIns()
      .then((nextCheckIns) => {
        setCheckIns(nextCheckIns)
        setCheckInMessage('')
      })
      .catch((error) => {
        setCheckInMessage(error instanceof LoginApiError ? error.message : '체크인 기록을 불러오지 못했습니다.')
      })

    readLoginMethods()
      .then((methods) => {
        setLoginMethods(methods)
        setSecurityMessage('')
      })
      .catch((error) => {
        setSecurityMessage(error instanceof LoginApiError ? error.message : '계정 보안 정보를 불러오지 못했습니다.')
      })
  }, [])

  const profileRows = useMemo(
    () => [
      { label: '이름', value: profile?.name ?? (profileMessage ? '확인 필요' : '불러오는 중') },
      { label: '로그인 아이디', value: profile?.loginId ?? (profileMessage ? '확인 필요' : '불러오는 중') },
      { label: '이메일', value: profile?.email || '등록하지 않음' },
      { label: '전화번호', value: profile?.phone || '등록하지 않음' },
    ],
    [profile, profileMessage],
  )

  const handleThemeChange = (nextThemeTone: ThemeTone) => {
    const nextTheme = themes.find((theme) => theme.value === nextThemeTone)
    setThemeTone(nextThemeTone)
    localStorage.setItem(THEME_TONE_STORAGE_KEY, nextThemeTone)
    setToastMessage(`${nextTheme?.label ?? '선택한'} 색감이 이 기기에 적용되었습니다.`)
  }

  const handleNotificationWeekdayToggle = (weekday: NotificationWeekday) => {
    setNotificationWeekdayValues((currentWeekdays) => {
      if (currentWeekdays.includes(weekday)) {
        return currentWeekdays.filter((currentWeekday) => currentWeekday !== weekday)
      }

      return [...currentWeekdays, weekday]
    })
  }

  const handleNotificationPermissionRequest = async () => {
    if (typeof Notification === 'undefined') {
      setNotificationPermission('unsupported')
      setNotificationMessage('이 브라우저에서는 알림 권한 요청을 지원하지 않습니다.')
      return
    }

    if (Notification.permission === 'denied') {
      setNotificationPermission('denied')
      setNotificationMessage('브라우저에서 알림이 차단되어 있습니다. 사이트 설정에서 알림을 허용해주세요.')
      return
    }

    const nextPermission = await Notification.requestPermission()
    setNotificationPermission(nextPermission)
    setNotificationMessage(
      nextPermission === 'granted'
        ? '브라우저 알림 권한이 허용되었습니다.'
        : '알림 권한을 허용해야 설정한 시간에 알림을 받을 수 있습니다.',
    )
  }

  const handleNotificationSave = async () => {
    setNotificationMessage('')
    if (notificationWeekdayValues.length === 0) {
      setNotificationMessage('알림 요일을 1개 이상 선택해주세요.')
      return
    }

    setIsNotificationSaving(true)
    try {
      const setting = await updateNotificationSetting({
        enabled: notificationEnabled,
        notificationTime,
        weekdays: notificationWeekdayValues,
      })
      setNotificationEnabled(setting.enabled)
      setNotificationTime(setting.notificationTime)
      setNotificationWeekdayValues(setting.weekdays)
      setSummary((currentSummary) =>
        currentSummary
          ? {
              ...currentSummary,
              notificationEnabled: setting.enabled,
              notificationTime: setting.notificationTime,
            }
          : currentSummary,
      )
      const currentPermission = typeof Notification === 'undefined' ? 'unsupported' : Notification.permission
      setNotificationPermission(currentPermission)
      if (setting.enabled && currentPermission === 'denied') {
        setNotificationMessage('설정은 저장했지만 브라우저 알림이 차단되어 실제 알림은 표시되지 않습니다.')
      } else if (setting.enabled && currentPermission === 'default') {
        setNotificationMessage('설정은 저장했습니다. 알림을 받으려면 브라우저 권한도 허용해주세요.')
      }
      setToastMessage('마음 체크 알림 설정이 저장되었습니다.')
    } catch (error) {
      setNotificationMessage(error instanceof LoginApiError ? error.message : '알림 설정을 저장하지 못했습니다.')
    } finally {
      setIsNotificationSaving(false)
    }
  }

  const handleLogout = () => {
    localStorage.removeItem('myMentalCare.accessToken')
    localStorage.removeItem('myMentalCare.refreshToken')
    sessionStorage.setItem(LOGOUT_NOTICE_REQUEST_KEY, '1')
    router.push('/')
  }

  const handleHistoryAction = (href?: string) => {
    if (href) {
      router.push(href)
      return
    }
    setToastMessage('이력 상세 조회 API가 연결되면 이 화면에서 바로 확인할 수 있습니다.')
  }

  const handleChatHistoryDetail = async (roomId: number) => {
    setHistoryMessage('')
    try {
      const detail = await readAiChatHistoryRoom(roomId)
      setSelectedChatHistory(detail)
    } catch (error) {
      setHistoryMessage(error instanceof LoginApiError ? error.message : '채팅 상세 이력을 불러오지 못했습니다.')
    }
  }

  const handleReportDetail = async (reportId: number) => {
    setReportMessage('')
    try {
      const report = await readAiChatReport(reportId)
      setSelectedReport(report)
    } catch (error) {
      setReportMessage(error instanceof LoginApiError ? error.message : '마음 리포트를 불러오지 못했습니다.')
    }
  }

  const handleDeleteHistory = async (
    targetType: DeleteAiChatHistoryTargetType,
    targetId: number,
    targetLabel: string,
  ) => {
    const confirmed = window.confirm(`${targetLabel}을 삭제할까요? 삭제한 이력은 되돌릴 수 없습니다.`)
    if (!confirmed) {
      return
    }

    setHistoryMessage('')
    setReportMessage('')
    setCheckInMessage('')

    try {
      const response = await deleteAiChatHistory(targetType, targetId)
      if (response.deletedCount <= 0) {
        setToastMessage('삭제할 수 있는 이력을 찾지 못했습니다.')
        return
      }

      if (targetType === 'CHAT_ROOM') {
        setChatHistoryRooms((currentRooms) => currentRooms.filter((room) => room.roomId !== targetId))
        setSelectedChatHistory((currentDetail) => (currentDetail?.roomId === targetId ? null : currentDetail))
      }

      if (targetType === 'REPORT') {
        setReports((currentReports) => currentReports.filter((report) => report.reportId !== targetId))
        setSelectedReport((currentReport) => (currentReport?.reportId === targetId ? null : currentReport))
      }

      if (targetType === 'CHECK_IN') {
        setCheckIns((currentCheckIns) => currentCheckIns.filter((checkIn) => checkIn.checkInId !== targetId))
      }

      setToastMessage(`${targetLabel}이 삭제되었습니다.`)
    } catch (error) {
      const message = error instanceof LoginApiError ? error.message : '선택한 이력을 삭제하지 못했습니다.'
      if (targetType === 'REPORT') {
        setReportMessage(message)
        return
      }
      if (targetType === 'CHECK_IN') {
        setCheckInMessage(message)
        return
      }
      setHistoryMessage(message)
    }
  }

  const handleProfileUpdate = async (request: UpdateMyProfileRequest) => {
    const nextProfile = await updateMyProfile(request)
    setProfile(nextProfile)
    setProfileMessage('')
  }

  const handleWithdrawAccount = async (request: WithdrawMemberRequest) => {
    await withdrawMyAccount(request)
    sessionStorage.setItem(WITHDRAWAL_NOTICE_REQUEST_KEY, '1')
    router.push('/')
  }

  const handlePasswordChange = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setPasswordMessage('')
    const formData = new FormData(event.currentTarget)
    const currentPassword = String(formData.get('currentPassword') ?? '')
    const newPassword = String(formData.get('newPassword') ?? '')
    const newPasswordConfirm = String(formData.get('newPasswordConfirm') ?? '')

    if (!currentPassword || !newPassword || !newPasswordConfirm) {
      setPasswordMessage('현재 비밀번호와 새 비밀번호를 모두 입력해주세요.')
      return
    }

    if (newPassword !== newPasswordConfirm) {
      setPasswordMessage('새 비밀번호 확인이 일치하지 않습니다.')
      return
    }

    if (newPassword.length < 8) {
      setPasswordMessage('새 비밀번호는 8자 이상 입력해주세요.')
      return
    }

    setIsPasswordSaving(true)
    try {
      await changeMyPassword({ currentPassword, newPassword })
      event.currentTarget.reset()
      setToastMessage('비밀번호가 변경되었습니다. 다시 로그인해주세요.')
      router.push('/')
    } catch (error) {
      setPasswordMessage(error instanceof LoginApiError ? error.message : '비밀번호를 변경하지 못했습니다.')
    } finally {
      setIsPasswordSaving(false)
    }
  }

  const formatShortDateTime = (dateTime?: string | null) => {
    if (!dateTime) {
      return null
    }

    return new Date(dateTime).toLocaleString('ko-KR', {
      month: 'numeric',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  if (!isAuthenticated) {
    return (
      <main className="page-shell mypage-shell" data-theme-tone={themeTone}>
        <section className="mypage-login-required" aria-labelledby="login-required-title">
          <span className="mypage-login-icon">
            <HeartHandshake size={28} aria-hidden="true" />
          </span>
          <p className="eyebrow">마이페이지</p>
          <h1 id="login-required-title">로그인이 필요합니다</h1>
          <p>프로필, 대화 이력, 마음 리포트는 로그인 후 확인할 수 있습니다.</p>
          <div className="mypage-login-actions">
            <button className="primary-button" type="button" onClick={() => router.push('/')}>
              홈으로 이동하기
              <Home size={18} aria-hidden="true" />
            </button>
            <button className="soft-button" type="button" onClick={() => router.push('/chat')}>
              AI 마음대화 보기
              <MessageCircle size={18} aria-hidden="true" />
            </button>
          </div>
        </section>
      </main>
    )
  }

  return (
    <main className="page-shell mypage-shell" data-theme-tone={themeTone}>
      <section className="mypage-layout" aria-labelledby="mypage-heading">
        <aside className="mypage-sidebar" aria-label="마이페이지 메뉴">
          <button className="mypage-back-button" type="button" onClick={() => router.push('/')}>
            <ArrowLeft size={18} aria-hidden="true" />
            홈으로
          </button>

          <div className="mypage-profile-card">
            <span className="mypage-avatar" aria-hidden="true">
              {profile?.name?.slice(0, 1) || '마'}
            </span>
            <div>
              <strong>{profile?.name ?? '내 마음'}</strong>
              <span>{profile?.loginId ?? (profileMessage ? '프로필 확인 필요' : '프로필 확인 중')}</span>
            </div>
          </div>

          <nav className="mypage-section-nav">
            {sections.map((section) => {
              const Icon = section.icon
              return (
                <button
                  className={activeSection === section.id ? 'is-active' : ''}
                  type="button"
                  key={section.id}
                  onClick={() => setActiveSection(section.id)}
                >
                  <Icon size={18} aria-hidden="true" />
                  <span>{section.label}</span>
                </button>
              )
            })}
          </nav>
        </aside>

        <div className="mypage-content">
          <header className="mypage-header">
            <div>
              <p className="eyebrow">내 마음 케어 공간</p>
              <h1 id="mypage-heading">마이페이지</h1>
              <p>필요한 관리 항목을 선택해 내 정보와 기록을 확인합니다.</p>
            </div>
          </header>

          {profileMessage && (
            <div className="mypage-alert" role="status">
              <AlertTriangle size={18} aria-hidden="true" />
              <span>{profileMessage}</span>
            </div>
          )}

          {activeSection === 'overview' && (
            <>
              <section className="mypage-overview-grid" aria-label="마이페이지 요약">
                <article className="mypage-hero-card">
                  <span className="mypage-card-icon">
                    <HeartHandshake size={22} aria-hidden="true" />
                  </span>
                  <div>
                    <p className="eyebrow">오늘 상태</p>
                    <h2>{profile?.name ? `${profile.name}님의 기록` : '내 기록'}</h2>
                    <p>대화를 이어가고, 저장된 기록과 알림 상태를 빠르게 확인합니다.</p>
                  </div>
                  <div className="mypage-hero-actions">
                    <button className="primary-button" type="button" onClick={() => router.push('/chat')}>
                      대화 이어가기
                      <MessageCircle size={18} aria-hidden="true" />
                    </button>
                    <button className="soft-button" type="button" onClick={() => setActiveSection('history')}>
                      지난 기록 보기
                      <ClipboardList size={18} aria-hidden="true" />
                    </button>
                  </div>
                </article>

                <article className="mypage-mini-card">
                  <MessageCircle size={21} aria-hidden="true" />
                  <strong>채팅</strong>
                  <span>
                    {summaryMessage
                      ? '확인 필요'
                      : summary
                        ? summary.hasTodayChat
                          ? `오늘 ${summary.todayMessageCount}개`
                          : '오늘 대화 없음'
                        : '불러오는 중'}
                  </span>
                </article>
                <article className="mypage-mini-card">
                  <FileText size={21} aria-hidden="true" />
                  <strong>리포트</strong>
                  <span>
                    {summaryMessage
                      ? '확인 필요'
                      : summary
                        ? summary.reportCount > 0
                          ? `${summary.reportCount}개 저장`
                          : '저장된 리포트 없음'
                        : '불러오는 중'}
                  </span>
                </article>
                <article className="mypage-mini-card">
                  <Bell size={21} aria-hidden="true" />
                  <strong>알림</strong>
                  <span>{summary?.notificationEnabled ? summary.notificationTime : notificationEnabled ? notificationTime : '꺼짐'}</span>
                </article>
              </section>

              {summary && (
                <section className="mypage-summary-strip" aria-label="마이페이지 상세 요약">
                  <span>
                    <b>최근 대화</b>
                    {formatShortDateTime(summary.recentChatAt) ?? '아직 없음'}
                  </span>
                  <span>
                    <b>최근 리포트</b>
                    {formatShortDateTime(summary.latestReportAt) ?? '아직 없음'}
                  </span>
                </section>
              )}
            </>
          )}

          {summaryMessage && (
            <div className="mypage-alert" role="status">
              <AlertTriangle size={18} aria-hidden="true" />
              <span>{summaryMessage}</span>
            </div>
          )}

          {activeSection === 'profile' && (
            <section className="mypage-panel" aria-labelledby="profile-section-title">
              <PanelHeader
                eyebrow="프로필"
                title="내 프로필 조회"
                description="현재 로그인된 계정의 기본 정보를 확인합니다."
                icon={UserRound}
              />
              <dl className="mypage-profile-list">
                {profileRows.map((row) => (
                  <div key={row.label}>
                    <dt>{row.label}</dt>
                    <dd>{row.value}</dd>
                  </div>
                ))}
              </dl>
              <div className="mypage-action-row">
                <button className="soft-button" type="button" onClick={() => setDialogType('editProfile')}>
                  개인정보 수정
                  <ChevronRight size={17} aria-hidden="true" />
                </button>
              </div>
            </section>
          )}

          {activeSection === 'history' && (
            <section className="mypage-panel" aria-labelledby="history-section-title">
              <PanelHeader
                eyebrow="내 이력"
                title="채팅과 리포트 조회"
                description="날짜별 채팅 이력을 확인하고 필요한 대화 내용을 다시 볼 수 있습니다."
                icon={ClipboardList}
              />
              <div className="mypage-chat-history-list">
                {historyMessage && (
                  <div className="mypage-alert" role="status">
                    <AlertTriangle size={18} aria-hidden="true" />
                    <span>{historyMessage}</span>
                  </div>
                )}
                {!historyMessage && chatHistoryRooms.length === 0 && (
                  <div className="mypage-empty-state">
                    <strong>아직 저장된 채팅 이력이 없습니다</strong>
                    <span>AI 마음대화를 시작하면 날짜별 기록이 이곳에 정리됩니다.</span>
                    <button className="soft-button" type="button" onClick={() => router.push('/chat')}>
                      대화 시작하기
                      <MessageCircle size={16} aria-hidden="true" />
                    </button>
                  </div>
                )}
                {chatHistoryRooms.map((room) => (
                  <article className="mypage-chat-history-item" key={room.roomId}>
                    <div>
                      <strong>{new Date(room.conversationDate).toLocaleDateString('ko-KR')}</strong>
                      <p>{room.latestMessage || '아직 메시지가 없습니다.'}</p>
                      <small>{room.messageCount}개 메시지 · {room.latestMessageAt ? formatShortDateTime(room.latestMessageAt) : '최근 메시지 없음'}</small>
                    </div>
                    <div className="mypage-item-actions">
                      <button type="button" onClick={() => handleChatHistoryDetail(room.roomId)}>
                        상세 보기
                        <ChevronRight size={16} aria-hidden="true" />
                      </button>
                      <button
                        className="mypage-inline-danger-button"
                        type="button"
                        onClick={() => handleDeleteHistory('CHAT_ROOM', room.roomId, '이 대화')}
                      >
                        <Trash2 size={15} aria-hidden="true" />
                        이 대화 삭제
                      </button>
                    </div>
                  </article>
                ))}
              </div>
              <div className="mypage-history-list">
                {historyItems.map((item) => {
                  const Icon = item.icon
                  return (
                    <article className="mypage-history-item" key={item.title}>
                      <span className="mypage-history-icon">
                        <Icon size={19} aria-hidden="true" />
                      </span>
                      <div>
                        <strong>{item.title}</strong>
                        <p>{item.description}</p>
                        <small>{item.meta}</small>
                      </div>
                      <button type="button" onClick={() => handleHistoryAction(item.href)}>
                        {item.action}
                        <ChevronRight size={16} aria-hidden="true" />
                      </button>
                    </article>
                  )
                })}
              </div>
              <div className="mypage-report-list">
                {reportMessage && (
                  <div className="mypage-alert" role="status">
                    <AlertTriangle size={18} aria-hidden="true" />
                    <span>{reportMessage}</span>
                  </div>
                )}
                {!reportMessage && reports.length === 0 && (
                  <div className="mypage-empty-state">
                    <strong>저장된 마음 리포트가 없습니다</strong>
                    <span>대화를 마무리하면 오늘 마음의 요약이 리포트로 남습니다.</span>
                  </div>
                )}
                {reports.map((report) => (
                  <article className="mypage-report-item" key={report.reportId}>
                    <div>
                      <strong>{new Date(report.conversationDate).toLocaleDateString('ko-KR')} 마음 리포트</strong>
                      <p>{report.summary}</p>
                      <small>{report.primaryEmotion} · {report.reportType === 'FULL' ? '충분한 대화' : '짧은 대화'}</small>
                    </div>
                    <div className="mypage-item-actions">
                      <button type="button" onClick={() => handleReportDetail(report.reportId)}>
                        리포트 보기
                        <ChevronRight size={16} aria-hidden="true" />
                      </button>
                      <button
                        className="mypage-inline-danger-button"
                        type="button"
                        onClick={() => handleDeleteHistory('REPORT', report.reportId, '이 리포트')}
                      >
                        <Trash2 size={15} aria-hidden="true" />
                        리포트 삭제
                      </button>
                    </div>
                  </article>
                ))}
              </div>
              <div className="mypage-checkin-list">
                {checkInMessage && (
                  <div className="mypage-alert" role="status">
                    <AlertTriangle size={18} aria-hidden="true" />
                    <span>{checkInMessage}</span>
                  </div>
                )}
                {!checkInMessage && checkIns.length === 0 && (
                  <div className="mypage-empty-state">
                    <strong>저장된 체크인 기록이 없습니다</strong>
                    <span>체크인으로 시작한 대화의 선택 답변이 이곳에 쌓입니다.</span>
                  </div>
                )}
                {checkIns.map((checkIn) => (
                  <article className="mypage-checkin-item" key={checkIn.checkInId}>
                    <div>
                      <strong>{checkIn.summaryText}</strong>
                      <p>{checkIn.answers.map((answer) => answer.freeText || answer.label || answer.value).filter(Boolean).join(' · ')}</p>
                      <small>{checkIn.templateType} · {checkIn.createdAt ? formatShortDateTime(checkIn.createdAt) : '시각 없음'}</small>
                    </div>
                    <div className="mypage-item-actions">
                      <button
                        className="mypage-inline-danger-button"
                        type="button"
                        onClick={() => handleDeleteHistory('CHECK_IN', checkIn.checkInId, '이 체크인 기록')}
                      >
                        <Trash2 size={15} aria-hidden="true" />
                        체크인 삭제
                      </button>
                    </div>
                  </article>
                ))}
              </div>
              <div className="mypage-action-row">
                <button className="danger-soft-button" type="button" onClick={() => setDialogType('deleteHistory')}>
                  <Trash2 size={17} aria-hidden="true" />
                  삭제 방법 안내
                </button>
              </div>
            </section>
          )}

          {activeSection === 'settings' && (
            <section className="mypage-panel" aria-labelledby="settings-section-title">
              <PanelHeader
                eyebrow="설정"
                title="내 공간 설정"
                description="홈에서 분리한 설정을 마이페이지 안에서 관리합니다."
                icon={Palette}
              />
              <div className="mypage-settings-list">
                <div className="mypage-notification-setting">
                  <div>
                    <strong>마음 체크 알림</strong>
                    <span>정해진 시간과 요일에 마음 체크를 떠올릴 수 있게 돕습니다.</span>
                  </div>
                  <div className="mypage-notification-controls">
                    <div className="mypage-notification-permission">
                      <span>
                        브라우저 권한:{' '}
                        {notificationPermission === 'granted'
                          ? '허용됨'
                          : notificationPermission === 'denied'
                            ? '차단됨'
                            : notificationPermission === 'default'
                              ? '미설정'
                              : '지원 안 됨'}
                      </span>
                      {notificationPermission !== 'granted' && (
                        <button className="soft-button" type="button" onClick={handleNotificationPermissionRequest}>
                          {notificationPermission === 'denied' ? '허용 방법 보기' : '알림 권한 허용하기'}
                          <Bell size={16} aria-hidden="true" />
                        </button>
                      )}
                    </div>
                    <div className="mypage-notification-topline">
                      <button
                        className={`toggle-button ${notificationEnabled ? 'is-on' : ''}`}
                        type="button"
                        role="switch"
                        aria-checked={notificationEnabled}
                        disabled={isNotificationSaving}
                        onClick={() => setNotificationEnabled((currentValue) => !currentValue)}
                      >
                        <span />
                      </button>
                      <label>
                        알림 시간
                        <input
                          type="time"
                          value={notificationTime}
                          disabled={isNotificationSaving}
                          onChange={(event) => setNotificationTime(event.target.value)}
                        />
                      </label>
                    </div>
                    <div className="mypage-weekday-grid" aria-label="알림 요일 선택">
                      {notificationWeekdays.map((weekday) => {
                        const isSelected = notificationWeekdayValues.includes(weekday.value)
                        return (
                          <button
                            className={isSelected ? 'is-selected' : ''}
                            type="button"
                            key={weekday.value}
                            aria-pressed={isSelected}
                            disabled={isNotificationSaving}
                            onClick={() => handleNotificationWeekdayToggle(weekday.value)}
                          >
                            {weekday.label}
                          </button>
                        )
                      })}
                    </div>
                    {notificationMessage && <p className="mypage-setting-message">{notificationMessage}</p>}
                    <button className="soft-button" type="button" disabled={isNotificationSaving} onClick={handleNotificationSave}>
                      {isNotificationSaving ? '저장 중...' : '알림 설정 저장'}
                      <CheckCircle2 size={17} aria-hidden="true" />
                    </button>
                  </div>
                </div>
                <div className="mypage-theme-setting">
                  <div>
                    <strong>화면 색상</strong>
                    <span>선택한 색감은 이 기기에서만 적용돼요.</span>
                  </div>
                  <div className="mypage-theme-grid" aria-label="화면 색상 선택">
                    {themes.map((theme) => {
                      const isSelected = themeTone === theme.value
                      return (
                        <button
                          className={`mypage-theme-option mypage-theme-option-${theme.value} ${isSelected ? 'is-selected' : ''}`}
                          type="button"
                          key={theme.value}
                          aria-pressed={isSelected}
                          aria-label={`${theme.label} 색감 선택${isSelected ? ', 현재 적용 중' : ''}`}
                          onClick={() => handleThemeChange(theme.value)}
                        >
                          <span className="theme-swatch" aria-hidden="true" />
                          <strong>{theme.label}</strong>
                          <small>{theme.description}</small>
                          {isSelected && <span className="mypage-theme-selected">적용 중</span>}
                        </button>
                      )
                    })}
                  </div>
                </div>
              </div>
            </section>
          )}

          {activeSection === 'support' && (
            <section className="mypage-panel" aria-labelledby="support-section-title">
              <PanelHeader
                eyebrow="문의"
                title="문의하기"
                description="이용 중 불편한 점이나 기록 확인이 필요하면 여기서 알려주세요."
                icon={Mail}
              />
              <InquiryForm onDone={(message) => setToastMessage(message)} />
            </section>
          )}

          {activeSection === 'security' && (
            <section className="mypage-panel" aria-labelledby="security-section-title">
              <PanelHeader
                eyebrow="계정"
                title="로그인 방식과 계정 보안"
                description="로그인 방식, 비밀번호 변경, 계정 종료 같은 민감한 설정을 구분해 관리합니다."
                icon={ShieldCheck}
              />
              {securityMessage && (
                <div className="mypage-alert" role="status">
                  <AlertTriangle size={18} aria-hidden="true" />
                  <span>{securityMessage}</span>
                </div>
              )}
              <div className="mypage-login-method-grid">
                <article className="mypage-login-method-card">
                  <span className="mypage-history-icon">
                    <KeyRound size={19} aria-hidden="true" />
                  </span>
                  <div>
                    <strong>일반 로그인</strong>
                    <p>{loginMethods?.passwordLoginEnabled ? '아이디와 비밀번호 로그인을 사용할 수 있습니다.' : '카카오 로그인 전용 계정입니다.'}</p>
                    <small>{loginMethods ? (loginMethods.canChangePassword ? '비밀번호 변경 가능' : '비밀번호 변경 불가') : '확인 중'}</small>
                  </div>
                </article>
                <article className="mypage-login-method-card">
                  <span className="mypage-history-icon">
                    <Link2 size={19} aria-hidden="true" />
                  </span>
                  <div>
                    <strong>카카오 로그인</strong>
                    <p>{loginMethods?.socialAccounts.some((account) => account.provider === 'KAKAO') ? '카카오 계정이 연결되어 있습니다.' : '연결된 카카오 계정이 없습니다.'}</p>
                    <small>
                      {loginMethods?.socialAccounts.find((account) => account.provider === 'KAKAO')?.email ||
                        (loginMethods ? '미연결' : '확인 중')}
                    </small>
                  </div>
                </article>
              </div>
              <form className="mypage-password-form" onSubmit={handlePasswordChange}>
                <div>
                  <strong>비밀번호 변경</strong>
                  <span>변경 후 보안을 위해 현재 로그인 상태가 종료됩니다.</span>
                </div>
                {!loginMethods ? (
                  <p className="mypage-setting-message">로그인 방식 정보를 확인하는 중입니다.</p>
                ) : !loginMethods.canChangePassword ? (
                  <p className="mypage-setting-message">카카오 로그인 전용 계정은 현재 비밀번호 변경을 지원하지 않습니다.</p>
                ) : (
                  <>
                    <label>
                      현재 비밀번호
                      <input name="currentPassword" type="password" placeholder="현재 비밀번호" disabled={isPasswordSaving} />
                    </label>
                    <label>
                      새 비밀번호
                      <input name="newPassword" type="password" placeholder="8자 이상" disabled={isPasswordSaving} />
                    </label>
                    <label>
                      새 비밀번호 확인
                      <input name="newPasswordConfirm" type="password" placeholder="새 비밀번호 다시 입력" disabled={isPasswordSaving} />
                    </label>
                    {passwordMessage && <p className="mypage-setting-message">{passwordMessage}</p>}
                    <button className="soft-button" type="submit" disabled={isPasswordSaving}>
                      {isPasswordSaving ? '변경 중...' : '비밀번호 변경'}
                      <CheckCircle2 size={17} aria-hidden="true" />
                    </button>
                  </>
                )}
              </form>
              <div className="mypage-security-actions">
                <button className="soft-button" type="button" onClick={handleLogout}>
                  <LogOut size={17} aria-hidden="true" />
                  로그아웃
                </button>
              </div>
              <div className="mypage-danger-zone">
                <div>
                  <strong>위험 구역</strong>
                  <span>계정을 닫는 작업은 되돌리기 어렵기 때문에 한 번 더 확인합니다.</span>
                </div>
                <button className="danger-soft-button" type="button" onClick={() => setDialogType('withdraw')}>
                  <Trash2 size={17} aria-hidden="true" />
                  회원 탈퇴 안내
                </button>
              </div>
            </section>
          )}
        </div>
      </section>

      {toastMessage && (
        <div className="mypage-toast" role="status">
          <CheckCircle2 size={18} aria-hidden="true" />
          <span>{toastMessage}</span>
          <button type="button" aria-label="알림 닫기" onClick={() => setToastMessage('')}>
            <X size={15} aria-hidden="true" />
          </button>
        </div>
      )}

      {dialogType && (
        <MyPageDialog
          type={dialogType}
          profile={profile}
          onProfileUpdate={handleProfileUpdate}
          onWithdrawAccount={handleWithdrawAccount}
          onClose={() => setDialogType(null)}
          onDone={(message) => {
            setDialogType(null)
            setToastMessage(message)
          }}
        />
      )}

      {selectedChatHistory && (
        <ChatHistoryDialog detail={selectedChatHistory} onClose={() => setSelectedChatHistory(null)} />
      )}

      {selectedReport && (
        <ReportDialog report={selectedReport} onClose={() => setSelectedReport(null)} />
      )}
    </main>
  )
}

function ChatHistoryDialog({ detail, onClose }: { detail: AiChatHistoryRoomDetail; onClose: () => void }) {
  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section
        className="auth-modal mypage-dialog mypage-chat-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="chat-history-dialog-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button className="icon-button" type="button" aria-label="채팅 이력 모달 닫기" onClick={onClose}>
          <X size={20} aria-hidden="true" />
        </button>
        <p className="eyebrow">읽기 전용</p>
        <h2 id="chat-history-dialog-title">{new Date(detail.conversationDate).toLocaleDateString('ko-KR')} 대화</h2>
        <p className="modal-description">저장된 대화를 확인하는 화면입니다. 내용 수정은 지원하지 않습니다.</p>
        <div className="mypage-chat-message-list">
          {detail.messages.map((message) => (
            <article className={message.senderType === 'USER' ? 'is-user' : 'is-assistant'} key={message.messageId}>
              <span>{message.senderType === 'USER' ? '나' : detail.chatbotName}</span>
              <p>{message.content}</p>
            </article>
          ))}
        </div>
      </section>
    </div>
  )
}

function ReportDialog({ report, onClose }: { report: AiChatReport; onClose: () => void }) {
  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section
        className="auth-modal mypage-dialog mypage-report-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="report-dialog-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button className="icon-button" type="button" aria-label="리포트 모달 닫기" onClick={onClose}>
          <X size={20} aria-hidden="true" />
        </button>
        <p className="eyebrow">마음 리포트</p>
        <h2 id="report-dialog-title">{new Date(report.conversationDate).toLocaleDateString('ko-KR')} 마음 리포트</h2>
        <div className="mypage-report-detail">
          <section>
            <strong>오늘 마음 요약</strong>
            <p>{report.summary}</p>
          </section>
          <section>
            <strong>주요 감정</strong>
            <p>{report.primaryEmotion}{report.emotionIntensity ? ` · 강도 ${report.emotionIntensity}/5` : ''}</p>
          </section>
          <section>
            <strong>마음 흐름</strong>
            <p>{report.emotionalFlow}</p>
          </section>
          <section>
            <strong>오늘의 문장</strong>
            <p>{report.todaySentence}</p>
          </section>
          <section>
            <strong>추천 노래</strong>
            <div className="mypage-report-songs">
              {report.songs.length === 0 ? (
                <span>추천 노래가 없습니다.</span>
              ) : (
                report.songs.map((song) => (
                  <a href={song.youtubeUrl} target="_blank" rel="noreferrer" key={`${song.title}-${song.artist}`}>
                    <b>{song.title}</b>
                    <small>{song.artist} · {song.reason}</small>
                  </a>
                ))
              )}
            </div>
          </section>
        </div>
      </section>
    </div>
  )
}

function PanelHeader({
  eyebrow,
  title,
  description,
  icon: Icon,
}: {
  eyebrow: string
  title: string
  description: string
  icon: typeof Home
}) {
  return (
    <div className="mypage-panel-header">
      <span className="mypage-panel-icon">
        <Icon size={19} aria-hidden="true" />
      </span>
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h2>{title}</h2>
        <p>{description}</p>
      </div>
    </div>
  )
}

function InquiryForm({ onDone }: { onDone: (message: string) => void }) {
  const [category, setCategory] = useState('이력/리포트')
  const [content, setContent] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [formMessage, setFormMessage] = useState('')
  const [submittedInquiry, setSubmittedInquiry] = useState<CreateInquiryResponse | null>(null)
  const contentRef = useRef<HTMLTextAreaElement>(null)

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const trimmedContent = content.trim()
    setFormMessage('')
    setSubmittedInquiry(null)

    if (trimmedContent.length < 10) {
      setFormMessage('문의 내용은 10자 이상 입력해주세요.')
      requestAnimationFrame(() => contentRef.current?.focus())
      return
    }

    setIsSubmitting(true)
    try {
      const response = await createInquiry({
        category,
        content: trimmedContent,
      })
      setSubmittedInquiry(response)
      setContent('')
      onDone('문의가 접수되었습니다.')
    } catch (error) {
      setFormMessage(error instanceof LoginApiError ? error.message : '문의 접수 중 문제가 발생했습니다.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <form className="mypage-inquiry-form" onSubmit={handleSubmit}>
      <label htmlFor="mypage-inquiry-category">
        문의 유형
        <select
          id="mypage-inquiry-category"
          value={category}
          disabled={isSubmitting}
          onChange={(event) => setCategory(event.target.value)}
        >
          <option>이력/리포트</option>
          <option>계정</option>
          <option>서비스 이용</option>
          <option>기타</option>
        </select>
      </label>
      <label htmlFor="mypage-inquiry-content">
        문의 내용
        <textarea
          id="mypage-inquiry-content"
          ref={contentRef}
          name="content"
          rows={4}
          value={content}
          disabled={isSubmitting}
          placeholder="문의 내용을 적어주세요."
          aria-invalid={Boolean(formMessage)}
          aria-describedby={formMessage ? 'mypage-inquiry-error' : undefined}
          onChange={(event) => setContent(event.target.value)}
        />
      </label>
      {formMessage && (
        <p className="mypage-dialog-message" id="mypage-inquiry-error" role="alert">
          {formMessage}
        </p>
      )}
      {submittedInquiry && (
        <div className="mypage-inquiry-result" role="status">
          <CheckCircle2 size={18} aria-hidden="true" />
          <div>
            <strong>문의 접수 완료</strong>
            <span>
              접수번호 #{submittedInquiry.inquiryId} · {new Date(submittedInquiry.createdAt).toLocaleString('ko-KR')}
            </span>
          </div>
        </div>
      )}
      <button className="primary-button" type="submit" disabled={isSubmitting}>
        {isSubmitting ? '접수 중...' : '문의 남기기'}
        <Mail size={17} aria-hidden="true" />
      </button>
    </form>
  )
}

function MyPageDialog({
  type,
  profile,
  onProfileUpdate,
  onWithdrawAccount,
  onClose,
  onDone,
}: {
  type: Exclude<DialogType, null>
  profile: MyProfileResponse | null
  onProfileUpdate: (request: UpdateMyProfileRequest) => Promise<void>
  onWithdrawAccount: (request: WithdrawMemberRequest) => Promise<void>
  onClose: () => void
  onDone: (message: string) => void
}) {
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [formMessage, setFormMessage] = useState('')

  const titleByType = {
    editProfile: '내 정보 수정',
    deleteHistory: '이력 삭제 안내',
    withdraw: '회원 탈퇴',
  }

  const descriptionByType = {
    editProfile: '대화 기록과 연결되는 기본 정보를 관리합니다. 로그인 아이디와 비밀번호는 계정 관리에서 다룹니다.',
    deleteHistory: '전체 삭제 대신 필요한 항목만 카드별로 선택해 삭제합니다.',
    withdraw: '탈퇴 후 계정은 비활성화되며, 현재 로그인 상태가 종료됩니다. 계속하려면 비밀번호와 확인 문구를 입력해주세요.',
  }

  const eyebrowByType = {
    editProfile: '프로필 정보',
    deleteHistory: '기록 관리',
    withdraw: '계정 관리',
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setFormMessage('')

    if (type === 'editProfile') {
      const formData = new FormData(event.currentTarget)
      const name = String(formData.get('name') ?? '').trim()
      const email = String(formData.get('email') ?? '').trim()
      const phone = String(formData.get('phone') ?? '').trim()

      if (!name) {
        setFormMessage('이름을 입력해주세요.')
        return
      }

      setIsSubmitting(true)
      try {
        await onProfileUpdate({
          name,
          email: email || null,
          phone: phone || null,
        })
        onDone('개인정보가 저장되었습니다.')
      } catch (error) {
        setFormMessage(error instanceof LoginApiError ? error.message : '개인정보를 저장하지 못했습니다.')
      } finally {
        setIsSubmitting(false)
      }
      return
    }
    if (type === 'deleteHistory') {
      onDone('각 이력 카드의 삭제 버튼으로 선택 삭제를 진행할 수 있습니다.')
      return
    }

    const formData = new FormData(event.currentTarget)
    const password = String(formData.get('password') ?? '')
    const confirmationText = String(formData.get('confirmationText') ?? '').trim()

    if (!password) {
      setFormMessage('비밀번호를 입력해주세요.')
      return
    }

    if (confirmationText !== '회원 탈퇴') {
      setFormMessage('확인 문구로 "회원 탈퇴"를 입력해주세요.')
      return
    }

    setIsSubmitting(true)
    try {
      await onWithdrawAccount({ password, confirmationText })
      onDone('회원 탈퇴가 완료되었습니다.')
    } catch (error) {
      setFormMessage(error instanceof LoginApiError ? error.message : '회원 탈퇴를 처리하지 못했습니다.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section
        className="auth-modal mypage-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="mypage-dialog-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button className="icon-button" type="button" aria-label="마이페이지 모달 닫기" onClick={onClose}>
          <X size={20} aria-hidden="true" />
        </button>
        <p className="eyebrow">{eyebrowByType[type]}</p>
        <h2 id="mypage-dialog-title">{titleByType[type]}</h2>
        <p className="modal-description">{descriptionByType[type]}</p>

        <form className="mypage-dialog-form" onSubmit={handleSubmit}>
          {type === 'editProfile' ? (
            <>
              <label>
                이름
                <input name="name" defaultValue={profile?.name ?? ''} placeholder="이름" disabled={isSubmitting} />
              </label>
              <label>
                이메일
                <input name="email" defaultValue={profile?.email ?? ''} placeholder="example@email.com" disabled={isSubmitting} />
              </label>
              <label>
                전화번호
                <input name="phone" defaultValue={profile?.phone ?? ''} placeholder="010-0000-0000" disabled={isSubmitting} />
              </label>
            </>
          ) : type === 'withdraw' ? (
            <>
              <div className="mypage-dialog-warning">
                <AlertTriangle size={20} aria-hidden="true" />
                <span>탈퇴하면 현재 토큰이 무효화되고 마이페이지 접근이 중단됩니다. 저장 데이터 처리는 추후 정책에 따라 별도 관리됩니다.</span>
              </div>
              <label>
                비밀번호
                <input name="password" type="password" placeholder="현재 비밀번호" disabled={isSubmitting} />
              </label>
              <label>
                확인 문구
                <input name="confirmationText" placeholder="회원 탈퇴" disabled={isSubmitting} />
              </label>
            </>
          ) : (
            <div className="mypage-dialog-warning">
              <AlertTriangle size={20} aria-hidden="true" />
              <span>각 카드의 `이 대화 삭제`, `리포트 삭제`, `체크인 삭제` 버튼으로 필요한 기록만 삭제할 수 있습니다.</span>
            </div>
          )}
          {formMessage && (
            <p className="mypage-dialog-message" role="alert">
              {formMessage}
            </p>
          )}
          <div className="modal-actions">
            <button className="ghost-button" type="button" onClick={onClose} disabled={isSubmitting}>
              취소
            </button>
            <button className={type === 'editProfile' ? 'primary-button' : 'danger-button'} type="submit" disabled={isSubmitting}>
              {type === 'editProfile' ? (isSubmitting ? '저장 중...' : '저장하기') : type === 'withdraw' ? (isSubmitting ? '탈퇴 처리 중...' : '회원 탈퇴하기') : '확인'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
