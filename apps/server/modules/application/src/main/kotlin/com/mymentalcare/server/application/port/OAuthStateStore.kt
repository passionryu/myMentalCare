package com.mymentalcare.server.application.port

import com.mymentalcare.server.application.auth.OAuthLoginState
import java.time.Duration

interface OAuthStateStore {
    fun storeState(state: OAuthLoginState, ttl: Duration)

    fun consumeState(state: String): OAuthLoginState?
}
