package com.mymentalcare.server.application.aichat.port

import com.mymentalcare.server.application.aichat.request.*
import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.domain.aichat.AiChatRoomSummary

interface AiChatRoomSummaryRepository {
    fun findByRoomId(roomId: Long): AiChatRoomSummary?

    fun save(summary: AiChatRoomSummary): AiChatRoomSummary
}
