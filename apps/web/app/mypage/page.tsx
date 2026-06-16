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
import { FormEvent, useEffect, useMemo, useState } from 'react'
import { LoginApiError, MyProfileResponse, UpdateMyProfileRequest, readMyProfile, updateMyProfile } from '@/lib/auth-api'
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
const NOTIFICATION_STORAGE_KEY = 'myMentalCare.notificationEnabled'
const LOGOUT_NOTICE_REQUEST_KEY = 'myMentalCare.logoutNotice'
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
    description: '오늘 이어간 마음이와의 대화방으로 이동합니다.',
    meta: '채팅 이력',
    icon: MessageCircle,
    action: '대화 보기',
    href: '/chat',
  },
  {
    title: '오늘 마음 리포트',
    description: '대화 마무리 후 저장된 마음 리포트를 확인합니다.',
    meta: '리포트 이력',
    icon: FileText,
    action: '리포트 보기',
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
  const [isNotificationSaving, setIsNotificationSaving] = useState(false)
  const [notificationMessage, setNotificationMessage] = useState('')
  const [summary, setSummary] = useState<MyPageSummaryResponse | null>(null)
  const [summaryMessage, setSummaryMessage] = useState('')
  const [dialogType, setDialogType] = useState<DialogType>(null)
  const [toastMessage, setToastMessage] = useState('')

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

    setNotificationEnabled(localStorage.getItem(NOTIFICATION_STORAGE_KEY) === '1')

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
        localStorage.setItem(NOTIFICATION_STORAGE_KEY, setting.enabled ? '1' : '0')
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
    setToastMessage(`${nextTheme?.label ?? '선택한'} 테마가 이 기기에 적용되었습니다.`)
  }

  const handleNotificationWeekdayToggle = (weekday: NotificationWeekday) => {
    setNotificationWeekdayValues((currentWeekdays) => {
      if (currentWeekdays.includes(weekday)) {
        return currentWeekdays.filter((currentWeekday) => currentWeekday !== weekday)
      }

      return [...currentWeekdays, weekday]
    })
  }

  const handleNotificationSave = async () => {
    setNotificationMessage('')
    if (notificationWeekdayValues.length === 0) {
      setNotificationMessage('알림 요일을 1개 이상 선택해주세요.')
      return
    }

    if (notificationEnabled && typeof Notification !== 'undefined' && Notification.permission === 'denied') {
      setNotificationMessage('브라우저 알림 권한이 차단되어 있습니다. 설정은 저장되지만 알림은 표시되지 않을 수 있습니다.')
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
      localStorage.setItem(NOTIFICATION_STORAGE_KEY, setting.enabled ? '1' : '0')
      setSummary((currentSummary) =>
        currentSummary
          ? {
              ...currentSummary,
              notificationEnabled: setting.enabled,
              notificationTime: setting.notificationTime,
            }
          : currentSummary,
      )
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

  const handleProfileUpdate = async (request: UpdateMyProfileRequest) => {
    const nextProfile = await updateMyProfile(request)
    setProfile(nextProfile)
    setProfileMessage('')
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
              <p>프로필, 대화 이력, 마음 리포트, 설정을 한 곳에서 관리합니다.</p>
            </div>
            <button className="ghost-button mypage-mobile-home" type="button" onClick={() => router.push('/')}>
              <Home size={18} aria-hidden="true" />
              홈
            </button>
          </header>

          {profileMessage && (
            <div className="mypage-alert" role="status">
              <AlertTriangle size={18} aria-hidden="true" />
              <span>{profileMessage}</span>
            </div>
          )}

          <section className="mypage-overview-grid" aria-label="마이페이지 요약">
            <article className="mypage-hero-card">
              <span className="mypage-card-icon">
                <HeartHandshake size={22} aria-hidden="true" />
              </span>
              <div>
                <p className="eyebrow">오늘의 케어</p>
                <h2>{profile?.name ? `${profile.name}님의 마음 공간` : '내 마음 공간'}</h2>
                <p>오늘 대화를 이어가거나, 마무리 리포트를 확인하고, 필요한 설정을 조정할 수 있습니다.</p>
              </div>
              <div className="mypage-hero-actions">
                <button className="primary-button" type="button" onClick={() => router.push('/chat')}>
                  오늘 대화 이어가기
                  <MessageCircle size={18} aria-hidden="true" />
                </button>
                <button className="soft-button" type="button" onClick={() => setActiveSection('history')}>
                  이력 보기
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
              <span>최근 대화: {formatShortDateTime(summary.recentChatAt) ?? '아직 없음'}</span>
              <span>최근 리포트: {formatShortDateTime(summary.latestReportAt) ?? '아직 없음'}</span>
            </section>
          )}

          {summaryMessage && (
            <div className="mypage-alert" role="status">
              <AlertTriangle size={18} aria-hidden="true" />
              <span>{summaryMessage}</span>
            </div>
          )}

          {(activeSection === 'overview' || activeSection === 'profile') && (
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

          {(activeSection === 'overview' || activeSection === 'history') && (
            <section className="mypage-panel" aria-labelledby="history-section-title">
              <PanelHeader
                eyebrow="내 이력"
                title="채팅과 리포트 조회"
                description="가장 먼저 필요한 이력부터 빠르게 접근합니다. 상세 조회/삭제 API는 후속 구현에서 연결합니다."
                icon={ClipboardList}
              />
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
              <div className="mypage-action-row">
                <button className="danger-soft-button" type="button" onClick={() => setDialogType('deleteHistory')}>
                  <Trash2 size={17} aria-hidden="true" />
                  이력 삭제 요청
                </button>
              </div>
            </section>
          )}

          {(activeSection === 'overview' || activeSection === 'settings') && (
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
                    <span>선택한 색상은 현재 기기에 저장됩니다. 다른 기기에서는 다시 선택할 수 있습니다.</span>
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

          {(activeSection === 'overview' || activeSection === 'support') && (
            <section className="mypage-panel" aria-labelledby="support-section-title">
              <PanelHeader
                eyebrow="문의"
                title="문의하기"
                description="계정, 이력, 리포트 관련 문의를 남길 수 있는 위치입니다."
                icon={Mail}
              />
              <InquiryForm onDone={(message) => setToastMessage(message)} />
            </section>
          )}

          {activeSection === 'security' && (
            <section className="mypage-panel" aria-labelledby="security-section-title">
              <PanelHeader
                eyebrow="계정"
                title="로그아웃 및 계정 관리"
                description="로그아웃은 여기에서 진행하고, 회원 탈퇴는 데이터 보관 안내와 본인 확인을 거친 뒤 진행합니다."
                icon={ShieldCheck}
              />
              <div className="mypage-security-actions">
                <button className="soft-button" type="button" onClick={handleLogout}>
                  <LogOut size={17} aria-hidden="true" />
                  로그아웃
                </button>
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
          onClose={() => setDialogType(null)}
          onDone={(message) => {
            setDialogType(null)
            setToastMessage(message)
          }}
        />
      )}
    </main>
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

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const trimmedContent = content.trim()
    setFormMessage('')
    setSubmittedInquiry(null)

    if (trimmedContent.length < 10) {
      setFormMessage('문의 내용은 10자 이상 입력해주세요.')
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
      <label>
        문의 유형
        <select value={category} disabled={isSubmitting} onChange={(event) => setCategory(event.target.value)}>
          <option>이력/리포트</option>
          <option>계정</option>
          <option>서비스 이용</option>
          <option>기타</option>
        </select>
      </label>
      <label>
        문의 내용
        <textarea
          name="content"
          rows={4}
          value={content}
          disabled={isSubmitting}
          placeholder="문의 내용을 적어주세요."
          onChange={(event) => setContent(event.target.value)}
        />
      </label>
      {formMessage && (
        <p className="mypage-dialog-message" role="alert">
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
  onClose,
  onDone,
}: {
  type: Exclude<DialogType, null>
  profile: MyProfileResponse | null
  onProfileUpdate: (request: UpdateMyProfileRequest) => Promise<void>
  onClose: () => void
  onDone: (message: string) => void
}) {
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [formMessage, setFormMessage] = useState('')

  const titleByType = {
    editProfile: '개인정보 수정',
    deleteHistory: '이력 삭제 요청',
    withdraw: '회원 탈퇴 안내',
  }

  const descriptionByType = {
    editProfile: '이름, 이메일, 전화번호를 수정합니다. 로그인 아이디와 비밀번호는 이 화면에서 변경하지 않습니다.',
    deleteHistory: '채팅과 리포트 삭제는 복구가 어려운 작업이므로, 삭제 API 연결 시 재확인 절차가 필요합니다.',
    withdraw: '회원 탈퇴는 보관 데이터 안내와 본인 확인이 필요합니다. 지금은 안내 흐름만 확인합니다.',
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
      onDone('이력 삭제 API가 연결되면 선택한 이력을 삭제할 수 있습니다.')
      return
    }
    onDone('회원 탈퇴 API가 연결되면 본인 확인 후 진행됩니다.')
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
        <p className="eyebrow">마이페이지</p>
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
          ) : (
            <div className="mypage-dialog-warning">
              <AlertTriangle size={20} aria-hidden="true" />
              <span>{type === 'deleteHistory' ? '삭제 전 보관 범위와 복구 불가 여부를 다시 안내해야 합니다.' : '탈퇴 전 저장된 대화와 리포트의 처리 방침을 명확히 안내해야 합니다.'}</span>
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
              {type === 'editProfile' ? (isSubmitting ? '저장 중...' : '저장하기') : '확인했습니다'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
