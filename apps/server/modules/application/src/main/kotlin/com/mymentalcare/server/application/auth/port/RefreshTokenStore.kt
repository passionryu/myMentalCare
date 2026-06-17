package com.mymentalcare.server.application.auth.port

import com.mymentalcare.server.application.auth.request.*
import com.mymentalcare.server.application.auth.response.*

interface RefreshTokenStore {
    fun storeRefreshToken(memberId: Long, refreshToken: String)

    fun readRefreshToken(memberId: Long): String?

    fun deleteRefreshToken(memberId: Long)
}
