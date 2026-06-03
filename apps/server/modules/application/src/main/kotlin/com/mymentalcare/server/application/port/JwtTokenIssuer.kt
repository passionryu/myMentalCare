package com.mymentalcare.server.application.port

interface JwtTokenIssuer {
    fun issueAccessToken(memberId: Long): String

    fun issueRefreshToken(memberId: Long): String
}
