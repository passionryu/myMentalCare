package com.mymentalcare.server.infrastructure.persistence.member

import org.springframework.data.jpa.repository.JpaRepository

interface JpaMemberRepository : JpaRepository<MemberEntity, Long> {
    fun findByLoginId(identifier: String): MemberEntity?

    fun findByEmail(identifier: String): MemberEntity?

    fun existsByLoginId(loginId: String): Boolean
}
