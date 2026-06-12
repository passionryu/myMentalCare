'use client'

import {
  ArrowLeft,
  ChevronLeft,
  HeartHandshake,
  Loader2,
  MessageCircle,
  Send,
  ShieldAlert,
  Sparkles,
  X,
} from 'lucide-react'
import { useRouter } from 'next/navigation'
import { FormEvent, useEffect, useMemo, useRef, useState } from 'react'
import { LoginApiError } from '@/lib/auth-api'
import {
  AiChatCheckInAnswer,
  AiChatMessage,
  CheckInTemplateType,
  TodayAiChatRoom,
  readTodayAiChatRoom,
  sendAiChatMessage,
  startCheckInAiChatSegment,
  startDirectAiChatSegment,
} from '@/lib/ai-chat-api'

type ModalMode = 'NONE' | 'EXISTING_CONVERSATION' | 'START_SELECTOR' | 'CHECK_IN_WIZARD'

type CheckInOption = {
  optionKey: string
  label: string
}

type CheckInStep =
  | {
      stepKey: string
      question: string
      type: 'choice'
      options: CheckInOption[]
    }
  | {
      stepKey: string
      question: string
      type: 'scale'
      min: number
      max: number
    }

type CheckInTemplateDefinition = {
  type: CheckInTemplateType
  title: string
  description: string
  steps: CheckInStep[]
}

const CHECK_IN_TEMPLATES: CheckInTemplateDefinition[] = [
  {
    type: 'BASIC_EMOTION',
    title: '기본 감정형',
    description: '감정, 강도, 이유를 1분 안에 선택',
    steps: [
      {
        stepKey: 'emotion',
        question: '지금 마음은 어떤가요?',
        type: 'choice',
        options: [
          { optionKey: 'OKAY', label: '괜찮음' },
          { optionKey: 'ANXIOUS', label: '불안함' },
          { optionKey: 'TIRED', label: '지침' },
          { optionKey: 'DEPRESSED', label: '우울함' },
          { optionKey: 'ANGRY', label: '화남' },
        ],
      },
      {
        stepKey: 'intensity',
        question: '그 정도는 어느 정도인가요?',
        type: 'scale',
        min: 1,
        max: 5,
      },
      {
        stepKey: 'reason',
        question: '무엇 때문인 것 같나요?',
        type: 'choice',
        options: [
          { optionKey: 'WORK_STUDY', label: '일/공부' },
          { optionKey: 'RELATIONSHIP', label: '인간관계' },
          { optionKey: 'FAMILY', label: '가족' },
          { optionKey: 'HEALTH', label: '건강' },
          { optionKey: 'MONEY', label: '돈' },
          { optionKey: 'OTHER', label: '기타' },
        ],
      },
    ],
  },
  {
    type: 'CONVERSATION_START',
    title: '대화 시작형',
    description: '걱정, 위로, 정리, 조언, 그냥 들어주기',
    steps: [
      {
        stepKey: 'topic',
        question: '지금 어떤 이야기를 하고 싶나요?',
        type: 'choice',
        options: [
          { optionKey: 'WORRY', label: '걱정' },
          { optionKey: 'COMFORT', label: '위로' },
          { optionKey: 'ORGANIZE', label: '정리' },
          { optionKey: 'ADVICE', label: '조언' },
          { optionKey: 'LISTEN', label: '그냥 들어주기' },
        ],
      },
      {
        stepKey: 'responseStyle',
        question: '마음이는 어떻게 반응하면 좋을까요?',
        type: 'choice',
        options: [
          { optionKey: 'CALM', label: '차분하게' },
          { optionKey: 'REALISTIC', label: '현실적으로' },
          { optionKey: 'WARM', label: '따뜻하게' },
          { optionKey: 'SHORT', label: '짧게' },
          { optionKey: 'OTHER', label: '기타' },
        ],
      },
    ],
  },
  {
    type: 'CONDITION',
    title: '컨디션 중심형',
    description: '몸과 마음의 에너지 상태로 시작',
    steps: [
      {
        stepKey: 'energy',
        question: '지금 몸과 마음의 에너지는 어떤가요?',
        type: 'choice',
        options: [
          { optionKey: 'ENOUGH', label: '충분함' },
          { optionKey: 'NORMAL', label: '보통' },
          { optionKey: 'LOW', label: '부족함' },
          { optionKey: 'EMPTY', label: '거의 없음' },
        ],
      },
      {
        stepKey: 'factor',
        question: '오늘 가장 크게 영향을 준 것은 무엇인가요?',
        type: 'choice',
        options: [
          { optionKey: 'SLEEP', label: '수면' },
          { optionKey: 'WORK_STUDY', label: '업무·학업' },
          { optionKey: 'PEOPLE', label: '사람' },
          { optionKey: 'HEALTH', label: '건강' },
          { optionKey: 'NOTHING_SPECIAL', label: '특별한 일 없음' },
          { optionKey: 'OTHER', label: '기타' },
        ],
      },
    ],
  },
  {
    type: 'DAY_REVIEW',
    title: '하루 회고형',
    description: '오늘 하루의 느낌과 마무리 방식 선택',
    steps: [
      {
        stepKey: 'day',
        question: '오늘 하루는 어땠나요?',
        type: 'choice',
        options: [
          { optionKey: 'OKAY', label: '괜찮았음' },
          { optionKey: 'AMBIGUOUS', label: '애매했음' },
          { optionKey: 'HARD', label: '힘들었음' },
          { optionKey: 'VERY_HARD', label: '많이 힘들었음' },
        ],
      },
      {
        stepKey: 'remainingEmotion',
        question: '오늘 가장 많이 남은 감정은 무엇인가요?',
        type: 'choice',
        options: [
          { optionKey: 'RELIEF', label: '안도' },
          { optionKey: 'ANXIETY', label: '불안' },
          { optionKey: 'FATIGUE', label: '피로' },
          { optionKey: 'REGRET', label: '후회' },
          { optionKey: 'ANGER', label: '분노' },
          { optionKey: 'LONELINESS', label: '외로움' },
          { optionKey: 'OTHER', label: '기타' },
        ],
      },
      {
        stepKey: 'closing',
        question: '오늘을 어떻게 마무리하고 싶나요?',
        type: 'choice',
        options: [
          { optionKey: 'ORGANIZE', label: '정리하기' },
          { optionKey: 'LET_GO', label: '내려놓기' },
          { optionKey: 'PREPARE_TOMORROW', label: '내일 준비하기' },
          { optionKey: 'REST', label: '그냥 쉬기' },
          { optionKey: 'OTHER', label: '기타' },
        ],
      },
    ],
  },
]

