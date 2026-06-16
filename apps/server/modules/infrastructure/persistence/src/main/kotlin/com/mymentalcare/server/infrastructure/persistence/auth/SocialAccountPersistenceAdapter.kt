package com.mymentalcare.server.infrastructure.persistence.auth

import com.mymentalcare.server.application.port.SocialAccountRepository
import com.mymentalcare.server.domain.auth.OAuthProvider
import com.mymentalcare.server.domain.auth.SocialAccount
import org.springframework.stereotype.Repository

@Repository
class SocialAccountPersistenceAdapter(
    private val jpaSocialAccountRepository: JpaSocialAccountRepository,
) : SocialAccountRepository {
    override fun findByProviderAndProviderUserId(provider: OAuthProvider, providerUserId: String): SocialAccount? {
        return jpaSocialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)?.toDomain()
    }

    override fun findByMemberId(memberId: Long): List<SocialAccount> {
        return jpaSocialAccountRepository.findByMemberId(memberId).map { it.toDomain() }
    }

    override fun save(socialAccount: SocialAccount): SocialAccount {
        return jpaSocialAccountRepository.save(socialAccount.toEntity()).toDomain()
    }
}
