package com.mymentalcare.server.application.port

interface AiChatHistoryDeletionRepository {
    fun deleteChatRoom(memberId: Long, roomId: Long): Int

    fun deleteReport(memberId: Long, reportId: Long): Int

    fun deleteCheckIn(memberId: Long, checkInId: Long): Int
}
