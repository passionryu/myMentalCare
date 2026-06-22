import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const page = readFileSync(resolve('app/page.tsx'), 'utf8')
const styles = readFileSync(resolve('app/globals.css'), 'utf8')
const authApi = readFileSync(resolve('lib/auth-api.ts'), 'utf8')
const themeTone = readFileSync(resolve('lib/theme-tone.ts'), 'utf8')

const checks = [
  ['메인 서비스 이름 표시', page.includes('Haru Mind')],
  ['메인 CTA 버튼 제거', !page.includes('마음 기록 시작하기')],
  ['기존 로그인 CTA 문구 제거', !page.includes('이미 계정이 있어요')],
  ['임시 구현 단계 안내 제거', !page.includes('아직은 화면 구현 단계입니다.')],
  ['홈 설정 버튼 제거', !page.includes('설정 열기') && !page.includes('SettingsButton') && !page.includes('settingsOpen')],
  ['데스크톱 서비스 소개 진입 제공', page.includes('서비스 소개') && page.includes("router.push('/service')")],
  ['따뜻한 화면 배경 5종 제공', ['morning-window', 'breathing-landscape', 'mind-journal', 'chat-bubbles', 'botanical-room'].every((theme) => themeTone.includes(theme))],
  ['기존 화면 색감 저장값 새 배경으로 전환', themeTone.includes('legacyThemeToneMap') && themeTone.includes("rose: 'chat-bubbles'")],
  ['화면 배경 선택값 실제 반영', page.includes('data-theme-tone={themeTone}') && page.includes('readStoredThemeTone') && !page.includes('선택값은 아직 화면 색감에 반영하지 않습니다')],
  ['회원가입 모달 진입 버튼', page.includes("setAuthMode('signup')")],
  ['로그인 모달 진입 버튼', page.includes("setAuthMode('login')")],
  ['회원가입 폼 제목', page.includes('회원가입')],
  ['로그인 폼 제목', page.includes('로그인')],
  ['비밀번호 보기 토글 제공', page.includes('PasswordField') && page.includes('EyeOff') && page.includes('password-toggle')],
  ['회원가입 비밀번호 확인 입력 제공', page.includes('passwordConfirm') && page.includes('비밀번호가 서로 다릅니다')],
  ['회원가입 API 호출 함수 사용', page.includes('await signupMember')],
  ['회원가입 성공 후 로그인 전환', page.includes('회원가입이 완료되었습니다') && page.includes("onModeChange('login')")],
  ['로그인 API 호출 함수 사용', page.includes('await loginMember')],
  ['로그인 성공 토큰 저장', page.includes('myMentalCare.accessToken') && page.includes('myMentalCare.refreshToken')],
  ['로그인 성공 후 인증 상태 반영', page.includes('setIsAuthenticated(true)') && page.includes('onLoginSuccess')],
  ['로그인 후 프로필/로그아웃 버튼 표시', page.includes('프로필') && page.includes('로그아웃')],
  ['프로필 API 호출 함수 사용', page.includes('await readMyProfile')],
  ['프로필 모달 표시', page.includes('내 프로필') && page.includes('profile-detail')],
  ['로그아웃 시 토큰 제거 함수 제공', authApi.includes('export function clearLoginTokens()') && authApi.includes('localStorage.removeItem(accessTokenKey)')],
  ['로그인/로그아웃 안내 모달 제공', page.includes('StatusNoticeModal') && page.includes('로그인되었습니다') && page.includes('로그아웃되었습니다')],
  ['로그인 API endpoint 확인', authApi.includes('/api/auth/login')],
  ['회원가입 API endpoint 확인', authApi.includes('/api/members/signup')],
  ['프로필 API endpoint 확인', authApi.includes('/api/members/me')],
  ['토큰 재발급 API endpoint 확인', authApi.includes('/api/auth/reissue')],
  ['401 응답 시 재발급 시도', authApi.includes('response.status !== 401') && authApi.includes('reissueToken')],
  ['재발급 성공 후 토큰 저장', authApi.includes('storeLoginTokens(tokens)') && authApi.includes('reissueStoredLoginTokens')],
  ['재발급 실패 시 토큰 제거', authApi.includes('clearLoginTokens()')],
  ['로그인 실패 오류 메시지 처리', authApi.includes('LoginApiError')],
  ['정신 건강 서비스 디자인 문구', page.includes('따뜻한 개인 멘탈 케어')],
  ['모달 backdrop 스타일', styles.includes('.modal-backdrop')],
  ['메인 카드 hover 스타일', styles.includes('.feature-card:hover') && styles.includes('.care-panel:hover')],
  ['마이페이지 설정 스타일 유지', styles.includes('.toggle-button') && styles.includes('.mypage-theme-grid')],
  ['화면 배경 테마 스타일', styles.includes(".page-shell[data-theme-tone='breathing-landscape']") && styles.includes(".page-shell[data-theme-tone='botanical-room']") && styles.includes('--theme-visual-layer')],
  ['비밀번호 보기 스타일', styles.includes('.password-field') && styles.includes('.password-toggle') && styles.includes('translateY(-50%)')],
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
