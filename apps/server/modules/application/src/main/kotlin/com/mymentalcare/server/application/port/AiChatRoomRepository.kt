package com.mymentalcare.server.application.port

import com.mymentalcare.server.domain.aichat.AiChatRoom
import java.time.LocalDate

interface AiChatRoomRepository {
    fun findTodayRoom(memberId: Long, chatbotCode: String, conversationDate: LocalDate): AiChatRoom?

    fun save(room: AiChatRoom): AiChatRoom
}
