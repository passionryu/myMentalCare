package com.mymentalcare.server.application.aichat.port

import com.mymentalcare.server.application.aichat.request.*
import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.domain.aichat.AiChatRoom
import java.time.LocalDate

interface AiChatRoomRepository {
    fun findTodayRoom(memberId: Long, chatbotCode: String, conversationDate: LocalDate): AiChatRoom?

    fun findByMemberId(memberId: Long): List<AiChatRoom>

    fun findByIdAndMemberId(roomId: Long, memberId: Long): AiChatRoom?

    fun save(room: AiChatRoom): AiChatRoom
}
