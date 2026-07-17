package com.mymentalcare.server.bootstrap.admin.web.request

import com.mymentalcare.server.domain.member.MemberStatus

data class AdminMemberStatusChangePayload(
    val status: MemberStatus,
    val reason: String,
)
