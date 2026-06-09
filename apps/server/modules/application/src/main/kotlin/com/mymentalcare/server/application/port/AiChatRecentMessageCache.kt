package com.mymentalcare.server.application.port

import com.mymentalcare.server.application.aichat.AiReplyMessage

interface AiChatRecentMessageCache {
    fun readRecentMessages(roomId: Long): List<AiReplyMessage>

    fun replaceRecentMessages(roomId: Long, messages: List<AiReplyMessage>)

    fun appendRecentMessages(roomId: Long, messages: List<AiReplyMessage>, limit: Int)
}
