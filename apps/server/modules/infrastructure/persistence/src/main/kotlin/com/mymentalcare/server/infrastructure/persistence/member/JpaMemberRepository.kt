package com.mymentalcare.server.infrastructure.persistence.member

import com.mymentalcare.server.domain.member.MemberStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface JpaMemberRepository : JpaRepository<MemberEntity, Long> {
    fun findByLoginId(identifier: String): MemberEntity?

    fun findByLoginIdAndStatus(identifier: String, status: MemberStatus): MemberEntity?

    fun findByEmail(identifier: String): MemberEntity?

    fun findByEmailAndStatus(identifier: String, status: MemberStatus): MemberEntity?

    fun existsByLoginIdAndStatus(loginId: String, status: MemberStatus): Boolean

    fun countByStatus(status: MemberStatus): Long

    fun countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(startAt: LocalDateTime, endAt: LocalDateTime): Long

    @Query(
        """
        select member
        from MemberEntity member
        where (:status is null or member.status = :status)
          and (
            :keywordLike is null
            or lower(member.loginId) like :keywordLike
            or lower(member.name) like :keywordLike
            or lower(coalesce(member.email, '')) like :keywordLike
            or member.id = :keywordMemberId
          )
        order by member.createdAt desc, member.id desc
        """,
    )
    fun findForAdmin(
        @Param("keywordLike") keywordLike: String?,
        @Param("keywordMemberId") keywordMemberId: Long?,
        @Param("status") status: MemberStatus?,
        pageable: Pageable,
    ): Page<MemberEntity>
}