export default function AiChatPage() {
  const router = useRouter()
  const [room, setRoom] = useState<TodayAiChatRoom | null>(null)
  const [activeSegmentId, setActiveSegmentId] = useState<number | null>(null)
  const [message, setMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [crisisGuideMessage, setCrisisGuideMessage] = useState('')
  const [modalMode, setModalMode] = useState<ModalMode>('NONE')
  const [selectedTemplate, setSelectedTemplate] = useState<CheckInTemplateDefinition | null>(null)
  const [optimisticMessages, setOptimisticMessages] = useState<AiChatMessage[]>([])
  const [isAssistantTyping, setIsAssistantTyping] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [isSending, setIsSending] = useState(false)
  const [isStarting, setIsStarting] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    readTodayAiChatRoom()
      .then((todayRoom) => {
        setRoom(todayRoom)
        setActiveSegmentId(todayRoom.activeSegmentId ?? null)
        setModalMode(todayRoom.hasConversation ? 'EXISTING_CONVERSATION' : 'START_SELECTOR')
      })
      .catch((error) => {
        setErrorMessage(error instanceof LoginApiError ? error.message : '오늘의 마음 대화방을 불러오지 못했습니다.')
      })
      .finally(() => setIsLoading(false))
  }, [])

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [room?.messages.length, optimisticMessages.length, isAssistantTyping])

  const segmentById = useMemo(() => {
    return new Map(room?.segments.map((segment) => [segment.segmentId, segment]) ?? [])
  }, [room?.segments])

  const displayedMessages = useMemo(() => {
    return [...(room?.messages ?? []), ...optimisticMessages]
  }, [room?.messages, optimisticMessages])

  const handleContinueTodayConversation = () => {
    setActiveSegmentId(room?.activeSegmentId ?? null)
    setModalMode('NONE')
  }

  const handleOpenStartSelector = () => {
    setSelectedTemplate(null)
    setModalMode('START_SELECTOR')
  }

  const handleSelectTemplate = (template: CheckInTemplateDefinition) => {
    setSelectedTemplate(template)
    setModalMode('CHECK_IN_WIZARD')
  }

  const handleStartDirect = async () => {
    if (isStarting) {
      return
    }

    setIsStarting(true)
    setErrorMessage('')
    try {
      const response = await startDirectAiChatSegment(buildClientRequestId('direct'))
      setRoom(response.room)
      setActiveSegmentId(response.segment.segmentId)
      setModalMode('NONE')
      if (response.crisisDetected && response.crisisGuideMessage) {
        setCrisisGuideMessage(response.crisisGuideMessage)
      }
    } catch (error) {
      setErrorMessage(error instanceof LoginApiError ? error.message : '새 대화 주제를 시작하지 못했습니다.')
    } finally {
      setIsStarting(false)
    }
  }

  const handleSubmitCheckIn = async (templateType: CheckInTemplateType, answers: AiChatCheckInAnswer[]) => {
    if (isStarting) {
      return
    }

    setIsStarting(true)
    setErrorMessage('')
    try {
      const response = await startCheckInAiChatSegment(templateType, answers, buildClientRequestId('checkin'))
      setRoom(response.room)
      setActiveSegmentId(response.segment.segmentId)
      setModalMode('NONE')
      if (response.crisisDetected && response.crisisGuideMessage) {
        setCrisisGuideMessage(response.crisisGuideMessage)
      }
    } catch (error) {
      setErrorMessage(error instanceof LoginApiError ? error.message : '체크인으로 대화를 시작하지 못했습니다.')
    } finally {
      setIsStarting(false)
    }
  }

  const handleSendMessage = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const content = message.trim()
    if (!content || isSending) {
      return
    }

    setIsSending(true)
    setIsAssistantTyping(true)
    setErrorMessage('')
    setCrisisGuideMessage('')
    setMessage('')

    const currentSegmentId = activeSegmentId
    const optimisticMessageId = -Date.now()
    const lastMessageOrder = room?.messages.reduce((maxOrder, chatMessage) => Math.max(maxOrder, chatMessage.messageOrder), 0) ?? 0
    const optimisticMessage: AiChatMessage = {
      messageId: optimisticMessageId,
      segmentId: currentSegmentId,
      senderType: 'USER',
      content,
      messageOrder: lastMessageOrder + 1,
      isCrisisDetected: false,
      createdAt: new Date().toISOString(),
    }

    setOptimisticMessages((current) => [...current, optimisticMessage])

    try {
      const response = await sendAiChatMessage(content, currentSegmentId, buildClientRequestId('message'))
      setRoom(response.room)
      setActiveSegmentId(response.room.activeSegmentId ?? currentSegmentId)
      setOptimisticMessages([])
      setIsAssistantTyping(false)
      if (response.aiReplyFailed) {
        setErrorMessage(response.aiReplyErrorMessage ?? '마음이의 답변을 준비하지 못했습니다. 잠시 후 다시 시도해주세요.')
      }
      if (response.crisisDetected && response.crisisGuideMessage) {
        setCrisisGuideMessage(response.crisisGuideMessage)
      }
    } catch (error) {
      setOptimisticMessages((current) => current.filter((chatMessage) => chatMessage.messageId !== optimisticMessageId))
      setMessage(content)
      setErrorMessage(error instanceof LoginApiError ? error.message : '마음 대화 메시지를 전송하지 못했습니다.')
    } finally {
      setIsAssistantTyping(false)
      setIsSending(false)
    }
  }

  return (
    <main className="chat-page-shell" suppressHydrationWarning>
      <section className="chat-layout" aria-labelledby="chat-page-title">
        <header className="chat-header">
          <button className="soft-button" type="button" onClick={() => router.push('/')}>
            <ArrowLeft size={18} aria-hidden="true" />
            홈으로
          </button>
          <div>
            <p className="eyebrow">AI 마음 대화</p>
            <h1 id="chat-page-title">마음이와 오늘의 대화</h1>
            <p>{room ? `${room.conversationDate} 하루 동안 이어지는 대화방입니다.` : '오늘의 대화방을 준비하고 있습니다.'}</p>
          </div>
        </header>

        <section className="chat-panel" aria-label="오늘의 AI 마음 대화">
          <div className="chat-bot-card">
            <span className="feature-icon">
              <HeartHandshake size={22} aria-hidden="true" />
            </span>
            <div>
              <strong>{room?.chatbotName ?? '마음이'}</strong>
              <p>진단이나 치료가 아니라, 오늘의 마음을 정리하도록 돕는 공감형 대화입니다.</p>
            </div>
          </div>

          {room?.hasConversation && (
            <div className="chat-action-strip" aria-label="오늘 대화 진입 선택">
              <button className="soft-button" type="button" onClick={handleContinueTodayConversation}>
                <MessageCircle size={17} aria-hidden="true" />
                오늘 대화 이어가기
              </button>
              <button className="ghost-button" type="button" onClick={handleOpenStartSelector}>
                <Sparkles size={17} aria-hidden="true" />
                새 주제로 시작
              </button>
            </div>
          )}

          <div className="chat-messages" aria-live="polite">
            {isLoading && (
              <div className="chat-loading">
                <Loader2 size={20} aria-hidden="true" />
                오늘의 대화방을 불러오고 있습니다.
              </div>
            )}
            {!isLoading && displayedMessages.length === 0 && (
              <div className="empty-chat">
                <p>바로 상담을 시작하거나 체크인으로 지금 상태를 짧게 알려주세요.</p>
              </div>
            )}
            {room && renderChatTimeline(displayedMessages, segmentById)}
            {isAssistantTyping && <AssistantTypingBubble />}
            <div ref={messagesEndRef} />
          </div>

          {errorMessage && <p className="form-message">{errorMessage}</p>}

          <form className="chat-input-row" onSubmit={handleSendMessage}>
            <label className="sr-only" htmlFor="ai-chat-message">
              마음 대화 메시지
            </label>
            <textarea
              id="ai-chat-message"
              suppressHydrationWarning
              value={message}
              maxLength={1000}
              rows={2}
              placeholder="지금 마음에 떠오르는 말을 적어보세요."
              onChange={(event) => setMessage(event.target.value)}
            />
            <button className="primary-button" type="submit" disabled={isSending || !message.trim()}>
              {isSending ? <Loader2 size={18} aria-hidden="true" /> : <Send size={18} aria-hidden="true" />}
              보내기
            </button>
          </form>
        </section>
      </section>

      {modalMode !== 'NONE' && (
        <CheckInEntryModal
          mode={modalMode}
          selectedTemplate={selectedTemplate}
          roomHasConversation={room?.hasConversation ?? false}
          isSubmitting={isStarting}
          errorMessage={errorMessage}
          onClose={room?.hasConversation ? handleContinueTodayConversation : undefined}
          onContinue={handleContinueTodayConversation}
          onStartNewTopic={handleOpenStartSelector}
          onStartDirect={handleStartDirect}
          onSelectTemplate={handleSelectTemplate}
          onBackToSelector={handleOpenStartSelector}
          onSubmitCheckIn={handleSubmitCheckIn}
        />
      )}

      {crisisGuideMessage && (
        <div className="modal-backdrop stacked" role="presentation" onMouseDown={() => setCrisisGuideMessage('')}>
          <section
            className="auth-modal guide-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="crisis-guide-title"
            onMouseDown={(event) => event.stopPropagation()}
          >
            <button className="icon-button" type="button" aria-label="안전 안내 닫기" onClick={() => setCrisisGuideMessage('')}>
              <X size={20} aria-hidden="true" />
            </button>
            <p className="eyebrow">안전 안내</p>
            <h2 id="crisis-guide-title">지금은 도움을 요청해도 괜찮습니다</h2>
            <div className="crisis-guide">
              <ShieldAlert size={28} aria-hidden="true" />
              <p>{crisisGuideMessage}</p>
            </div>
            <div className="modal-actions">
              <button className="primary-button" type="button" onClick={() => setCrisisGuideMessage('')}>
                확인
              </button>
            </div>
          </section>
        </div>
      )}
    </main>
  )
}

