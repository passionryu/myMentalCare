package com.mymentalcare.server.application.auth

interface KakaoAuthenticationInputPort {
    fun startLogin(request: KakaoLoginStartRequest): KakaoLoginStartResponse

    fun handleCallback(request: KakaoCallbackRequest): KakaoCallbackResponse

    fun exchange(request: KakaoExchangeRequest): SignInResponse
}
