import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const root = resolve(process.cwd())
const page = readFileSync(resolve(root, 'app/page.tsx'), 'utf8')
const chatPage = readFileSync(resolve(root, 'app/chat/page.tsx'), 'utf8')
const styles = readFileSync(resolve(root, 'app/globals.css'), 'utf8')
const checkInTemplates = readFileSync(resolve(root, 'lib/check-in-templates.ts'), 'utf8')

const templateTitles = ['기본 감정형', '대화 시작형', '컨디션 중심형', '하루 회고형']
const templateTypes = ['BASIC_EMOTION', 'CONVERSATION_START', 'CONDITION', 'DAY_REVIEW']

const checks = [
  ['홈 화면이 체크인 템플릿 공유 모듈 사용', page.includes('CHECK_IN_TEMPLATES') && page.includes('PENDING_CHECK_IN_TEMPLATE_STORAGE_KEY')],
  ['홈 화면 기존 문장 프롬프트 제거', !page.includes('conversationPrompts')],
  ['홈 체크인 버튼 클릭 핸들러 제공', page.includes('handleStartCheckIn') && page.includes('sessionStorage.setItem')],
  ['체크인 버튼에서 채팅 화면으로 템플릿 전달', page.includes("router.push(`/chat?checkInTemplate=${template.type}`)")],
  ['기존 체크인 템플릿 4종 유지', templateTitles.every((title) => checkInTemplates.includes(title))],
  ['체크인 템플릿 type 4종 유지', templateTypes.every((type) => checkInTemplates.includes(type))],
  ['채팅 화면이 전달받은 체크인 템플릿으로 모달 자동 오픈', chatPage.includes('findCheckInTemplate') && chatPage.includes("setModalMode('CHECK_IN_WIZARD')")],
  ['모바일 앱형 하단 네비게이션 제공', page.includes('mobile-bottom-nav') && page.includes('모바일 주요 메뉴')],
  ['모바일 체크인 카드 스타일 제공', styles.includes('.checkin-prompt-card') && styles.includes('.mobile-bottom-nav')],
  ['모바일 상단 네비게이션 앱바 스타일 제공', styles.includes('position: sticky') && styles.includes('.top-nav .profile-button')],
]

const failed = checks.filter(([, passed]) => !passed)

if (failed.length > 0) {
  console.error('mobile check-in home smoke checks failed')
  for (const [name] of failed) {
    console.error(`- ${name}`)
  }
  process.exit(1)
}

console.log('mobile check-in home smoke checks passed')
