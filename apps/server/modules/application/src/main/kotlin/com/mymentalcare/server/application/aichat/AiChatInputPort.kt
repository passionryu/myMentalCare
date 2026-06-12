package com.mymentalcare.server.application.aichat

interface AiChatInputPort {
    fun readTodayRoom(memberId: Long): TodayAiChatRoomResponse

    fun startSegment(memberId: Long, request: StartAiChatSegmentRequest): StartAiChatSegmentResponse

    fun startCheckInSegment(memberId: Long, request: StartAiChatCheckInRequest): StartAiChatSegmentResponse

    fun sendMessage(memberId: Long, request: SendAiChatMessageRequest): SendAiChatMessageResponse

    fun readTodayReportReadiness(memberId: Long): AiChatReportReadinessResponse

    fun createTodayReport(memberId: Long, request: CreateAiChatReportRequest): AiChatReportResponse

    fun readLatestTodayReport(memberId: Long): AiChatReportResponse?
}
