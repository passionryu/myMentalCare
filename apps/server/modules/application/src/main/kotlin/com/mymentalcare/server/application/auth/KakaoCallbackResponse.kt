package com.mymentalcare.server.application.auth

data class KakaoCallbackResponse(
    val oneTimeCode: String,
    val redirectTo: String,
)
