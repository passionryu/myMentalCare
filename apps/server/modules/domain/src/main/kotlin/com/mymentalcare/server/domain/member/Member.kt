package com.mymentalcare.server.domain.member

data class Member(
    val id: Long,
    val loginId: String,
    val email: String?,
    val password: String,
    val name: String,
    val phone: String?,
    val status: MemberStatus = MemberStatus.ACTIVE,
    val role: MemberRole = MemberRole.USER,
)

enum class MemberStatus {
    ACTIVE,
    WITHDRAWN,
}

enum class MemberRole {
    USER,
    ADMIN,
}
