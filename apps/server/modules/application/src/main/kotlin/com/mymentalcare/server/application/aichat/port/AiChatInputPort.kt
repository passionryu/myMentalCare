package com.mymentalcare.server.application.aichat.port

import com.mymentalcare.server.application.aichat.request.*
import com.mymentalcare.server.application.aichat.response.*

interface AiChatInputPort {
    fun readTodayRoom(memberId: Long): TodayAiChatRoomResponse

    fun readHistoryRooms(memberId: Long): List<AiChatHistoryRoomResponse>

    fun readHistoryRoom(memberId: Long, roomId: Long): AiChatHistoryRoomDetailResponse?

    fun readReports(memberId: Long): List<AiChatReportResponse>

    fun readReport(memberId: Long, reportId: Long): AiChatReportResponse?

    fun readCheckIns(memberId: Long): List<AiChatCheckInHistoryResponse>

    fun deleteHistory(memberId: Long, request: DeleteAiChatHistoryRequest): DeleteAiChatHistoryResponse

    fun startSegment(memberId: Long, request: StartAiChatSegmentRequest): StartAiChatSegmentResponse

    fun startCheckInSegment(memberId: Long, request: StartAiChatCheckInRequest): StartAiChatSegmentResponse

    fun sendMessage(memberId: Long, request: SendAiChatMessageRequest): SendAiChatMessageResponse

    fun readTodayReportReadiness(memberId: Long): AiChatReportReadinessResponse

    fun createTodayReport(memberId: Long, request: CreateAiChatReportRequest): AiChatReportResponse

    fun readLatestTodayReport(memberId: Long): AiChatReportResponse?
}
