package com.mymentalcare.server.bootstrap.member.web.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class MemberSignUpRequest(
    @field:NotBlank(message = "로그인 아이디를 입력해주세요.")
    @field:Size(min = 4, max = 20, message = "로그인 아이디는 4자 이상 20자 이하로 입력해주세요.")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z]).{4,20}$",
        message = "로그인 아이디에는 영문자가 최소 1개 포함되어야 합니다.",
    )
    val loginId: String,

    val email: String?,

    @field:NotBlank(message = "비밀번호를 입력해주세요.")
    @field:Size(min = 8, message = "비밀번호는 8자 이상으로 입력해주세요.")
    val password: String,

    @field:NotBlank(message = "이름을 입력해주세요.")
    val name: String,

    val phone: String?,
)
