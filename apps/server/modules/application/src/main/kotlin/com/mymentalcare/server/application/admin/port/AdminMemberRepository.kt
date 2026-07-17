package com.mymentalcare.server.application.admin.port

import com.mymentalcare.server.domain.member.MemberRole
import com.mymentalcare.server.domain.member.MemberStatus
import java.time.LocalDateTime

interface AdminMemberRepository {
    fun findMembers(
        keyword: String?,
        status: MemberStatus?,
        page: Int,
        size: Int,
    ): AdminMemberPage

    fun findMemberById(memberId: Long): AdminMemberRecord?

    fun changeStatus(memberId: Long, status: MemberStatus): AdminMemberRecord?
}

data class AdminMemberPage(
    val members: List<AdminMemberRecord>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

data class AdminMemberRecord(
    val id: Long,
    val loginId: String,
    val email: String?,
    val name: String,
    val phone: String?,
    val status: MemberStatus,
    val role: MemberRole,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val deletedAt: LocalDateTime?,
)
