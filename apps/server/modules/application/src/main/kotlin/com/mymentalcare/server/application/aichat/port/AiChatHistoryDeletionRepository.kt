package com.mymentalcare.server.application.aichat.port

import com.mymentalcare.server.application.aichat.request.*
import com.mymentalcare.server.application.aichat.response.*

interface AiChatHistoryDeletionRepository {
    fun deleteChatRoom(memberId: Long, roomId: Long): Int

    fun deleteReport(memberId: Long, reportId: Long): Int

    fun deleteCheckIn(memberId: Long, checkInId: Long): Int
}