function CheckInEntryModal({
  mode,
  selectedTemplate,
  roomHasConversation,
  isSubmitting,
  errorMessage,
  onClose,
  onContinue,
  onStartNewTopic,
  onStartDirect,
  onSelectTemplate,
  onBackToSelector,
  onSubmitCheckIn,
}: {
  mode: ModalMode
  selectedTemplate: CheckInTemplateDefinition | null
  roomHasConversation: boolean
  isSubmitting: boolean
  errorMessage: string
  onClose?: () => void
  onContinue: () => void
  onStartNewTopic: () => void
  onStartDirect: () => void
  onSelectTemplate: (template: CheckInTemplateDefinition) => void
  onBackToSelector: () => void
  onSubmitCheckIn: (templateType: CheckInTemplateType, answers: AiChatCheckInAnswer[]) => void
}) {
  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section
        className="auth-modal checkin-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="checkin-modal-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        {onClose && (
          <button className="icon-button" type="button" aria-label="체크인 닫기" onClick={onClose}>
            <X size={20} aria-hidden="true" />
          </button>
        )}

        {mode === 'EXISTING_CONVERSATION' && (
          <ExistingConversationChoice onContinue={onContinue} onStartNewTopic={onStartNewTopic} />
        )}

        {mode === 'START_SELECTOR' && (
          <StartModeSelector
            roomHasConversation={roomHasConversation}
            isSubmitting={isSubmitting}
            errorMessage={errorMessage}
            onStartDirect={onStartDirect}
            onSelectTemplate={onSelectTemplate}
          />
        )}

        {mode === 'CHECK_IN_WIZARD' && selectedTemplate && (
          <CheckInWizard
            template={selectedTemplate}
            isSubmitting={isSubmitting}
            errorMessage={errorMessage}
            onBack={onBackToSelector}
            onSubmit={onSubmitCheckIn}
          />
        )}
      </section>
    </div>
  )
}

