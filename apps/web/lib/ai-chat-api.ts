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

export type AiChatReportReadiness = {
  ready: boolean
  reason: 'SUFFICIENT_CONVERSATION' | 'SHORT_CONVERSATION' | string
  userMessageCount: number
  userTextLength: number
  requiredUserMessageCount?: number
  requiredUserTextLength?: number
  unmetRequirements?: string[]
  guideMessage?: string | null
}

export type AiChatReport = {
  reportId: number
  roomId: number
  reportType: 'FULL' | 'SHORT'
  conversationDate: string
  summary: string
  primaryEmotion: string
  emotionIntensity?: number | null
  mainCause: string
  emotionalFlow: string
  todaySentence: string
  songs: AiChatReportSong[]
  saved: boolean
  createdAt?: string | null
}

export type AiChatReportSong = {
  title: string
  artist: string
  reason: string
  youtubeUrl: string
}

export type AiChatHistoryRoom = {
  roomId: number
  conversationDate: string
  status: string
  messageCount: number
  latestMessage?: string | null
  latestMessageAt?: string | null
}

export type AiChatHistoryRoomDetail = {
  roomId: number
  chatbotCode: string
  chatbotName: string
  conversationDate: string
  status: string
  messages: AiChatMessage[]
}

export type AiChatCheckInHistory = {
  checkInId: number
  roomId: number
  segmentId: number
  templateType: CheckInTemplateType
  summaryText: string
  answers: AiChatCheckInHistoryAnswer[]
  createdAt?: string | null
}

export type AiChatCheckInHistoryAnswer = {
  stepKey: string
  optionKey?: string | null
  label?: string | null
  value?: number | null
  freeText?: string | null
}

export type DeleteAiChatHistoryTargetType = 'CHAT_ROOM' | 'REPORT' | 'CHECK_IN'

export type DeleteAiChatHistoryResponse = {
  deletedCount: number
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

export async function readAiChatHistoryRooms(): Promise<AiChatHistoryRoom[]> {
  const response = await requestWithAuth('/api/ai-chat/rooms')
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '채팅 이력을 불러오지 못했습니다.')
  }

  return body as AiChatHistoryRoom[]
}

export async function readAiChatHistoryRoom(roomId: number): Promise<AiChatHistoryRoomDetail> {
  const response = await requestWithAuth(`/api/ai-chat/rooms/${roomId}`)
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '채팅 상세 이력을 불러오지 못했습니다.')
  }

  return body as AiChatHistoryRoomDetail
}

export async function readAiChatReports(): Promise<AiChatReport[]> {
  const response = await requestWithAuth('/api/ai-chat/reports')
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '마음 리포트 보관함을 불러오지 못했습니다.')
  }

  return body as AiChatReport[]
}

export async function readAiChatCheckIns(): Promise<AiChatCheckInHistory[]> {
  const response = await requestWithAuth('/api/ai-chat/check-ins')
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '체크인 기록을 불러오지 못했습니다.')
  }

  return body as AiChatCheckInHistory[]
}

export async function readAiChatReport(reportId: number): Promise<AiChatReport> {
  const response = await requestWithAuth(`/api/ai-chat/reports/${reportId}`)
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '마음 리포트를 불러오지 못했습니다.')
  }

  return body as AiChatReport
}

export async function deleteAiChatHistory(
  targetType: DeleteAiChatHistoryTargetType,
  targetId: number,
): Promise<DeleteAiChatHistoryResponse> {
  const response = await requestWithAuth('/api/ai-chat/history/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ targetType, targetId }),
  })
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '선택한 이력을 삭제하지 못했습니다.')
  }

  return body as DeleteAiChatHistoryResponse
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

export async function readAiChatReportReadiness(): Promise<AiChatReportReadiness> {
  const response = await requestWithAuth('/api/ai-chat/rooms/today/report-readiness')
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '오늘 마음 리포트 가능 여부를 확인하지 못했습니다.')
  }

  return body as AiChatReportReadiness
}

export async function createAiChatReport(forceCreate: boolean, clientRequestId: string): Promise<AiChatReport> {
  const response = await requestWithAuth('/api/ai-chat/rooms/today/reports', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ forceCreate, clientRequestId }),
  })
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '오늘 마음 리포트를 만들지 못했습니다.')
  }

  return body as AiChatReport
}

export async function readLatestAiChatReport(): Promise<AiChatReport | null> {
  const response = await requestWithAuth('/api/ai-chat/rooms/today/reports/latest')
  if (response.status === 204) {
    return null
  }

  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '오늘 마음 리포트를 불러오지 못했습니다.')
  }

  return body as AiChatReport
}
