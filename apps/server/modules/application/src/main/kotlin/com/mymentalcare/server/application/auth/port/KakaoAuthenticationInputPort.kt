package com.mymentalcare.server.application.auth.port

import com.mymentalcare.server.application.auth.request.*
import com.mymentalcare.server.application.auth.response.*

interface KakaoAuthenticationInputPort {
    fun startLogin(request: KakaoLoginStartRequest): KakaoLoginStartResponse

    fun handleCallback(request: KakaoCallbackRequest): KakaoCallbackResponse

    fun exchange(request: KakaoExchangeRequest): SignInResponse
}
