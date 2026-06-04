package com.mymentalcare.server.application.port

import com.mymentalcare.server.domain.member.Member

interface MemberRepository {
    fun findByLoginIdOrEmail(identifier: String): Member?
}