function ExistingConversationChoice({ onContinue, onStartNewTopic }: { onContinue: () => void; onStartNewTopic: () => void }) {
  return (
    <>
      <p className="eyebrow">오늘의 대화</p>
      <h2 id="checkin-modal-title">오늘 나눈 대화가 있습니다</h2>
      <p className="modal-description">하루 하나의 대화방 안에서 이어가거나, 같은 방 안에 새 주제 구간을 만들 수 있습니다.</p>
      <div className="conversation-choice-grid">
        <button className="start-choice-card" type="button" onClick={onContinue}>
          <MessageCircle size={22} aria-hidden="true" />
          <span>
            <strong>오늘 대화 이어가기</strong>
            <small>마지막 구간에서 자연스럽게 계속합니다.</small>
          </span>
        </button>
        <button className="start-choice-card" type="button" onClick={onStartNewTopic}>
          <Sparkles size={22} aria-hidden="true" />
          <span>
            <strong>새 주제로 시작</strong>
            <small>오늘 방 안에 새 구간을 추가합니다.</small>
          </span>
        </button>
      </div>
    </>
  )
}

function StartModeSelector({
  roomHasConversation,
  isSubmitting,
  errorMessage,
  onStartDirect,
  onSelectTemplate,
}: {
  roomHasConversation: boolean
  isSubmitting: boolean
  errorMessage: string
  onStartDirect: () => void
  onSelectTemplate: (template: CheckInTemplateDefinition) => void
}) {
  return (
    <>
      <p className="eyebrow">{roomHasConversation ? '새 주제 시작' : '대화 시작'}</p>
      <h2 id="checkin-modal-title">어떻게 시작할까요?</h2>
      <p className="modal-description">체크인을 건너뛰고 바로 말해도 되고, 지금 상태를 짧게 고른 뒤 시작해도 됩니다.</p>

      <button className="direct-start-card" type="button" onClick={onStartDirect} disabled={isSubmitting}>
        {isSubmitting ? <Loader2 size={18} aria-hidden="true" /> : <MessageCircle size={18} aria-hidden="true" />}
        바로 상담 시작하기
      </button>

      <div className="checkin-start-heading">
        <p>체크인으로 시작하기</p>
      </div>

      <div className="checkin-template-grid">
        {CHECK_IN_TEMPLATES.map((template) => (
          <button className="checkin-template-card" type="button" key={template.type} onClick={() => onSelectTemplate(template)}>
            <strong>{template.title}</strong>
            <span>{template.description}</span>
          </button>
        ))}
      </div>

      {errorMessage && <p className="form-message">{errorMessage}</p>}
    </>
  )
}

