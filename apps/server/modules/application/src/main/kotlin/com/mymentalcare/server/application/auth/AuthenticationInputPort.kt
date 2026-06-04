package com.mymentalcare.server.application.auth

interface AuthenticationInputPort {
    fun signIn(request: SignInRequest): SignInResponse

    fun reissue(request: ReissueTokenRequest): SignInResponse
}
