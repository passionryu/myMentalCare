package com.mymentalcare.server.infrastructure.persistence.auth

import com.mymentalcare.server.domain.auth.OAuthProvider
import com.mymentalcare.server.domain.auth.SocialAccount
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "social_accounts")
class SocialAccountEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    val provider: OAuthProvider,

    @Column(name = "provider_user_id", nullable = false)
    val providerUserId: String,

    @Column(name = "email", nullable = true)
    val email: String?,

    @Column(name = "linked_at", nullable = false)
    val linkedAt: LocalDateTime,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): SocialAccount {
        return SocialAccount(
            id = id,
            memberId = memberId,
            provider = provider,
            providerUserId = providerUserId,
            email = email,
            linkedAt = linkedAt,
        )
    }
}

fun SocialAccount.toEntity(): SocialAccountEntity {
    return SocialAccountEntity(
        id = id,
        memberId = memberId,
        provider = provider,
        providerUserId = providerUserId,
        email = email,
        linkedAt = linkedAt,
    )
}
