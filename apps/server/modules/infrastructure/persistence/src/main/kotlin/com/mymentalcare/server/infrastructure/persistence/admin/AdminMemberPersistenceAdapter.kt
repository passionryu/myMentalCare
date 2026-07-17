package com.mymentalcare.server.infrastructure.persistence.admin

import com.mymentalcare.server.application.admin.port.AdminMemberPage
import com.mymentalcare.server.application.admin.port.AdminMemberRecord
import com.mymentalcare.server.application.admin.port.AdminMemberRepository
import com.mymentalcare.server.domain.member.MemberStatus
import com.mymentalcare.server.infrastructure.persistence.member.JpaMemberRepository
import com.mymentalcare.server.infrastructure.persistence.member.MemberEntity
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneOffset

@Repository
class AdminMemberPersistenceAdapter(
    private val jpaMemberRepository: JpaMemberRepository,
) : AdminMemberRepository {
    override fun findMembers(
        keyword: String?,
        status: MemberStatus?,
        page: Int,
        size: Int,
    ): AdminMemberPage {
        val result = jpaMemberRepository.findForAdmin(
            keywordLike = keyword?.lowercase()?.let { "%$it%" },
            keywordMemberId = keyword?.toLongOrNull(),
            status = status,
            pageable = PageRequest.of(page, size),
        )

        return AdminMemberPage(
            members = result.content.map { it.toAdminMemberRecord() },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    override fun findMemberById(memberId: Long): AdminMemberRecord? {
        return jpaMemberRepository.findById(memberId)
            .map { it.toAdminMemberRecord() }
            .orElse(null)
    }

    override fun changeStatus(memberId: Long, status: MemberStatus): AdminMemberRecord? {
        val currentMember = jpaMemberRepository.findById(memberId).orElse(null)
            ?: return null
        val now = LocalDateTime.now()
        val nextMember = when (status) {
            MemberStatus.WITHDRAWN -> currentMember.toWithdrawnEntity(now)
            else -> currentMember.copyForAdminStatus(status = status, updatedAt = now, deletedAt = null)
        }

        return jpaMemberRepository.save(nextMember).toAdminMemberRecord()
    }
}

private fun MemberEntity.toAdminMemberRecord(): AdminMemberRecord {
    return AdminMemberRecord(
        id = id,
        loginId = loginId,
        email = email,
        name = name,
        phone = phone,
        status = status,
        role = role,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
}

private fun MemberEntity.copyForAdminStatus(
    status: MemberStatus,
    updatedAt: LocalDateTime,
    deletedAt: LocalDateTime?,
): MemberEntity {
    return MemberEntity(
        id = id,
        loginId = loginId,
        email = email,
        password = password,
        name = name,
        phone = phone,
        status = status,
        role = role,
        deletedAt = deletedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

private fun MemberEntity.toWithdrawnEntity(now: LocalDateTime): MemberEntity {
    return MemberEntity(
        id = id,
        loginId = "wd_${id}_${now.toEpochSecond(ZoneOffset.UTC)}",
        email = null,
        password = password,
        name = "탈퇴회원",
        phone = null,
        status = MemberStatus.WITHDRAWN,
        role = role,
        deletedAt = now,
        createdAt = createdAt,
        updatedAt = now,
    )
}
