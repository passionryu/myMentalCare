package com.mymentalcare.server.application.port

import com.mymentalcare.server.domain.auth.OAuthProvider
import com.mymentalcare.server.domain.auth.SocialAccount

interface SocialAccountRepository {
    fun findByProviderAndProviderUserId(provider: OAuthProvider, providerUserId: String): SocialAccount?

    fun save(socialAccount: SocialAccount): SocialAccount
}
