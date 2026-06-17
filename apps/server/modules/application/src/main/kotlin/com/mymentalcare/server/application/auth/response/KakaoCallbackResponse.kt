package com.mymentalcare.server.application.auth.response

data class KakaoCallbackResponse(
    val oneTimeCode: String,
    val redirectTo: String,
)
