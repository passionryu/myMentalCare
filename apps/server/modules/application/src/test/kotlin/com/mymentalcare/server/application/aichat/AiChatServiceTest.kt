package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.application.port.AiChatRoomRepository
import com.mymentalcare.server.application.port.AiChatRecentMessageCache
import com.mymentalcare.server.application.port.AiChatRoomSummaryRepository
import com.mymentalcare.server.application.port.ChatMessageRepository
import com.mymentalcare.server.application.port.CrisisDetectionEventRepository
import com.mymentalcare.server.domain.aichat.AiChatRoom
import com.mymentalcare.server.domain.aichat.AiChatRoomSummary
import com.mymentalcare.server.domain.aichat.AiChatRoomStatus
import com.mymentalcare.server.domain.aichat.ChatMessage
import com.mymentalcare.server.domain.aichat.ChatMessageSenderType
import com.mymentalcare.server.domain.aichat.CrisisDetectionEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AiChatServiceTest {
    @Test
    fun `오늘 대화방이 없으면 새로 만들고 빈 메시지 목록을 반환한다`() {
        val roomRepository = FakeAiChatRoomRepository()
        val service = aiChatService(roomRepository = roomRepository)

        val response = service.readTodayRoom(memberId = 1L)

        assertEquals(1L, response.roomId)
        assertEquals("마음이", response.chatbotName)
        assertEquals(emptyList<AiChatMessageResponse>(), response.messages)
        assertEquals(1, roomRepository.rooms.size)
    }

    @Test
    fun `일반 메시지를 보내면 사용자 메시지와 공감형 응답을 함께 저장한다`() {
        val messageRepository = FakeChatMessageRepository()
        val aiReplyProvider = FakeAiReplyProvider(reply = "OpenAI가 만든 마음이 응답입니다.")
        val service = aiChatService(
            messageRepository = messageRepository,
            aiReplyProvider = aiReplyProvider,
        )

        val response = service.sendMessage(1L, SendAiChatMessageRequest(content = "오늘 조금 지쳤어"))

        assertEquals(false, response.crisisDetected)
        assertEquals(2, response.room.messages.size)
        assertEquals("USER", response.userMessage.senderType)
        assertEquals("ASSISTANT", response.assistantMessage.senderType)
        assertEquals("OpenAI가 만든 마음이 응답입니다.", response.assistantMessage.content)
        assertEquals(2, messageRepository.messages.size)
    }

    @Test
    fun `위기 키워드가 포함되면 위기 이벤트를 기록하고 안전 안내를 반환한다`() {
        val eventRepository = FakeCrisisDetectionEventRepository()
        val service = aiChatService(eventRepository = eventRepository)

        val response = service.sendMessage(1L, SendAiChatMessageRequest(content = "죽고 싶다"))

        assertTrue(response.crisisDetected)
        assertEquals(SAFETY_GUIDE_MESSAGE, response.crisisGuideMessage)
        assertEquals(SAFETY_GUIDE_MESSAGE, response.assistantMessage.content)
        assertEquals(1, eventRepository.events.size)
        assertEquals(1L, eventRepository.events.first().memberId)
        assertNotNull(eventRepository.events.first().messageId)
    }

    @Test
    fun `AI 응답 생성에는 최근 6개 메시지만 전달한다`() {
        val messageRepository = FakeChatMessageRepository()
        val aiReplyProvider = FakeAiReplyProvider(reply = "최근 대화를 참고한 응답입니다.")
        val service = aiChatService(
            messageRepository = messageRepository,
            aiReplyProvider = aiReplyProvider,
        )

        repeat(6) { index ->
            messageRepository.save(
                ChatMessage(
                    id = 0,
                    roomId = 1L,
                    senderType = if (index % 2 == 0) ChatMessageSenderType.USER else ChatMessageSenderType.ASSISTANT,
                    content = "기존 메시지 ${index + 1}",
                    messageOrder = index + 1,
                    isCrisisDetected = false,
                )
            )
        }

        service.sendMessage(1L, SendAiChatMessageRequest(content = "오늘은 조금 나아졌어"))

        assertEquals(6, aiReplyProvider.lastRequest!!.recentMessages.size)
        assertEquals("기존 메시지 2", aiReplyProvider.lastRequest!!.recentMessages.first().content)
        assertEquals("오늘은 조금 나아졌어", aiReplyProvider.lastRequest!!.recentMessages.last().content)
    }

    @Test
    fun `미요약 메시지가 6개 이상이면 오늘 대화 요약을 갱신하고 AI 요청에 반영한다`() {
        val messageRepository = FakeChatMessageRepository()
        val summaryRepository = FakeAiChatRoomSummaryRepository()
        val aiReplyProvider = FakeAiReplyProvider(reply = "오늘 대화 흐름을 기억한 응답입니다.")
        val service = aiChatService(
            messageRepository = messageRepository,
            summaryRepository = summaryRepository,
            aiReplyProvider = aiReplyProvider,
        )

        repeat(5) { index ->
            messageRepository.save(
                ChatMessage(
                    id = 0,
                    roomId = 1L,
                    senderType = ChatMessageSenderType.USER,
                    content = "오전 대화 ${index + 1}: 회사 일 때문에 불안해",
                    messageOrder = index + 1,
                    isCrisisDetected = false,
                )
            )
        }

        service.sendMessage(1L, SendAiChatMessageRequest(content = "오후에도 같은 일이 계속 생각나"))

        val savedSummary = summaryRepository.summaries.single()
        assertEquals(6L, savedSummary.lastSummarizedMessageId)
        assertTrue(savedSummary.summary.contains("회사 일 때문에 불안해"))
        assertEquals("불안과 걱정", savedSummary.emotionalState)
        assertEquals(savedSummary.summary, aiReplyProvider.lastRequest!!.summaryContext!!.summary)
    }

    @Test
    fun `요약 갱신이 실패해도 메시지 전송과 AI 응답 생성은 계속 진행한다`() {
        val messageRepository = FakeChatMessageRepository()
        val summaryRepository = FakeAiChatRoomSummaryRepository()
        val aiReplyProvider = FakeAiReplyProvider(reply = "최근 메시지만 보고 응답합니다.")
        val service = aiChatService(
            messageRepository = messageRepository,
            summaryRepository = summaryRepository,
            aiChatSummaryGenerator = FailingAiChatSummaryGenerator(),
            aiReplyProvider = aiReplyProvider,
        )

        repeat(5) { index ->
            messageRepository.save(
                ChatMessage(
                    id = 0,
                    roomId = 1L,
                    senderType = ChatMessageSenderType.USER,
                    content = "요약 실패 테스트 메시지 ${index + 1}",
                    messageOrder = index + 1,
                    isCrisisDetected = false,
                )
            )
        }

        val response = service.sendMessage(1L, SendAiChatMessageRequest(content = "요약이 실패해도 대화는 이어져야 해"))

        assertEquals("최근 메시지만 보고 응답합니다.", response.assistantMessage.content)
        assertEquals(emptyList<AiChatRoomSummary>(), summaryRepository.summaries)
        assertEquals(null, aiReplyProvider.lastRequest!!.summaryContext)
    }

    @Test
    fun `위기 키워드가 포함되면 AI 응답 생성기를 호출하지 않는다`() {
        val aiReplyProvider = FakeAiReplyProvider(reply = "호출되면 안 되는 응답입니다.")
        val service = aiChatService(aiReplyProvider = aiReplyProvider)

        service.sendMessage(1L, SendAiChatMessageRequest(content = "자해하고 싶다"))

        assertEquals(0, aiReplyProvider.callCount)
    }

    @Test
    fun `AI 답변 생성에 실패하면 실패 여부와 사용자 안내 메시지를 함께 반환한다`() {
        val service = aiChatService(
            aiReplyProvider = FakeAiReplyProvider(
                reply = OPEN_AI_REPLY_ERROR_MESSAGE,
                failed = true,
            )
        )

        val response = service.sendMessage(1L, SendAiChatMessageRequest(content = "오늘은 조금 불안해"))

        assertEquals(true, response.aiReplyFailed)
        assertEquals(OPEN_AI_REPLY_ERROR_MESSAGE, response.aiReplyErrorMessage)
        assertEquals(OPEN_AI_REPLY_ERROR_MESSAGE, response.assistantMessage.content)
    }

    private fun aiChatService(
        roomRepository: FakeAiChatRoomRepository = FakeAiChatRoomRepository(),
        messageRepository: FakeChatMessageRepository = FakeChatMessageRepository(),
        summaryRepository: FakeAiChatRoomSummaryRepository = FakeAiChatRoomSummaryRepository(),
        recentMessageCache: FakeAiChatRecentMessageCache = FakeAiChatRecentMessageCache(),
        eventRepository: FakeCrisisDetectionEventRepository = FakeCrisisDetectionEventRepository(),
        aiChatSummaryGenerator: AiChatSummaryGenerator = DefaultAiChatSummaryGenerator(),
        aiReplyProvider: AiReplyProvider = FakeAiReplyProvider(reply = "마음이 기본 응답입니다."),
    ): AiChatService {
        return AiChatService(
            todayAiChatRoomReader = TodayAiChatRoomReader(roomRepository),
            aiChatMessageAppender = AiChatMessageAppender(messageRepository),
            aiChatSummaryRefreshProcessor = AiChatSummaryRefreshProcessor(
                summaryRepository,
                messageRepository,
                AiChatSummaryRefreshDecider(),
                aiChatSummaryGenerator,
            ),
            aiReplyContextReader = AiReplyContextReader(messageRepository, summaryRepository, recentMessageCache),
            crisisDetectionRecorder = CrisisDetectionRecorder(eventRepository),
            aiChatResponseAssembler = AiChatResponseAssembler(messageRepository),
            crisisKeywordDetector = CrisisKeywordDetector(),
            aiReplyProvider = aiReplyProvider,
        )
    }

    private class FakeAiChatRoomRepository : AiChatRoomRepository {
        val rooms = mutableListOf<AiChatRoom>()

        override fun findTodayRoom(memberId: Long, chatbotCode: String, conversationDate: LocalDate): AiChatRoom? {
            return rooms.firstOrNull {
                it.memberId == memberId &&
                    it.chatbotCode == chatbotCode &&
                    it.conversationDate == conversationDate
            }
        }

        override fun save(room: AiChatRoom): AiChatRoom {
            val savedRoom = room.copy(id = (rooms.size + 1).toLong(), status = AiChatRoomStatus.ACTIVE)
            rooms.add(savedRoom)
            return savedRoom
        }
    }

    private class FakeAiChatRoomSummaryRepository : AiChatRoomSummaryRepository {
        val summaries = mutableListOf<AiChatRoomSummary>()

        override fun findByRoomId(roomId: Long): AiChatRoomSummary? {
            return summaries.firstOrNull { it.roomId == roomId }
        }

        override fun save(summary: AiChatRoomSummary): AiChatRoomSummary {
            val savedSummary = summary.copy(id = summary.id.takeIf { it > 0 } ?: (summaries.size + 1).toLong())
            summaries.removeIf { it.roomId == savedSummary.roomId }
            summaries.add(savedSummary)
            return savedSummary
        }
    }

    private class FakeChatMessageRepository : ChatMessageRepository {
        val messages = mutableListOf<ChatMessage>()

        override fun findByRoomId(roomId: Long): List<ChatMessage> {
            return messages.filter { it.roomId == roomId }.sortedBy { it.messageOrder }
        }

        override fun findRecentByRoomId(roomId: Long, limit: Int): List<ChatMessage> {
            return findByRoomId(roomId).takeLast(limit)
        }

        override fun countByRoomId(roomId: Long): Int {
            return messages.count { it.roomId == roomId }
        }

        override fun save(message: ChatMessage): ChatMessage {
            val savedMessage = message.copy(id = (messages.size + 1).toLong())
            messages.add(savedMessage)
            return savedMessage
        }
    }

    private class FakeAiChatRecentMessageCache : AiChatRecentMessageCache {
        private val cachedMessages = mutableListOf<AiReplyMessage>()

        override fun readRecentMessages(roomId: Long): List<AiReplyMessage> {
            return cachedMessages.toList()
        }

        override fun replaceRecentMessages(roomId: Long, messages: List<AiReplyMessage>) {
            cachedMessages.clear()
            cachedMessages.addAll(messages)
        }

        override fun appendRecentMessages(roomId: Long, messages: List<AiReplyMessage>, limit: Int) {
            val mergedMessages = (cachedMessages + messages).takeLast(limit)
            cachedMessages.clear()
            cachedMessages.addAll(mergedMessages)
        }
    }

    private class FakeCrisisDetectionEventRepository : CrisisDetectionEventRepository {
        val events = mutableListOf<CrisisDetectionEvent>()

        override fun save(event: CrisisDetectionEvent): CrisisDetectionEvent {
            val savedEvent = event.copy(id = (events.size + 1).toLong())
            events.add(savedEvent)
            return savedEvent
        }
    }

    private class FailingAiChatSummaryGenerator : AiChatSummaryGenerator {
        override fun generateTodaySummary(
            existingSummary: AiChatRoomSummary?,
            unsummarizedMessages: List<ChatMessage>,
        ): AiChatSummaryGenerationResult {
            throw IllegalStateException("요약 생성 실패")
        }
    }

    private class FakeAiReplyProvider(
        private val reply: String,
        private val failed: Boolean = false,
    ) : AiReplyProvider {
        var callCount = 0
        var lastRequest: AiReplyRequest? = null

        override fun generateReply(request: AiReplyRequest): AiReplyResponse {
            callCount += 1
            lastRequest = request
            return AiReplyResponse(reply, failed)
        }
    }
}
