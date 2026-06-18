package com.mymentalcare.server.application.aichat.port

import com.mymentalcare.server.application.aichat.response.AiChatHistoryRoomDetailResponse
import com.mymentalcare.server.application.aichat.response.AiChatHistoryRoomResponse
import com.mymentalcare.server.application.aichat.response.TodayAiChatRoomResponse

interface AiChatRoomInputPort {
    fun readTodayRoom(memberId: Long): TodayAiChatRoomResponse

    fun readHistoryRooms(memberId: Long): List<AiChatHistoryRoomResponse>

    fun readHistoryRoom(memberId: Long, roomId: Long): AiChatHistoryRoomDetailResponse?
}
