package com.mymentalcare.server.domain.auth

import java.time.LocalDateTime

data class SocialAccount(
    val id: Long,
    val memberId: Long,
    val provider: OAuthProvider,
    val providerUserId: String,
    val email: String?,
    val linkedAt: LocalDateTime,
)
