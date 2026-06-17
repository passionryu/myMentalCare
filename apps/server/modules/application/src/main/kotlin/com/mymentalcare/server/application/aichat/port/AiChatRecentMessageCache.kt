package com.mymentalcare.server.application.aichat.port

import com.mymentalcare.server.application.aichat.request.*
import com.mymentalcare.server.application.aichat.response.*

interface AiChatRecentMessageCache {
    fun readRecentMessages(roomId: Long): List<AiReplyMessage>

    fun replaceRecentMessages(roomId: Long, messages: List<AiReplyMessage>)

    fun appendRecentMessages(roomId: Long, messages: List<AiReplyMessage>, limit: Int)
}
