package com.mymentalcare.server.application.auth.port

import com.mymentalcare.server.application.auth.request.*
import com.mymentalcare.server.application.auth.response.*

interface JwtTokenIssuer {
    fun issueAccessToken(memberId: Long): String

    fun issueRefreshToken(memberId: Long): String

    fun readMemberIdFromRefreshToken(refreshToken: String): Long?
}
