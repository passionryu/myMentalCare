package com.mymentalcare.server.infrastructure.persistence.member

import com.mymentalcare.server.domain.member.Member
import com.mymentalcare.server.domain.member.MemberStatus
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
@Table(name = "members")
class MemberEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "login_id", nullable = false, unique = true)
    val loginId: String,

    @Column(name = "email", nullable = true, unique = true)
    val email: String?,

    @Column(name = "password", nullable = false)
    val password: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "phone", nullable = true)
    val phone: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: MemberStatus = MemberStatus.ACTIVE,

    @Column(name = "deleted_at", nullable = true)
    val deletedAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): Member {
        return Member(
            id = id,
            loginId = loginId,
            email = email,
            password = password,
            name = name,
            phone = phone,
            status = status,
        )
    }
}

fun Member.toEntity(
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
    deletedAt: LocalDateTime? = null,
): MemberEntity {
    return MemberEntity(
        id = id,
        loginId = loginId,
        email = email,
        password = password,
        name = name,
        phone = phone,
        status = status,
        deletedAt = deletedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
