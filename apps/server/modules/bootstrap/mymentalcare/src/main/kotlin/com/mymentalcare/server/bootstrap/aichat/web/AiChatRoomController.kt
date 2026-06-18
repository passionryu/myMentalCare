package com.mymentalcare.server.bootstrap.aichat.web

import com.mymentalcare.server.application.aichat.port.AiChatRoomInputPort
import com.mymentalcare.server.bootstrap.aichat.web.response.AiChatHistoryRoomDetailResponse
import com.mymentalcare.server.bootstrap.aichat.web.response.AiChatHistoryRoomResponse
import com.mymentalcare.server.bootstrap.aichat.web.response.TodayAiChatRoomResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ai-chat")
class AiChatRoomController(
    private val aiChatRoomInputPort: AiChatRoomInputPort,
) {
    @Operation(
        summary = "오늘의 AI 마음 대화방 조회",
        description = "로그인한 사용자의 한국 시간 기준 오늘 대화방과 메시지를 조회합니다. 오늘 대화방이 없으면 새로 생성합니다.",
    )
    @GetMapping("/rooms/today")
    fun readTodayRoom(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<TodayAiChatRoomResponse> {
        val response = aiChatRoomInputPort.readTodayRoom(memberId)

        return ResponseEntity.ok(response.toBootstrapResponse())
    }

    @Operation(
        summary = "내 AI 마음 대화방 이력 목록 조회",
        description = "로그인한 사용자의 날짜별 대화방 목록과 최신 메시지 요약을 조회합니다.",
    )
    @GetMapping("/rooms")
    fun readHistoryRooms(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<List<AiChatHistoryRoomResponse>> {
        return ResponseEntity.ok(
            aiChatRoomInputPort.readHistoryRooms(memberId).map { it.toBootstrapResponse() }
        )
    }

    @Operation(
        summary = "내 AI 마음 대화방 상세 조회",
        description = "로그인한 사용자의 특정 대화방 메시지 이력을 조회합니다.",
    )
    @GetMapping("/rooms/{roomId}")
    fun readHistoryRoom(
        @AuthenticationPrincipal memberId: Long,
        @PathVariable roomId: Long,
    ): ResponseEntity<AiChatHistoryRoomDetailResponse> {
        val response = aiChatRoomInputPort.readHistoryRoom(memberId = memberId, roomId = roomId)

        return response?.let { ResponseEntity.ok(it.toBootstrapResponse()) }
            ?: ResponseEntity.notFound().build()
    }
}
