import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const root = resolve(process.cwd())
const page = readFileSync(resolve(root, 'app/page.tsx'), 'utf8')
const chatPage = readFileSync(resolve(root, 'app/chat/page.tsx'), 'utf8')
const chatApi = readFileSync(resolve(root, 'lib/ai-chat-api.ts'), 'utf8')
const checkInTemplates = readFileSync(resolve(root, 'lib/check-in-templates.ts'), 'utf8')

const checks = [
  ['메인 화면 AI 마음 대화 카드 진입 버튼', page.includes('AI 마음대화 시작') && page.includes("router.push('/chat')")],
  ['비로그인 상태 로그인 모달 표시', page.includes("setAuthMode('login')") && page.includes('handleOpenAiChat')],
  ['채팅 화면 경로 구현', chatPage.includes('마음이와 오늘의 대화')],
  ['오늘 대화방 조회 API 사용', chatApi.includes('/api/ai-chat/rooms/today')],
  ['메시지 전송 API 사용', chatApi.includes('/api/ai-chat/rooms/today/messages')],
  ['위기 안내 모달 구현', chatPage.includes('crisisGuideMessage') && chatPage.includes('안전 안내')],
  ['채팅 메시지 저장 응답 타입', chatApi.includes('TodayAiChatRoom') && chatApi.includes('AiChatMessage')],
  ['체크인 템플릿 공유 모듈 사용', chatPage.includes("from '@/lib/check-in-templates'") && checkInTemplates.includes('CHECK_IN_TEMPLATES')],
  ['홈 체크인 진입용 pending template 키 제공', checkInTemplates.includes('PENDING_CHECK_IN_TEMPLATE_STORAGE_KEY')],
  ['pending template 조회 함수 제공', checkInTemplates.includes('findCheckInTemplate')],
  ['채팅 화면 체크인 템플릿 쿼리 처리', chatPage.includes("new URLSearchParams(window.location.search).get('checkInTemplate')")],
  ['홈 진입 체크인 모달 자동 표시', chatPage.includes("setModalMode('CHECK_IN_WIZARD')") && chatPage.includes('setSelectedTemplate(pendingTemplate)')],
  ['체크인 템플릿 4종 제공', ['기본 감정형', '대화 시작형', '컨디션 중심형', '하루 회고형'].every((title) => checkInTemplates.includes(title))],
]

const failed = checks.filter(([, passed]) => !passed)

for (const [name, passed] of checks) {
  console.log(`${passed ? 'PASS' : 'FAIL'} ${name}`)
}

if (failed.length > 0) {
  process.exit(1)
}
