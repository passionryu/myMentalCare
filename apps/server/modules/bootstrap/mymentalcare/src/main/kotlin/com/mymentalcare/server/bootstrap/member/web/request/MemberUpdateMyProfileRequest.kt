package com.mymentalcare.server.bootstrap.member.web.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MemberUpdateMyProfileRequest(
    @field:NotBlank(message = "이름을 입력해주세요.")
    @field:Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
    val name: String,

    @field:Email(message = "이메일 형식을 확인해주세요.")
    val email: String?,

    @field:Size(max = 30, message = "전화번호는 30자 이하로 입력해주세요.")
    val phone: String?,
)
