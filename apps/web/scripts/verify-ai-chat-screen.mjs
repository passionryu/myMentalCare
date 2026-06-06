import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const root = resolve(process.cwd())
const page = readFileSync(resolve(root, 'app/page.tsx'), 'utf8')
const chatPage = readFileSync(resolve(root, 'app/chat/page.tsx'), 'utf8')
const chatApi = readFileSync(resolve(root, 'lib/ai-chat-api.ts'), 'utf8')

const checks = [
  ['메인 화면 AI 마음 대화 카드 진입 버튼', page.includes('오늘의 대화 시작') && page.includes("router.push('/chat')")],
  ['비로그인 상태 로그인 모달 표시', page.includes("setAuthMode('login')") && page.includes('handleOpenAiChat')],
  ['채팅 화면 경로 구현', chatPage.includes('마음이와 오늘의 대화')],
  ['오늘 대화방 조회 API 사용', chatApi.includes('/api/ai-chat/rooms/today')],
  ['메시지 전송 API 사용', chatApi.includes('/api/ai-chat/rooms/today/messages')],
  ['위기 안내 모달 구현', chatPage.includes('crisisGuideMessage') && chatPage.includes('안전 안내')],
  ['채팅 메시지 저장 응답 타입', chatApi.includes('TodayAiChatRoom') && chatApi.includes('AiChatMessage')],
]

const failed = checks.filter(([, passed]) => !passed)

for (const [name, passed] of checks) {
  console.log(`${passed ? 'PASS' : 'FAIL'} ${name}`)
}

if (failed.length > 0) {
  process.exit(1)
}
