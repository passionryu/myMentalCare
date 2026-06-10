package com.mymentalcare.server.application.aichat

interface AiChatInputPort {
    fun readTodayRoom(memberId: Long): TodayAiChatRoomResponse

    fun sendMessage(memberId: Long, request: SendAiChatMessageRequest): SendAiChatMessageResponse
}
