package com.mymentalcare.server.application.port

import com.mymentalcare.server.domain.aichat.AiChatReport

interface AiChatReportRepository {
    fun findLatestByRoomId(roomId: Long): AiChatReport?

    fun findLatestByMemberId(memberId: Long): AiChatReport?

    fun countByMemberId(memberId: Long): Int

    fun findByRoomIdAndClientRequestId(roomId: Long, clientRequestId: String): AiChatReport?

    fun save(report: AiChatReport): AiChatReport
}
