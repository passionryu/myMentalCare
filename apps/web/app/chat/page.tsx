'use client'

import { ArrowLeft, HeartHandshake, Loader2, Send, ShieldAlert, X } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { FormEvent, useEffect, useRef, useState } from 'react'
import { LoginApiError } from '@/lib/auth-api'
import { AiChatMessage, TodayAiChatRoom, readTodayAiChatRoom, sendAiChatMessage } from '@/lib/ai-chat-api'

export default function AiChatPage() {
  const router = useRouter()
  const [room, setRoom] = useState<TodayAiChatRoom | null>(null)
  const [message, setMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [crisisGuideMessage, setCrisisGuideMessage] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isSending, setIsSending] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    readTodayAiChatRoom()
      .then(setRoom)
      .catch((error) => {
        setErrorMessage(error instanceof LoginApiError ? error.message : '오늘의 마음 대화방을 불러오지 못했습니다.')
      })
      .finally(() => setIsLoading(false))
  }, [])

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [room?.messages.length])

  const handleSendMessage = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const content = message.trim()
    if (!content || isSending) {
      return
    }

    setIsSending(true)
    setErrorMessage('')
    try {
      const response = await sendAiChatMessage(content)
      setRoom(response.room)
      setMessage('')
      if (response.aiReplyFailed) {
        setErrorMessage(response.aiReplyErrorMessage ?? '마음이의 답변을 준비하지 못했습니다. 잠시 후 다시 시도해주세요.')
      }
      if (response.crisisDetected && response.crisisGuideMessage) {
        setCrisisGuideMessage(response.crisisGuideMessage)
      }
    } catch (error) {
      setErrorMessage(error instanceof LoginApiError ? error.message : '마음 대화 메시지를 전송하지 못했습니다.')
    } finally {
      setIsSending(false)
    }
  }

  return (
    <main className="chat-page-shell">
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

          <div className="chat-messages" aria-live="polite">
            {isLoading && (
              <div className="chat-loading">
                <Loader2 size={20} aria-hidden="true" />
                오늘의 대화방을 불러오고 있습니다.
              </div>
            )}
            {!isLoading && room?.messages.length === 0 && (
              <div className="empty-chat">
                <p>오늘 마음에 남아 있는 말을 한 문장으로 시작해보세요.</p>
              </div>
            )}
            {room?.messages.map((chatMessage) => (
              <ChatBubble key={chatMessage.messageId} message={chatMessage} />
            ))}
            <div ref={messagesEndRef} />
          </div>

          {errorMessage && <p className="form-message">{errorMessage}</p>}

          <form className="chat-input-row" onSubmit={handleSendMessage}>
            <label className="sr-only" htmlFor="ai-chat-message">
              마음 대화 메시지
            </label>
            <textarea
              id="ai-chat-message"
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

      {crisisGuideMessage && (
        <div className="modal-backdrop" role="presentation" onMouseDown={() => setCrisisGuideMessage('')}>
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

function ChatBubble({ message }: { message: AiChatMessage }) {
  const isUser = message.senderType === 'USER'

  return (
    <article className={`chat-bubble ${isUser ? 'is-user' : 'is-assistant'} ${message.isCrisisDetected ? 'is-crisis' : ''}`}>
      <span>{isUser ? '나' : '마음이'}</span>
      <p>{message.content}</p>
    </article>
  )
}
