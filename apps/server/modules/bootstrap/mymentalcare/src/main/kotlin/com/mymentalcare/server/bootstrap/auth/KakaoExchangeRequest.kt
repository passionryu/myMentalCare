package com.mymentalcare.server.bootstrap.auth

import jakarta.validation.constraints.NotBlank

data class KakaoExchangeRequest(
    @field:NotBlank
    val code: String,
)