function CheckInWizard({
  template,
  isSubmitting,
  errorMessage,
  onBack,
  onSubmit,
}: {
  template: CheckInTemplateDefinition
  isSubmitting: boolean
  errorMessage: string
  onBack: () => void
  onSubmit: (templateType: CheckInTemplateType, answers: AiChatCheckInAnswer[]) => void
}) {
  const [stepIndex, setStepIndex] = useState(0)
  const [answers, setAnswers] = useState<Record<string, AiChatCheckInAnswer>>({})
  const step = template.steps[stepIndex]
  const currentAnswer = answers[step.stepKey]
  const isLastStep = stepIndex === template.steps.length - 1
  const canContinue = isAnswerComplete(step, currentAnswer)

  const moveNextWithAnswers = (nextAnswers: Record<string, AiChatCheckInAnswer>) => {
    if (isLastStep) {
      onSubmit(
        template.type,
        template.steps.map((templateStep) => nextAnswers[templateStep.stepKey]).filter(Boolean),
      )
      return
    }

    setStepIndex((current) => current + 1)
  }

  const handleSelectChoice = (option: CheckInOption) => {
    const nextAnswers = {
      ...answers,
      [step.stepKey]: {
        stepKey: step.stepKey,
        optionKey: option.optionKey,
        label: option.label,
        freeText: option.optionKey === 'OTHER' ? (answers[step.stepKey]?.freeText ?? '') : undefined,
      },
    }

    setAnswers(nextAnswers)

    if (option.optionKey !== 'OTHER') {
      window.setTimeout(() => moveNextWithAnswers(nextAnswers), 80)
    }
  }

  const handleSelectScale = (value: number) => {
    const nextAnswers = {
      ...answers,
      [step.stepKey]: {
        stepKey: step.stepKey,
        value,
        label: `${value}`,
      },
    }

    setAnswers(nextAnswers)
    window.setTimeout(() => moveNextWithAnswers(nextAnswers), 80)
  }

  const handleOtherInput = (freeText: string) => {
    setAnswers((previous) => ({
      ...previous,
      [step.stepKey]: {
        stepKey: step.stepKey,
        optionKey: 'OTHER',
        label: '기타',
        freeText,
      },
    }))
  }

  const handleNext = () => {
    if (!canContinue) {
      return
    }

    if (isLastStep) {
      onSubmit(
        template.type,
        template.steps.map((templateStep) => answers[templateStep.stepKey]).filter(Boolean),
      )
      return
    }

    setStepIndex((current) => current + 1)
  }

  return (
    <>
      <button className="wizard-back-button" type="button" onClick={stepIndex === 0 ? onBack : () => setStepIndex((current) => current - 1)}>
        <ChevronLeft size={18} aria-hidden="true" />
        이전
      </button>
      <p className="eyebrow">{template.title}</p>
      <h2 id="checkin-modal-title">{step.question}</h2>
      <div className="wizard-progress" aria-label="체크인 진행 단계">
        {template.steps.map((templateStep, index) => (
          <span className={index <= stepIndex ? 'is-active' : ''} key={templateStep.stepKey} />
        ))}
      </div>

      {step.type === 'choice' && (
        <div className="wizard-option-grid">
          {step.options.map((option) => (
            <button
              className={`wizard-option ${currentAnswer?.optionKey === option.optionKey ? 'is-selected' : ''}`}
              type="button"
              key={option.optionKey}
              onClick={() => handleSelectChoice(option)}
            >
              {option.label}
            </button>
          ))}
        </div>
      )}

      {step.type === 'scale' && (
        <div className="intensity-row">
          {Array.from({ length: step.max - step.min + 1 }, (_, index) => step.min + index).map((value) => (
            <button
              className={`scale-button ${currentAnswer?.value === value ? 'is-selected' : ''}`}
              type="button"
              key={value}
              onClick={() => handleSelectScale(value)}
            >
              {value}
            </button>
          ))}
        </div>
      )}

      {currentAnswer?.optionKey === 'OTHER' && (
        <label className="other-input">
          직접 입력
          <input
            value={currentAnswer.freeText ?? ''}
            maxLength={200}
            placeholder="짧게 적어주세요."
            onChange={(event) => handleOtherInput(event.target.value)}
          />
        </label>
      )}

      {errorMessage && <p className="form-message">{errorMessage}</p>}

      <div className="wizard-actions">
        <button className="primary-button" type="button" onClick={handleNext} disabled={!canContinue || isSubmitting}>
          {isSubmitting ? <Loader2 size={18} aria-hidden="true" /> : null}
          {currentAnswer?.optionKey === 'OTHER' ? (isLastStep ? '입력 내용으로 체크인 완료' : '입력 내용으로 다음') : isLastStep ? '체크인 완료' : '다음'}
        </button>
      </div>
    </>
  )
}

