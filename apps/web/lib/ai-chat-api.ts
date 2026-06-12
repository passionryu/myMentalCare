import { LoginApiError, requestWithAuth } from '@/lib/auth-api'

export type AiChatMessage = {
  messageId: number
  segmentId?: number | null
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
  hasConversation: boolean
  activeSegmentId?: number | null
  segments: AiChatSegment[]
  messages: AiChatMessage[]
}

export type AiChatSegment = {
  segmentId: number
  segmentOrder: number
  startType: StartType
  title: string
  startedAt?: string | null
  checkIn?: AiChatCheckInSummary | null
}

export type AiChatCheckInSummary = {
  checkInId: number
  templateType: CheckInTemplateType
  summaryText: string
}

export type SendAiChatMessageResponse = {
  room: TodayAiChatRoom
  userMessage: AiChatMessage
  assistantMessage: AiChatMessage
  crisisDetected: boolean
  crisisGuideMessage?: string | null
  aiReplyFailed: boolean
  aiReplyErrorMessage?: string | null
}

export type StartType = 'DIRECT' | CheckInTemplateType

export type CheckInTemplateType = 'BASIC_EMOTION' | 'CONVERSATION_START' | 'CONDITION' | 'DAY_REVIEW'

export type AiChatCheckInAnswer = {
  stepKey: string
  optionKey?: string
  label?: string
  value?: number
  freeText?: string
}

export type StartAiChatSegmentResponse = {
  room: TodayAiChatRoom
  segment: AiChatSegment
  checkIn?: AiChatCheckInSummary | null
  assistantMessage: AiChatMessage
  crisisDetected: boolean
  crisisGuideMessage?: string | null
  aiReplyFailed: boolean
  aiReplyErrorMessage?: string | null
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

export async function startDirectAiChatSegment(clientRequestId: string): Promise<StartAiChatSegmentResponse> {
  const response = await requestWithAuth('/api/ai-chat/rooms/today/segments', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ startType: 'DIRECT', clientRequestId }),
  })
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '새 대화 주제를 시작하지 못했습니다.')
  }

  return body as StartAiChatSegmentResponse
}

export async function startCheckInAiChatSegment(
  templateType: CheckInTemplateType,
  answers: AiChatCheckInAnswer[],
  clientRequestId: string,
): Promise<StartAiChatSegmentResponse> {
  const response = await requestWithAuth('/api/ai-chat/rooms/today/segments/check-in', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ templateType, answers, clientRequestId }),
  })
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '체크인으로 대화를 시작하지 못했습니다.')
  }

  return body as StartAiChatSegmentResponse
}

export async function sendAiChatMessage(
  content: string,
  segmentId?: number | null,
  clientRequestId?: string,
): Promise<SendAiChatMessageResponse> {
  const response = await requestWithAuth('/api/ai-chat/rooms/today/messages', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ content, segmentId, clientRequestId }),
  })
  const body = await readJson(response)

  if (!response.ok && body?.aiReplyFailed && body?.room) {
    return body as SendAiChatMessageResponse
  }

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '마음 대화 메시지를 전송하지 못했습니다.')
  }

  return body as SendAiChatMessageResponse
}
