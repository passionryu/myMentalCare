package com.mymentalcare.server.bootstrap.aichat.web

import com.mymentalcare.server.application.aichat.port.AiChatHistoryInputPort
import com.mymentalcare.server.application.aichat.request.DeleteAiChatHistoryRequest
import com.mymentalcare.server.bootstrap.aichat.web.request.DeleteAiChatHistoryPayload
import com.mymentalcare.server.bootstrap.aichat.web.request.DeleteAiChatHistoryResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ai-chat")
class AiChatHistoryController(
    private val aiChatHistoryInputPort: AiChatHistoryInputPort,
) {
    @Operation(
        summary = "내 AI 마음 이력 삭제",
        description = "로그인한 사용자의 채팅방, 리포트, 체크인 기록 중 선택한 이력을 삭제합니다.",
    )
    @PostMapping("/history/delete")
    fun deleteHistory(
        @AuthenticationPrincipal memberId: Long,
        @Valid @RequestBody payload: DeleteAiChatHistoryPayload,
    ): ResponseEntity<DeleteAiChatHistoryResponse> {
        val response = aiChatHistoryInputPort.deleteHistory(
            memberId = memberId,
            request = DeleteAiChatHistoryRequest(
                targetType = payload.targetType,
                targetId = payload.targetId,
            ),
        )

        return ResponseEntity.ok(DeleteAiChatHistoryResponse(deletedCount = response.deletedCount))
    }
}
