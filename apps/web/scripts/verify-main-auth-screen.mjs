import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const page = readFileSync(resolve('app/page.tsx'), 'utf8')
const styles = readFileSync(resolve('app/globals.css'), 'utf8')
const authApi = readFileSync(resolve('lib/auth-api.ts'), 'utf8')

const checks = [
  ['메인 서비스 이름 표시', page.includes('myMentalCare')],
  ['메인 CTA 버튼 제거', !page.includes('마음 기록 시작하기')],
  ['기존 로그인 CTA 문구 제거', !page.includes('이미 계정이 있어요')],
  ['임시 구현 단계 안내 제거', !page.includes('아직은 화면 구현 단계입니다.')],
  ['설정 메뉴 상태 관리', page.includes('settingsOpen') && page.includes('setSettingsOpen')],
  ['설정 버튼 아이콘 사용', page.includes('Settings') && page.includes('설정 메뉴 열기')],
  ['설정 메뉴 MVP 항목 표시', page.includes('알림 설정') && page.includes('화면 분위기') && page.includes('서비스 안내')],
  ['설정 메뉴 닫기 방식', page.includes("event.key === 'Escape'") && page.includes('closeSettingsFromOutside')],
  ['회원가입 모달 진입 버튼', page.includes("setAuthMode('signup')")],
  ['로그인 모달 진입 버튼', page.includes("setAuthMode('login')")],
  ['회원가입 폼 제목', page.includes('회원가입')],
  ['로그인 폼 제목', page.includes('로그인')],
  ['회원가입 API 호출 함수 사용', page.includes('await signupMember')],
  ['회원가입 성공 후 로그인 전환', page.includes('회원가입이 완료되었습니다') && page.includes("onModeChange('login')")],
  ['로그인 API 호출 함수 사용', page.includes('await loginMember')],
  ['로그인 성공 토큰 저장', page.includes('myMentalCare.accessToken') && page.includes('myMentalCare.refreshToken')],
  ['로그인 성공 후 인증 상태 반영', page.includes('setIsAuthenticated(true)') && page.includes('onLoginSuccess')],
  ['로그인 후 프로필/로그아웃 버튼 표시', page.includes('프로필') && page.includes('로그아웃')],
  ['프로필 API 호출 함수 사용', page.includes('await readMyProfile')],
  ['프로필 모달 표시', page.includes('내 프로필') && page.includes('profile-detail')],
  ['로그아웃 시 토큰 제거', page.includes("localStorage.removeItem('myMentalCare.accessToken')")],
  ['로그아웃 완료 안내 메시지', page.includes('로그아웃되었습니다.') && page.includes('session-message')],
  ['로그인 API endpoint 확인', authApi.includes('/api/auth/login')],
  ['회원가입 API endpoint 확인', authApi.includes('/api/members/signup')],
  ['프로필 API endpoint 확인', authApi.includes('/api/members/me')],
  ['토큰 재발급 API endpoint 확인', authApi.includes('/api/auth/reissue')],
  ['401 응답 시 재발급 시도', authApi.includes('response.status !== 401') && authApi.includes('reissueToken')],
  ['재발급 성공 후 토큰 저장', authApi.includes('storeLoginTokens(await reissueToken')],
  ['재발급 실패 시 토큰 제거', authApi.includes('clearLoginTokens()')],
  ['로그인 실패 오류 메시지 처리', authApi.includes('LoginApiError')],
  ['정신 건강 서비스 디자인 문구', page.includes('따뜻한 개인 멘탈 케어')],
  ['모달 backdrop 스타일', styles.includes('.modal-backdrop')],
  ['메인 카드 hover 스타일', styles.includes('.feature-card:hover') && styles.includes('.care-panel:hover')],
  ['설정 메뉴 스타일', styles.includes('.settings-menu') && styles.includes('.settings-button')],
  ['모바일 반응형 스타일', styles.includes('@media (max-width: 860px)')],
]

const failed = checks.filter(([, passed]) => !passed)

if (failed.length > 0) {
  console.error('main auth screen smoke checks failed')
  for (const [name] of failed) {
    console.error(`- ${name}`)
  }
  process.exit(1)
}

console.log('main auth screen smoke checks passed')
