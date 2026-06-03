package com.mymentalcare.server.bootstrap.auth

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "로그인 ID 또는 이메일을 입력해주세요.")
    val identifier: String,

    @field:NotBlank(message = "비밀번호를 입력해주세요.")
    val password: String,
)
