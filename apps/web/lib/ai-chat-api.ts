import { LoginApiError, requestWithAuth } from '@/lib/auth-api'

export type AiChatMessage = {
  messageId: number
  senderType: 'USER' | 'ASSISTANT' | 'SYSTEM'
  content: string
  messageOrder: number
  isCrisisDetected: boolean
  createdAt?: string | null
}

export type TodayAiChatRoom = {
  roomId: number
  chatbotCode: string
  chatbotName: string
  conversationDate: string
  status: string
  messages: AiChatMessage[]
}

export type SendAiChatMessageResponse = {
  room: TodayAiChatRoom
  userMessage: AiChatMessage
  assistantMessage: AiChatMessage
  crisisDetected: boolean
  crisisGuideMessage?: string | null
}

async function readJson(response: Response) {
  return response.json().catch(() => null)
}

export async function readTodayAiChatRoom(): Promise<TodayAiChatRoom> {
  const response = await requestWithAuth('/api/ai-chat/rooms/today')
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '오늘의 마음 대화방을 불러오지 못했습니다.')
  }

  return body as TodayAiChatRoom
}

export async function sendAiChatMessage(content: string): Promise<SendAiChatMessageResponse> {
  const response = await requestWithAuth('/api/ai-chat/rooms/today/messages', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ content }),
  })
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '마음 대화 메시지를 전송하지 못했습니다.')
  }

  return body as SendAiChatMessageResponse
}
