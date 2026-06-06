package com.mymentalcare.server.application.port

import com.mymentalcare.server.domain.aichat.ChatMessage

interface ChatMessageRepository {
    fun findByRoomId(roomId: Long): List<ChatMessage>

    fun countByRoomId(roomId: Long): Int

    fun save(message: ChatMessage): ChatMessage
}
