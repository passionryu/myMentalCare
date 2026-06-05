package com.mymentalcare.server.bootstrap.auth

import jakarta.validation.constraints.NotBlank

data class ReissueTokenRequest(
    @field:NotBlank(message = "리프레시 토큰을 입력해주세요.")
    val refreshToken: String,
)
