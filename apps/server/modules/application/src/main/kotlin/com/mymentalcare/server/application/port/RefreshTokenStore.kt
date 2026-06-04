package com.mymentalcare.server.application.port

interface RefreshTokenStore {
    fun storeRefreshToken(memberId: Long, refreshToken: String)

    fun readRefreshToken(memberId: Long): String?
}
