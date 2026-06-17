package com.mymentalcare.server.application.member.port

import com.mymentalcare.server.application.member.request.*
import com.mymentalcare.server.application.member.response.*

import com.mymentalcare.server.domain.auth.OAuthProvider
import com.mymentalcare.server.domain.auth.SocialAccount

interface SocialAccountRepository {
    fun findByProviderAndProviderUserId(provider: OAuthProvider, providerUserId: String): SocialAccount?

    fun findByMemberId(memberId: Long): List<SocialAccount>

    fun save(socialAccount: SocialAccount): SocialAccount
}
