package com.mymentalcare.server.application.aichat.usecase

import com.mymentalcare.server.application.aichat.AiChatInvalidRequestException
import com.mymentalcare.server.application.aichat.port.AiChatHistoryDeletionRepository
import com.mymentalcare.server.application.aichat.port.AiChatHistoryInputPort
import com.mymentalcare.server.application.aichat.request.DeleteAiChatHistoryRequest
import com.mymentalcare.server.application.aichat.request.DeleteAiChatHistoryResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class AiChatHistoryService(
    private val aiChatHistoryDeletionRepository: AiChatHistoryDeletionRepository,
) : AiChatHistoryInputPort {
    @Transactional
    override fun deleteHistory(memberId: Long, request: DeleteAiChatHistoryRequest): DeleteAiChatHistoryResponse {
        val deletedCount = when (request.targetType) {
            "CHAT_ROOM" -> aiChatHistoryDeletionRepository.deleteChatRoom(memberId, request.targetId)
            "REPORT" -> aiChatHistoryDeletionRepository.deleteReport(memberId, request.targetId)
            "CHECK_IN" -> aiChatHistoryDeletionRepository.deleteCheckIn(memberId, request.targetId)
            else -> throw AiChatInvalidRequestException("지원하지 않는 삭제 대상입니다.")
        }

        return DeleteAiChatHistoryResponse(deletedCount = deletedCount)
    }
}
