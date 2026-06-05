package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.application.port.AiChatRoomRepository
import com.mymentalcare.server.application.port.ChatMessageRepository
import com.mymentalcare.server.application.port.CrisisDetectionEventRepository
import com.mymentalcare.server.domain.aichat.AiChatRoom
import com.mymentalcare.server.domain.aichat.AiChatRoomStatus
import com.mymentalcare.server.domain.aichat.ChatMessage
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
        val service = aiChatService(messageRepository = messageRepository)

        val response = service.sendMessage(1L, SendAiChatMessageRequest(content = "오늘 조금 지쳤어"))

        assertEquals(false, response.crisisDetected)
        assertEquals(2, response.room.messages.size)
        assertEquals("USER", response.userMessage.senderType)
        assertEquals("ASSISTANT", response.assistantMessage.senderType)
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

    private fun aiChatService(
        roomRepository: FakeAiChatRoomRepository = FakeAiChatRoomRepository(),
        messageRepository: FakeChatMessageRepository = FakeChatMessageRepository(),
        eventRepository: FakeCrisisDetectionEventRepository = FakeCrisisDetectionEventRepository(),
    ): AiChatService {
        return AiChatService(
            aiChatRoomRepository = roomRepository,
            chatMessageRepository = messageRepository,
            crisisDetectionEventRepository = eventRepository,
            crisisKeywordDetector = CrisisKeywordDetector(),
            defaultEmpathyReplyProvider = DefaultEmpathyReplyProvider(),
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

    private class FakeChatMessageRepository : ChatMessageRepository {
        val messages = mutableListOf<ChatMessage>()

        override fun findByRoomId(roomId: Long): List<ChatMessage> {
            return messages.filter { it.roomId == roomId }.sortedBy { it.messageOrder }
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

    private class FakeCrisisDetectionEventRepository : CrisisDetectionEventRepository {
        val events = mutableListOf<CrisisDetectionEvent>()

        override fun save(event: CrisisDetectionEvent): CrisisDetectionEvent {
            val savedEvent = event.copy(id = (events.size + 1).toLong())
            events.add(savedEvent)
            return savedEvent
        }
    }
}
