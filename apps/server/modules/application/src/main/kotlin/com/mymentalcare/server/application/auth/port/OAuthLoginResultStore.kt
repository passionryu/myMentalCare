package com.mymentalcare.server.application.auth.port

import com.mymentalcare.server.application.auth.request.*
import com.mymentalcare.server.application.auth.response.*

import com.mymentalcare.server.application.auth.response.OAuthLoginResult
import java.time.Duration

interface OAuthLoginResultStore {
    fun storeResult(code: String, result: OAuthLoginResult, ttl: Duration)

    fun consumeResult(code: String): OAuthLoginResult?
}
