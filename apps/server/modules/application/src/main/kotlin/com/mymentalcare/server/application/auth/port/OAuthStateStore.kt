package com.mymentalcare.server.application.auth.port

import com.mymentalcare.server.application.auth.request.*
import com.mymentalcare.server.application.auth.response.*

import com.mymentalcare.server.application.auth.response.OAuthLoginState
import java.time.Duration

interface OAuthStateStore {
    fun storeState(state: OAuthLoginState, ttl: Duration)

    fun consumeState(state: String): OAuthLoginState?
}
