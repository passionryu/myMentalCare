package com.mymentalcare.server.bootstrap.aichat

import com.mymentalcare.server.application.aichat.AiChatInputPort
import com.mymentalcare.server.application.aichat.SendAiChatMessageRequest
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ai-chat")
class AiChatController(
    private val aiChatInputPort: AiChatInputPort,
) {
    @Operation(
        summary = "오늘의 AI 마음 대화방 조회",
        description = "로그인한 사용자의 한국 시간 기준 오늘 대화방과 메시지를 조회합니다. 오늘 대화방이 없으면 새로 생성합니다.",
    )
    @GetMapping("/rooms/today")
    fun readTodayRoom(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<TodayAiChatRoomResponse> {
        val response = aiChatInputPort.readTodayRoom(memberId)

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
        val response = aiChatInputPort.sendMessage(
            memberId = memberId,
            request = SendAiChatMessageRequest(content = request.content),
        )

        return ResponseEntity.ok(response.toBootstrapResponse())
    }
}
