package com.mymentalcare.server.application.mindstatistics

import com.mymentalcare.server.application.aichat.port.AiChatReportRepository
import com.mymentalcare.server.application.aichat.port.AiChatRoomRepository
import com.mymentalcare.server.application.aichat.port.ChatMessageRepository
import com.mymentalcare.server.application.mindstatistics.usecase.MindStatisticsService
import com.mymentalcare.server.domain.aichat.AiChatReport
import com.mymentalcare.server.domain.aichat.AiChatReportType
import com.mymentalcare.server.domain.aichat.AiChatRoom
import com.mymentalcare.server.domain.aichat.AiChatRoomStatus
import com.mymentalcare.server.domain.aichat.ChatMessage
import com.mymentalcare.server.domain.aichat.ChatMessageSenderType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class MindStatisticsServiceTest {
    @Test
    fun `월별 마음 달력은 대화와 리포트가 있는 날짜를 표시한다`() {
        val roomRepository = FakeAiChatRoomRepository(
            rooms = mutableListOf(
                testRoom(id = 1L, conversationDate = LocalDate.of(2026, 7, 2)),
                testRoom(id = 2L, conversationDate = LocalDate.of(2026, 7, 3)),
            ),
        )
        val messageRepository = FakeChatMessageRepository(
            messages = mutableListOf(
                testMessage(id = 1L, roomId = 1L, messageOrder = 1),
                testMessage(id = 2L, roomId = 1L, messageOrder = 2, senderType = ChatMessageSenderType.ASSISTANT),
                testMessage(id = 3L, roomId = 2L, messageOrder = 1),
            ),
        )
        val reportRepository = FakeAiChatReportRepository(
            reports = mutableListOf(
                testReport(id = 1L, roomId = 1L, conversationDate = LocalDate.of(2026, 7, 2), primaryEmotion = "편안함"),
            ),
        )
        val service = MindStatisticsService(roomRepository, messageRepository, reportRepository)

        val response = service.readCalendar(memberId = 1L, month = YearMonth.of(2026, 7))

        val reportDay = response.days.first { it.date == LocalDate.of(2026, 7, 2) }
        assertEquals(2, response.totalConversationDays)
        assertEquals(1, response.totalReportDays)
        assertTrue(reportDay.hasConversation)
        assertTrue(reportDay.hasReport)
        assertEquals("편안함", reportDay.primaryEmotion)
        assertEquals(100, reportDay.emotionIntensity)
    }

    @Test
    fun `선택한 날짜 상세는 메시지 미리보기와 최신 리포트를 반환한다`() {
        val targetDate = LocalDate.of(2026, 7, 3)
        val roomRepository = FakeAiChatRoomRepository(mutableListOf(testRoom(id = 10L, conversationDate = targetDate)))
        val messageRepository = FakeChatMessageRepository(
            mutableListOf(
                testMessage(id = 10L, roomId = 10L, messageOrder = 1, content = "오늘은 마음이 조금 가벼워졌어"),
                testMessage(id = 11L, roomId = 10L, messageOrder = 2, senderType = ChatMessageSenderType.ASSISTANT, content = "그 가벼움이 반갑게 느껴져요."),
            ),
        )
        val reportRepository = FakeAiChatReportRepository(
            mutableListOf(
                testReport(id = 10L, roomId = 10L, conversationDate = targetDate, primaryEmotion = "가벼움"),
            ),
        )
        val service = MindStatisticsService(roomRepository, messageRepository, reportRepository)

        val response = service.readDayDetail(memberId = 1L, date = targetDate)

        assertEquals(10L, response.roomId)
        assertEquals(2, response.messageCount)
        assertEquals("가벼움", response.report!!.primaryEmotion)
        assertEquals(100, response.report!!.emotionIntensity)
        assertEquals("오늘은 마음이 조금 가벼워졌어", response.messages.first().contentPreview)
    }

    @Test
    fun `최근 감정 통계는 감정 분포와 주간 평균 강도를 계산한다`() {
        val today = LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))
        val roomRepository = FakeAiChatRoomRepository(
            mutableListOf(
                testRoom(id = 20L, conversationDate = today.minusDays(1)),
                testRoom(id = 21L, conversationDate = today.minusDays(8)),
            ),
        )
        val messageRepository = FakeChatMessageRepository(
            mutableListOf(
                testMessage(id = 20L, roomId = 20L, messageOrder = 1),
                testMessage(id = 21L, roomId = 21L, messageOrder = 1),
            ),
        )
        val reportRepository = FakeAiChatReportRepository(
            mutableListOf(
                testReport(id = 20L, roomId = 20L, conversationDate = today.minusDays(1), primaryEmotion = "편안함", emotionIntensity = 7),
                testReport(id = 21L, roomId = 21L, conversationDate = today.minusDays(8), primaryEmotion = "불안", emotionIntensity = 4),
            ),
        )
        val service = MindStatisticsService(roomRepository, messageRepository, reportRepository)

        val response = service.readOverview(memberId = 1L, rangeWeeks = 4)

        assertEquals(2, response.totalConversationDays)
        assertEquals(2, response.totalMessages)
        assertEquals(2, response.totalReports)
        assertEquals(setOf("편안함", "불안"), response.emotionDistribution.map { it.emotion }.toSet())
        assertEquals(4, response.emotionTrend.size)
        assertTrue(response.emotionTrend.mapNotNull { it.averageEmotionIntensity }.all { it in 0.0..100.0 })
    }

    private fun testRoom(id: Long, conversationDate: LocalDate): AiChatRoom {
        return AiChatRoom(
            id = id,
            memberId = 1L,
            chatbotCode = "DEFAULT_EMPATHY",
            conversationDate = conversationDate,
            status = AiChatRoomStatus.ACTIVE,
        )
    }

    private fun testMessage(
        id: Long,
        roomId: Long,
        messageOrder: Int,
        senderType: ChatMessageSenderType = ChatMessageSenderType.USER,
        content: String = "오늘의 마음 대화",
    ): ChatMessage {
        return ChatMessage(
            id = id,
            roomId = roomId,
            senderType = senderType,
            content = content,
            messageOrder = messageOrder,
            isCrisisDetected = false,
            createdAt = LocalDateTime.of(2026, 7, 3, 10, messageOrder),
        )
    }

    private fun testReport(
        id: Long,
        roomId: Long,
        conversationDate: LocalDate,
        primaryEmotion: String,
        emotionIntensity: Int = 6,
    ): AiChatReport {
        return AiChatReport(
            id = id,
            roomId = roomId,
            memberId = 1L,
            conversationDate = conversationDate,
            reportType = AiChatReportType.FULL,
            summary = "오늘의 마음을 정리했습니다.",
            primaryEmotion = primaryEmotion,
            emotionIntensity = emotionIntensity,
            mainCause = "대화",
            emotionalFlow = "편안하게 이어짐",
            todaySentence = "오늘의 마음을 잘 살폈습니다.",
            clientRequestId = null,
            songs = emptyList(),
            createdAt = conversationDate.atTime(22, 0),
        )
    }

    private class FakeAiChatRoomRepository(
        private val rooms: MutableList<AiChatRoom> = mutableListOf(),
    ) : AiChatRoomRepository {
        override fun findTodayRoom(memberId: Long, chatbotCode: String, conversationDate: LocalDate): AiChatRoom? {
            return rooms.firstOrNull { it.memberId == memberId && it.chatbotCode == chatbotCode && it.conversationDate == conversationDate }
        }

        override fun findByMemberId(memberId: Long): List<AiChatRoom> {
            return rooms.filter { it.memberId == memberId }
        }

        override fun findByMemberIdAndDateRange(memberId: Long, startDate: LocalDate, endDate: LocalDate): List<AiChatRoom> {
            return rooms.filter { it.memberId == memberId && !it.conversationDate.isBefore(startDate) && !it.conversationDate.isAfter(endDate) }
        }

        override fun findByIdAndMemberId(roomId: Long, memberId: Long): AiChatRoom? {
            return rooms.firstOrNull { it.id == roomId && it.memberId == memberId }
        }

        override fun save(room: AiChatRoom): AiChatRoom {
            val savedRoom = room.copy(id = room.id.takeIf { it > 0 } ?: (rooms.size + 1).toLong())
            rooms.add(savedRoom)
            return savedRoom
        }
    }

    private class FakeChatMessageRepository(
        private val messages: MutableList<ChatMessage> = mutableListOf(),
    ) : ChatMessageRepository {
        override fun findByRoomId(roomId: Long): List<ChatMessage> {
            return messages.filter { it.roomId == roomId }.sortedBy { it.messageOrder }
        }

        override fun findByRoomIds(roomIds: List<Long>): List<ChatMessage> {
            return messages.filter { it.roomId in roomIds }.sortedWith(compareBy<ChatMessage> { it.roomId }.thenBy { it.messageOrder })
        }

        override fun findBySegmentId(segmentId: Long): List<ChatMessage> {
            return messages.filter { it.segmentId == segmentId }
        }

        override fun findRecentByRoomId(roomId: Long, limit: Int): List<ChatMessage> {
            return findByRoomId(roomId).takeLast(limit)
        }

        override fun countByRoomId(roomId: Long): Int {
            return messages.count { it.roomId == roomId }
        }

        override fun findLatestByRoomId(roomId: Long): ChatMessage? {
            return findByRoomId(roomId).maxByOrNull { it.messageOrder }
        }

        override fun save(message: ChatMessage): ChatMessage {
            val savedMessage = message.copy(id = message.id.takeIf { it > 0 } ?: (messages.size + 1).toLong())
            messages.add(savedMessage)
            return savedMessage
        }
    }

    private class FakeAiChatReportRepository(
        private val reports: MutableList<AiChatReport> = mutableListOf(),
    ) : AiChatReportRepository {
        override fun findLatestByRoomId(roomId: Long): AiChatReport? {
            return reports.filter { it.roomId == roomId }.maxByOrNull { it.createdAt ?: LocalDateTime.MIN }
        }

        override fun findLatestByMemberId(memberId: Long): AiChatReport? {
            return reports.filter { it.memberId == memberId }.maxByOrNull { it.createdAt ?: LocalDateTime.MIN }
        }

        override fun countByMemberId(memberId: Long): Int {
            return reports.count { it.memberId == memberId }
        }

        override fun findByMemberId(memberId: Long): List<AiChatReport> {
            return reports.filter { it.memberId == memberId }
        }

        override fun findByMemberIdAndDateRange(memberId: Long, startDate: LocalDate, endDate: LocalDate): List<AiChatReport> {
            return reports.filter { it.memberId == memberId && !it.conversationDate.isBefore(startDate) && !it.conversationDate.isAfter(endDate) }
        }

        override fun findByIdAndMemberId(reportId: Long, memberId: Long): AiChatReport? {
            return reports.firstOrNull { it.id == reportId && it.memberId == memberId }
        }

        override fun findByRoomIdAndClientRequestId(roomId: Long, clientRequestId: String): AiChatReport? {
            return reports.firstOrNull { it.roomId == roomId && it.clientRequestId == clientRequestId }
        }

        override fun save(report: AiChatReport): AiChatReport {
            val savedReport = report.copy(id = report.id.takeIf { it > 0 } ?: (reports.size + 1).toLong())
            reports.add(savedReport)
            return savedReport
        }
    }
}
