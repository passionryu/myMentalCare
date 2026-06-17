package com.mymentalcare.server.application.auth.port

import com.mymentalcare.server.application.auth.request.*
import com.mymentalcare.server.application.auth.response.*

interface AuthenticationInputPort {
    fun signIn(request: SignInRequest): SignInResponse

    fun reissue(request: ReissueTokenRequest): SignInResponse
}
