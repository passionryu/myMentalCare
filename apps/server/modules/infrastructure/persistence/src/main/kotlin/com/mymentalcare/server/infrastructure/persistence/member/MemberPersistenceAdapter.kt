package com.mymentalcare.server.infrastructure.persistence.member

import com.mymentalcare.server.application.port.MemberRepository
import com.mymentalcare.server.domain.member.Member
import com.mymentalcare.server.domain.member.MemberStatus
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class MemberPersistenceAdapter(
    private val jpaMemberRepository: JpaMemberRepository,
) : MemberRepository {
    override fun findByLoginIdOrEmail(identifier: String): Member? {
        return jpaMemberRepository.findByLoginIdAndStatus(identifier, MemberStatus.ACTIVE)?.toDomain()
            ?: jpaMemberRepository.findByEmailAndStatus(identifier, MemberStatus.ACTIVE)?.toDomain()
    }

    override fun findById(memberId: Long): Member? {
        return jpaMemberRepository.findById(memberId)
            .filter { it.status == MemberStatus.ACTIVE }
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByEmail(email: String): Member? {
        return jpaMemberRepository.findByEmailAndStatus(email, MemberStatus.ACTIVE)?.toDomain()
    }

    override fun existsByLoginId(loginId: String): Boolean {
        return jpaMemberRepository.existsByLoginIdAndStatus(loginId, MemberStatus.ACTIVE)
    }

    override fun save(member: Member): Member {
        val existingMember = member.id.takeIf { it > 0 }
            ?.let { jpaMemberRepository.findById(it).orElse(null) }
        val now = LocalDateTime.now()

        return jpaMemberRepository.save(
            member.toEntity(
                createdAt = existingMember?.createdAt ?: now,
                updatedAt = now,
            )
        ).toDomain()
    }

    override fun withdraw(member: Member): Member {
        val currentMember = jpaMemberRepository.findById(member.id).orElse(null)
            ?: return member
        val now = LocalDateTime.now()

        return jpaMemberRepository.save(
            member.copy(
                loginId = "wd_${member.id}_${now.toEpochSecond(java.time.ZoneOffset.UTC)}",
                email = null,
                name = "탈퇴회원",
                phone = null,
                status = MemberStatus.WITHDRAWN,
            ).toEntity(
                createdAt = currentMember.createdAt,
                updatedAt = now,
                deletedAt = now,
            )
        ).toDomain()
    }
}