function renderChatTimeline(messages: AiChatMessage[], segmentById: Map<number, TodayAiChatRoom['segments'][number]>) {
  const renderedSegmentIds = new Set<string>()

  return messages.map((chatMessage) => {
    const segmentKey = chatMessage.segmentId == null ? 'legacy' : `${chatMessage.segmentId}`
    const shouldRenderSegment = !renderedSegmentIds.has(segmentKey)
    renderedSegmentIds.add(segmentKey)
    const segment = chatMessage.segmentId == null ? null : segmentById.get(chatMessage.segmentId)

    return (
      <div className="chat-timeline-item" key={chatMessage.messageId}>
        {shouldRenderSegment && <ChatSegmentDivider title={segment?.title ?? '이전 대화'} summary={segment?.checkIn?.summaryText} />}
        <ChatBubble message={chatMessage} />
      </div>
    )
  })
}

function ChatSegmentDivider({ title, summary }: { title: string; summary?: string | null }) {
  return (
    <div className="chat-segment-divider">
      <span>{title}</span>
      {summary && <small>{summary}</small>}
    </div>
  )
}

function ChatBubble({ message }: { message: AiChatMessage }) {
  const isUser = message.senderType === 'USER'
  const createdTime = formatChatMessageTime(message.createdAt)

  return (
    <article className={`chat-message-row ${isUser ? 'is-user' : 'is-assistant'} ${message.isCrisisDetected ? 'is-crisis' : ''}`}>
      {!isUser && <img className="chat-avatar" src="/maeumi-avatar.svg" alt="마음이 프로필" />}
      <div className="chat-message-body">
        {isUser && createdTime && (
          <time suppressHydrationWarning dateTime={message.createdAt ?? undefined}>
            {createdTime}
          </time>
        )}
        <div className="chat-bubble">
          <p>{message.content}</p>
        </div>
        {!isUser && createdTime && (
          <time suppressHydrationWarning dateTime={message.createdAt ?? undefined}>
            {createdTime}
          </time>
        )}
      </div>
    </article>
  )
}

