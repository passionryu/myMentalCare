package com.mymentalcare.server.application.port

import com.mymentalcare.server.domain.aichat.AiChatRoomSummary

interface AiChatRoomSummaryRepository {
    fun findByRoomId(roomId: Long): AiChatRoomSummary?

    fun save(summary: AiChatRoomSummary): AiChatRoomSummary
}
