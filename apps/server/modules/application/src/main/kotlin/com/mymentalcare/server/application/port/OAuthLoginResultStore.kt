package com.mymentalcare.server.application.port

import com.mymentalcare.server.application.auth.OAuthLoginResult
import java.time.Duration

interface OAuthLoginResultStore {
    fun storeResult(code: String, result: OAuthLoginResult, ttl: Duration)

    fun consumeResult(code: String): OAuthLoginResult?
}
