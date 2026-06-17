package com.mymentalcare.server.application.aichat.port

import com.mymentalcare.server.application.aichat.request.*
import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.domain.aichat.ChatMessage

interface ChatMessageRepository {
    fun findByRoomId(roomId: Long): List<ChatMessage>

    fun findBySegmentId(segmentId: Long): List<ChatMessage>

    fun findRecentByRoomId(roomId: Long, limit: Int): List<ChatMessage>

    fun countByRoomId(roomId: Long): Int

    fun findLatestByRoomId(roomId: Long): ChatMessage?

    fun save(message: ChatMessage): ChatMessage
}
