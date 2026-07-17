package com.mymentalcare.server.infrastructure.persistence.member

import com.mymentalcare.server.domain.member.MemberStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface JpaMemberRepository : JpaRepository<MemberEntity, Long> {
    fun findByLoginId(identifier: String): MemberEntity?

    fun findByLoginIdAndStatus(identifier: String, status: MemberStatus): MemberEntity?

    fun findByEmail(identifier: String): MemberEntity?

    fun findByEmailAndStatus(identifier: String, status: MemberStatus): MemberEntity?

    fun existsByLoginIdAndStatus(loginId: String, status: MemberStatus): Boolean

    fun countByStatus(status: MemberStatus): Long

    fun countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(startAt: LocalDateTime, endAt: LocalDateTime): Long
}
