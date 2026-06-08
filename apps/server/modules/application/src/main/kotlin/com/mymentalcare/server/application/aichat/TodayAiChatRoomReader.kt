package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.application.port.AiChatRoomRepository
import com.mymentalcare.server.domain.aichat.AiChatRoom
import com.mymentalcare.server.domain.aichat.AiChatRoomStatus
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId

private const val DEFAULT_EMPATHY_CHATBOT_CODE = "DEFAULT_EMPATHY"
private val KOREA_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")

@Component
internal class TodayAiChatRoomReader(
    private val aiChatRoomRepository: AiChatRoomRepository,
) {
    // 한국 시간 기준 오늘의 기본 공감형 챗봇 대화방을 조회하거나 새로 만든다.
    fun readOrCreateTodayRoom(memberId: Long): AiChatRoom {
        val today = LocalDate.now(KOREA_ZONE_ID)
        return aiChatRoomRepository.findTodayRoom(memberId, DEFAULT_EMPATHY_CHATBOT_CODE, today)
            ?: aiChatRoomRepository.save(
                AiChatRoom(
                    id = 0,
                    memberId = memberId,
                    chatbotCode = DEFAULT_EMPATHY_CHATBOT_CODE,
                    conversationDate = today,
                    status = AiChatRoomStatus.ACTIVE,
                )
            )
    }
}
