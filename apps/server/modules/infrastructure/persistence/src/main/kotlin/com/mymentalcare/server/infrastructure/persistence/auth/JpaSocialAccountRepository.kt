package com.mymentalcare.server.infrastructure.persistence.auth

import com.mymentalcare.server.domain.auth.OAuthProvider
import org.springframework.data.jpa.repository.JpaRepository

interface JpaSocialAccountRepository : JpaRepository<SocialAccountEntity, Long> {
    fun findByProviderAndProviderUserId(provider: OAuthProvider, providerUserId: String): SocialAccountEntity?

    fun findByMemberId(memberId: Long): List<SocialAccountEntity>
}
