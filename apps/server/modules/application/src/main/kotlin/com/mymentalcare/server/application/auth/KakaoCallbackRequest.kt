package com.mymentalcare.server.application.auth

data class KakaoCallbackRequest(
    val code: String,
    val state: String,
)
