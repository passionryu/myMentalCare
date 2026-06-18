package com.mymentalcare.server.application.aichat.usecase

import com.mymentalcare.server.application.aichat.AiChatResponseAssembler
import com.mymentalcare.server.application.aichat.port.AiChatRoomInputPort
import com.mymentalcare.server.application.aichat.port.AiChatRoomRepository
import com.mymentalcare.server.application.aichat.port.ChatMessageRepository
import com.mymentalcare.server.application.aichat.reader.TodayAiChatRoomReader
import com.mymentalcare.server.application.aichat.response.AiChatHistoryRoomDetailResponse
import com.mymentalcare.server.application.aichat.response.AiChatHistoryRoomResponse
import com.mymentalcare.server.application.aichat.response.TodayAiChatRoomResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class AiChatRoomService(
    private val todayAiChatRoomReader: TodayAiChatRoomReader,
    private val aiChatResponseAssembler: AiChatResponseAssembler,
    private val aiChatRoomRepository: AiChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
) : AiChatRoomInputPort {
    // 오늘의 AI 마음 대화방을 조회하거나 없으면 생성한다.
    @Transactional
    override fun readTodayRoom(memberId: Long): TodayAiChatRoomResponse {
        val room = todayAiChatRoomReader.readOrCreateTodayRoom(memberId)

        return aiChatResponseAssembler.toTodayRoomResponse(room)
    }

    @Transactional(readOnly = true)
    override fun readHistoryRooms(memberId: Long): List<AiChatHistoryRoomResponse> {
        return aiChatRoomRepository.findByMemberId(memberId).map { room ->
            val latestMessage = chatMessageRepository.findLatestByRoomId(room.id)
            AiChatHistoryRoomResponse(
                roomId = room.id,
                conversationDate = room.conversationDate,
                status = room.status.name,
                messageCount = chatMessageRepository.countByRoomId(room.id),
                latestMessage = latestMessage?.content,
                latestMessageAt = latestMessage?.createdAt,
            )
        }
    }

    @Transactional(readOnly = true)
    override fun readHistoryRoom(memberId: Long, roomId: Long): AiChatHistoryRoomDetailResponse? {
        val room = aiChatRoomRepository.findByIdAndMemberId(roomId = roomId, memberId = memberId) ?: return null
        return AiChatHistoryRoomDetailResponse(
            roomId = room.id,
            chatbotCode = room.chatbotCode,
            chatbotName = "마음이",
            conversationDate = room.conversationDate,
            status = room.status.name,
            messages = chatMessageRepository.findByRoomId(room.id).map { aiChatResponseAssembler.toMessageResponse(it) },
        )
    }
}
