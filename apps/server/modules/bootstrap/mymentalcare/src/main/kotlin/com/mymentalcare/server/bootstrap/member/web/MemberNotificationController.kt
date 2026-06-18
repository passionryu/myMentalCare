package com.mymentalcare.server.bootstrap.member.web

import com.mymentalcare.server.application.member.port.MemberNotificationInputPort
import com.mymentalcare.server.application.member.request.MemberNotificationSettingRequest
import com.mymentalcare.server.bootstrap.member.web.request.MemberNotificationSettingPayload
import com.mymentalcare.server.bootstrap.member.web.response.MemberNotificationSettingResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalTime

@RestController
@RequestMapping("/api/members")
class MemberNotificationController(
    private val memberNotificationInputPort: MemberNotificationInputPort,
) {
    @Operation(
        summary = "내 마음 체크 알림 설정 조회",
        description = "로그인한 사용자의 마음 체크 알림 사용 여부, 시간, 요일을 조회합니다.",
    )
    @GetMapping("/me/notification-settings")
    fun readNotificationSetting(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<MemberNotificationSettingResponse> {
        val response = memberNotificationInputPort.readNotificationSetting(memberId)

        return ResponseEntity.ok(
            MemberNotificationSettingResponse(
                enabled = response.enabled,
                notificationTime = response.notificationTime,
                weekdays = response.weekdays,
            )
        )
    }

    @Operation(
        summary = "내 마음 체크 알림 설정 수정",
        description = "로그인한 사용자의 마음 체크 알림 사용 여부, 시간, 요일을 저장합니다.",
    )
    @PatchMapping("/me/notification-settings")
    fun updateNotificationSetting(
        @AuthenticationPrincipal memberId: Long,
        @Valid @RequestBody payload: MemberNotificationSettingPayload,
    ): ResponseEntity<MemberNotificationSettingResponse> {
        val response = memberNotificationInputPort.updateNotificationSetting(
            memberId = memberId,
            request = MemberNotificationSettingRequest(
                enabled = payload.enabled,
                notificationTime = LocalTime.parse(payload.notificationTime),
                weekdays = payload.weekdays,
            ),
        )

        return ResponseEntity.ok(
            MemberNotificationSettingResponse(
                enabled = response.enabled,
                notificationTime = response.notificationTime,
                weekdays = response.weekdays,
            )
        )
    }
}
