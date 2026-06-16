package com.mymentalcare.server.application.port

import com.mymentalcare.server.domain.aichat.AiChatReport

interface AiChatReportRepository {
    fun findLatestByRoomId(roomId: Long): AiChatReport?

    fun findLatestByMemberId(memberId: Long): AiChatReport?

    fun countByMemberId(memberId: Long): Int

    fun findByMemberId(memberId: Long): List<AiChatReport>

    fun findByIdAndMemberId(reportId: Long, memberId: Long): AiChatReport?

    fun findByRoomIdAndClientRequestId(roomId: Long, clientRequestId: String): AiChatReport?

    fun save(report: AiChatReport): AiChatReport
}
