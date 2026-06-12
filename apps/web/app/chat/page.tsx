'use client'

import {
  ArrowLeft,
  ChevronLeft,
  ExternalLink,
  FileText,
  HeartHandshake,
  Home,
  Loader2,
  MessageCircle,
  Music,
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
  AiChatReport,
  AiChatReportReadiness,
  CheckInTemplateType,
  TodayAiChatRoom,
  createAiChatReport,
  readTodayAiChatRoom,
  readAiChatReportReadiness,
  sendAiChatMessage,
  startCheckInAiChatSegment,
  startDirectAiChatSegment,
} from '@/lib/ai-chat-api'
import { CHECK_IN_TEMPLATES, PENDING_CHECK_IN_TEMPLATE_STORAGE_KEY, findCheckInTemplate } from '@/lib/check-in-templates'
import type { CheckInOption, CheckInTemplateDefinition } from '@/lib/check-in-templates'

type ModalMode = 'NONE' | 'EXISTING_CONVERSATION' | 'START_SELECTOR' | 'CHECK_IN_WIZARD'

export default function AiChatPage() {
  const router = useRouter()
  const [room, setRoom] = useState<TodayAiChatRoom | null>(null)
  const [activeSegmentId, setActiveSegmentId] = useState<number | null>(null)
  const [message, setMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [crisisGuideMessage, setCrisisGuideMessage] = useState('')
  const [modalMode, setModalMode] = useState<ModalMode>('NONE')
  const [selectedTemplate, setSelectedTemplate] = useState<CheckInTemplateDefinition | null>(null)
  const [reportReadiness, setReportReadiness] = useState<AiChatReportReadiness | null>(null)
  const [todayReport, setTodayReport] = useState<AiChatReport | null>(null)
  const [isShortReportGuideOpen, setIsShortReportGuideOpen] = useState(false)
  const [isReportModalOpen, setIsReportModalOpen] = useState(false)
  const [optimisticMessages, setOptimisticMessages] = useState<AiChatMessage[]>([])
  const [isAssistantTyping, setIsAssistantTyping] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [isSending, setIsSending] = useState(false)
  const [isStarting, setIsStarting] = useState(false)
  const [isReportLoading, setIsReportLoading] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement | null>(null)
  const pendingCheckInTemplateRef = useRef<CheckInTemplateDefinition | null | undefined>(undefined)

  useEffect(() => {
    const readPendingCheckInTemplate = () => {
      if (pendingCheckInTemplateRef.current !== undefined) {
        return pendingCheckInTemplateRef.current
      }

      const checkInTemplateParam = new URLSearchParams(window.location.search).get('checkInTemplate')
      const storedTemplateType = sessionStorage.getItem(PENDING_CHECK_IN_TEMPLATE_STORAGE_KEY)
      sessionStorage.removeItem(PENDING_CHECK_IN_TEMPLATE_STORAGE_KEY)
      pendingCheckInTemplateRef.current = findCheckInTemplate(checkInTemplateParam) ?? findCheckInTemplate(storedTemplateType)
      return pendingCheckInTemplateRef.current
    }

    readTodayAiChatRoom()
      .then((todayRoom) => {
        const pendingTemplate = readPendingCheckInTemplate()
        setRoom(todayRoom)
        setActiveSegmentId(todayRoom.activeSegmentId ?? null)
        if (pendingTemplate) {
          setSelectedTemplate(pendingTemplate)
          setModalMode('CHECK_IN_WIZARD')
          window.history.replaceState(null, '', '/chat')
          return
        }

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

  const handleFinishTodayConversation = async () => {
    if (isReportLoading || isSending || isAssistantTyping) {
      return
    }

    setIsReportLoading(true)
    setErrorMessage('')
    try {
      const readiness = await readAiChatReportReadiness()
      setReportReadiness(readiness)
      if (!readiness.ready) {
        setIsShortReportGuideOpen(true)
        return
      }

      await handleCreateReport(false)
    } catch (error) {
      setErrorMessage(error instanceof LoginApiError ? error.message : '오늘 마음 리포트를 준비하지 못했습니다.')
    } finally {
      setIsReportLoading(false)
    }
  }

  const handleCreateReport = async (forceCreate: boolean) => {
    setIsReportLoading(true)
    setErrorMessage('')
    try {
      const report = await createAiChatReport(forceCreate, buildClientRequestId('report'))
      setTodayReport(report)
      setIsShortReportGuideOpen(false)
      setIsReportModalOpen(true)
    } catch (error) {
      setErrorMessage(error instanceof LoginApiError ? error.message : '오늘 마음 리포트를 만들지 못했습니다.')
    } finally {
      setIsReportLoading(false)
    }
  }

  const isConversationBusy = isSending || isAssistantTyping

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
              <button className="chat-action-button is-primary-action" type="button" onClick={handleContinueTodayConversation}>
                <span className="chat-action-icon">
                  <MessageCircle size={19} aria-hidden="true" />
                </span>
                <span className="chat-action-copy">
                  <strong>오늘 대화 이어가기</strong>
                  <small>방금 흐름 그대로 계속</small>
                </span>
              </button>
              <button className="chat-action-button is-secondary-action" type="button" onClick={handleOpenStartSelector}>
                <span className="chat-action-icon">
                  <Sparkles size={19} aria-hidden="true" />
                </span>
                <span className="chat-action-copy">
                  <strong>새 주제로 시작</strong>
                  <small>체크인 또는 새 구간</small>
                </span>
              </button>
              <button
                className="chat-action-button is-report-action"
                type="button"
                onClick={handleFinishTodayConversation}
                disabled={isReportLoading || isConversationBusy}
                aria-label={isConversationBusy ? '마음이 답변 완료 후 오늘 대화 마무리' : '오늘 대화 마무리'}
                title={isConversationBusy ? '마음이 답변이 끝난 뒤 마무리할 수 있어요.' : undefined}
              >
                <span className="chat-action-icon">
                  {isReportLoading || isConversationBusy ? <Loader2 size={19} aria-hidden="true" /> : <FileText size={19} aria-hidden="true" />}
                </span>
                <span className="chat-action-copy">
                  <strong>오늘 대화 마무리</strong>
                  <small>리포트 만들기</small>
                </span>
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

      {isShortReportGuideOpen && reportReadiness && (
        <ShortReportGuideModal
          readiness={reportReadiness}
          isSubmitting={isReportLoading}
          onClose={() => setIsShortReportGuideOpen(false)}
          onContinue={() => setIsShortReportGuideOpen(false)}
          onCreateReport={() => handleCreateReport(true)}
        />
      )}

      {isReportModalOpen && todayReport && (
        <AiChatReportModal report={todayReport} onClose={() => setIsReportModalOpen(false)} onGoHome={() => router.push('/')} />
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

function ShortReportGuideModal({
  readiness,
  isSubmitting,
  onClose,
  onContinue,
  onCreateReport,
}: {
  readiness: AiChatReportReadiness
  isSubmitting: boolean
  onClose: () => void
  onContinue: () => void
  onCreateReport: () => void
}) {
  const requiredUserMessageCount = readiness.requiredUserMessageCount ?? 10
  const requiredUserTextLength = readiness.requiredUserTextLength ?? 80
  const messageCountMet = readiness.userMessageCount >= requiredUserMessageCount
  const textLengthMet = readiness.userTextLength >= requiredUserTextLength
  const missingLabels = [
    !messageCountMet ? `유저 메시지 ${Math.max(0, requiredUserMessageCount - readiness.userMessageCount)}개 더 필요` : null,
    !textLengthMet ? `글자 수 ${Math.max(0, requiredUserTextLength - readiness.userTextLength)}자 더 필요` : null,
  ].filter((label): label is string => Boolean(label))

  return (
    <div className="modal-backdrop stacked" role="presentation" onMouseDown={onClose}>
      <section
        className="auth-modal report-modal report-guide-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="short-report-guide-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button className="icon-button" type="button" aria-label="짧은 대화 안내 닫기" onClick={onClose}>
          <X size={20} aria-hidden="true" />
        </button>
        <p className="eyebrow">리포트 기준 안내</p>
        <h2 id="short-report-guide-title">아직 채워지지 않은 기준이 있습니다</h2>
        <p className="modal-description">
          {readiness.guideMessage ??
            '지금 리포트를 만들면, 충분히 확인된 내용만 바탕으로 간단히 정리됩니다.'}
        </p>
        {missingLabels.length > 0 && <p className="report-missing-summary">미충족 항목: {missingLabels.join(', ')}</p>}
        <div className="report-readiness-box" aria-label="현재 대화 기준">
          <span className={messageCountMet ? 'is-met' : 'is-missing'}>
            <b>{messageCountMet ? '충족' : '미충족'}</b>
            유저 메시지 {readiness.userMessageCount}/{requiredUserMessageCount}개
          </span>
          <span className={textLengthMet ? 'is-met' : 'is-missing'}>
            <b>{textLengthMet ? '충족' : '미충족'}</b>
            글자 수 {readiness.userTextLength}/{requiredUserTextLength}자
          </span>
        </div>
        <div className="modal-actions split">
          <button className="soft-button" type="button" onClick={onContinue} disabled={isSubmitting}>
            대화 더 이어가기
          </button>
          <button className="primary-button report-confirm-button" type="button" onClick={onCreateReport} disabled={isSubmitting}>
            {isSubmitting ? <Loader2 size={18} aria-hidden="true" /> : <FileText size={18} aria-hidden="true" />}
            그래도 리포트 만들기
          </button>
        </div>
      </section>
    </div>
  )
}

function AiChatReportModal({ report, onClose, onGoHome }: { report: AiChatReport; onClose: () => void; onGoHome: () => void }) {
  return (
    <div className="modal-backdrop stacked" role="presentation" onMouseDown={onClose}>
      <section
        className="auth-modal report-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="ai-chat-report-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button className="icon-button" type="button" aria-label="마음 리포트 닫기" onClick={onClose}>
          <X size={20} aria-hidden="true" />
        </button>

        <div className="report-hero">
          <div className="report-hero-icon">
            <FileText size={24} aria-hidden="true" />
          </div>
          <div>
            <p className="eyebrow">{report.reportType === 'FULL' ? '대화 기반 리포트' : '짧은 대화 기반 리포트'}</p>
            <h2 id="ai-chat-report-title">오늘 마음 리포트</h2>
            <div className="report-meta-row">
              <span>자동 저장됨</span>
              <span>{report.conversationDate}</span>
            </div>
          </div>
        </div>

        <section className="report-highlight-card" aria-label="오늘 마음 리포트 핵심">
          <div>
            <span>대표 감정</span>
            <strong>{report.primaryEmotion}</strong>
          </div>
          <div>
            <span>감정 강도</span>
            <strong>{report.emotionIntensity ? `${report.emotionIntensity}/5` : '판단 유보'}</strong>
          </div>
          <p>{report.todaySentence}</p>
        </section>

        <div className="report-section-grid">
          <ReportSection title="오늘의 대화 요약" value={report.summary} />
          <ReportSection title="주요 원인" value={report.mainCause} />
          <ReportSection title="마음 흐름" value={report.emotionalFlow} />
        </div>

        <div className="report-song-section">
          <div className="report-song-heading">
            <Music size={18} aria-hidden="true" />
            <strong>추천 노래</strong>
          </div>
          <div className="report-song-list">
            {report.songs.map((song) => (
              <a className="report-song-card" href={song.youtubeUrl} target="_blank" rel="noreferrer" key={`${song.artist}-${song.title}`}>
                <span>
                  <strong>{song.title}</strong>
                  <small>{song.artist}</small>
                </span>
                <p>{song.reason}</p>
                <em>
                  YouTube에서 듣기
                  <ExternalLink size={14} aria-hidden="true" />
                </em>
              </a>
            ))}
          </div>
        </div>

        <div className="report-footer-actions">
          <button className="primary-button report-home-button" type="button" onClick={onGoHome}>
            <Home size={18} aria-hidden="true" />
            홈으로 이동하기
          </button>
        </div>
      </section>
    </div>
  )
}

function ReportSection({ title, value }: { title: string; value: string }) {
  return (
    <section className="report-section">
      <h3>{title}</h3>
      <p>{value}</p>
    </section>
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
