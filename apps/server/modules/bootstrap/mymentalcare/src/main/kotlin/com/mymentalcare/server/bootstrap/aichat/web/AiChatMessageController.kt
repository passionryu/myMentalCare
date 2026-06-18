package com.mymentalcare.server.bootstrap.aichat.web

import com.mymentalcare.server.application.aichat.port.AiChatMessageInputPort
import com.mymentalcare.server.application.aichat.request.SendAiChatMessageRequest
import com.mymentalcare.server.application.aichat.request.StartAiChatSegmentRequest
import com.mymentalcare.server.bootstrap.aichat.web.request.SendAiChatMessagePayload
import com.mymentalcare.server.bootstrap.aichat.web.request.StartAiChatSegmentPayload
import com.mymentalcare.server.bootstrap.aichat.web.response.SendAiChatMessageResponse
import com.mymentalcare.server.bootstrap.aichat.web.response.StartAiChatSegmentResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ai-chat")
class AiChatMessageController(
    private val aiChatMessageInputPort: AiChatMessageInputPort,
) {
    @Operation(
        summary = "오늘 대화방 새 주제 시작",
        description = "체크인 없이 오늘 대화방 안에 새 주제 구간을 만들고 마음이의 첫 메시지를 반환합니다.",
    )
    @PostMapping("/rooms/today/segments")
    fun startSegment(
        @AuthenticationPrincipal memberId: Long,
        @Valid @RequestBody request: StartAiChatSegmentPayload,
    ): ResponseEntity<StartAiChatSegmentResponse> {
        val response = aiChatMessageInputPort.startSegment(
            memberId = memberId,
            request = StartAiChatSegmentRequest(
                startType = request.startType,
                clientRequestId = request.clientRequestId,
            ),
        )

        return ResponseEntity.ok(response.toBootstrapResponse())
    }

    @Operation(
        summary = "AI 마음 대화 메시지 전송",
        description = "사용자 메시지를 저장하고 기본 공감형 챗봇 응답을 생성합니다. 위기 키워드가 감지되면 안전 안내를 함께 반환합니다.",
    )
    @PostMapping("/rooms/today/messages")
    fun sendMessage(
        @AuthenticationPrincipal memberId: Long,
        @Valid @RequestBody request: SendAiChatMessagePayload,
    ): ResponseEntity<SendAiChatMessageResponse> {
        val response = aiChatMessageInputPort.sendMessage(
            memberId = memberId,
            request = SendAiChatMessageRequest(
                content = request.content,
                segmentId = request.segmentId,
                clientRequestId = request.clientRequestId,
            ),
        )

        val bootstrapResponse = response.toBootstrapResponse()
        val status = if (response.aiReplyFailed) HttpStatus.SERVICE_UNAVAILABLE else HttpStatus.OK

        return ResponseEntity.status(status).body(bootstrapResponse)
    }
}
