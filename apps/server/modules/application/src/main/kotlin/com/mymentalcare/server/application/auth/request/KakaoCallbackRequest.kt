package com.mymentalcare.server.application.auth.request

data class KakaoCallbackRequest(
    val code: String,
    val state: String,
)
