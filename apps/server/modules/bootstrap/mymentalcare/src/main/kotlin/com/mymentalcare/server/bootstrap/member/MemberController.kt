package com.mymentalcare.server.bootstrap.member

import com.mymentalcare.server.application.member.MemberInputPort
import com.mymentalcare.server.application.member.MemberNotificationSettingRequest
import com.mymentalcare.server.application.member.SignUpMemberRequest
import com.mymentalcare.server.application.member.UpdateMyProfileRequest
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalTime

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberInputPort: MemberInputPort,
) {
    @Operation(
        summary = "회원가입",
        description = "로그인 아이디, 이름, 비밀번호를 기반으로 새 회원을 생성합니다. 이메일과 전화번호는 선택 입력입니다.",
    )
    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody request: MemberSignUpRequest,
    ): ResponseEntity<MemberSignUpResponse> {
        val response = memberInputPort.signUp(
            SignUpMemberRequest(
                loginId = request.loginId,
                email = request.email,
                password = request.password,
                name = request.name,
                phone = request.phone,
            )
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                MemberSignUpResponse(
                    memberId = response.memberId,
                    loginId = response.loginId,
                    name = response.name,
                )
            )
    }

    @Operation(
        summary = "내 프로필 조회",
        description = "로그인한 사용자의 이름, 로그인 아이디, 이메일, 전화번호를 조회합니다.",
    )
    @GetMapping("/me")
    fun readMyProfile(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<MyProfileResponse> {
        val response = memberInputPort.readMyProfile(memberId)

        return ResponseEntity.ok(
            MyProfileResponse(
                memberId = response.memberId,
                loginId = response.loginId,
                email = response.email,
                name = response.name,
                phone = response.phone,
            )
        )
    }

    @Operation(
        summary = "내 프로필 수정",
        description = "로그인한 사용자의 이름, 이메일, 전화번호를 수정합니다. 로그인 아이디와 비밀번호는 수정하지 않습니다.",
    )
    @PatchMapping("/me")
    fun updateMyProfile(
        @AuthenticationPrincipal memberId: Long,
        @Valid @RequestBody request: MemberUpdateMyProfileRequest,
    ): ResponseEntity<MyProfileResponse> {
        val response = memberInputPort.updateMyProfile(
            memberId = memberId,
            request = UpdateMyProfileRequest(
                name = request.name,
                email = request.email,
                phone = request.phone,
            ),
        )

        return ResponseEntity.ok(
            MyProfileResponse(
                memberId = response.memberId,
                loginId = response.loginId,
                email = response.email,
                name = response.name,
                phone = response.phone,
            )
        )
    }

    @Operation(
        summary = "내 마음 체크 알림 설정 조회",
        description = "로그인한 사용자의 마음 체크 알림 사용 여부, 시간, 요일을 조회합니다.",
    )
    @GetMapping("/me/notification-settings")
    fun readNotificationSetting(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<MemberNotificationSettingResponse> {
        val response = memberInputPort.readNotificationSetting(memberId)

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
        val response = memberInputPort.updateNotificationSetting(
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