function AssistantTypingBubble() {
  return (
    <div className="chat-timeline-item is-typing">
      <article className="chat-message-row is-assistant is-typing">
        <img className="chat-avatar" src="/maeumi-avatar.svg" alt="마음이 프로필" />
        <div className="chat-message-body">
          <div className="chat-bubble chat-typing-bubble" role="status" aria-live="polite">
            <span>마음이가 답변을 준비 중이에요</span>
            <span className="typing-dots" aria-hidden="true">
              <i />
              <i />
              <i />
            </span>
          </div>
        </div>
      </article>
    </div>
  )
}

function isAnswerComplete(step: CheckInStep, answer?: AiChatCheckInAnswer) {
  if (!answer) {
    return false
  }

  if (step.type === 'scale') {
    return typeof answer.value === 'number'
  }

  if (answer.optionKey === 'OTHER') {
    return Boolean(answer.freeText?.trim())
  }

  return Boolean(answer.optionKey)
}

function buildClientRequestId(prefix: string) {
  const randomValue = typeof crypto !== 'undefined' && 'randomUUID' in crypto ? crypto.randomUUID() : `${Date.now()}-${Math.random()}`
  return `${prefix}-${randomValue}`.slice(0, 80)
}

function formatChatMessageTime(createdAt?: string | null) {
  if (!createdAt) {
    return ''
  }

  return new Intl.DateTimeFormat('en-US', {
    hour: 'numeric',
    minute: '2-digit',
    hour12: false,
    timeZone: 'Asia/Seoul',
  }).format(new Date(createdAt))
}
