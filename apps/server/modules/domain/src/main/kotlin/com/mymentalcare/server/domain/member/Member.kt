package com.mymentalcare.server.domain.member

data class Member(
    val id: Long,
    val loginId: String,
    val email: String?,
    val password: String,
    val name: String,
    val phone: String?,
    val status: MemberStatus = MemberStatus.ACTIVE,
)

enum class MemberStatus {
    ACTIVE,
    WITHDRAWN,
}
