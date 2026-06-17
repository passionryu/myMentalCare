package com.mymentalcare.server.bootstrap.auth.web.request

import jakarta.validation.constraints.NotBlank

data class KakaoExchangeRequest(
    @field:NotBlank
    val code: String,
)
