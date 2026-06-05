package com.mymentalcare.server.infrastructure.persistence.member

import com.mymentalcare.server.application.port.MemberRepository
import com.mymentalcare.server.domain.member.Member
import org.springframework.stereotype.Repository

@Repository
class MemberPersistenceAdapter(
    private val jpaMemberRepository: JpaMemberRepository,
) : MemberRepository {
    override fun findByLoginIdOrEmail(identifier: String): Member? {
        return jpaMemberRepository.findByLoginId(identifier)?.toDomain()
            ?: jpaMemberRepository.findByEmail(identifier)?.toDomain()
    }

    override fun findById(memberId: Long): Member? {
        return jpaMemberRepository.findById(memberId)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun existsByLoginId(loginId: String): Boolean {
        return jpaMemberRepository.existsByLoginId(loginId)
    }

    override fun save(member: Member): Member {
        return jpaMemberRepository.save(member.toEntity()).toDomain()
    }
}
