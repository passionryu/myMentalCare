package com.mymentalcare.server.application.admin.request

import com.mymentalcare.server.domain.member.MemberStatus

data class AdminMemberSearchRequest(
    val keyword: String? = null,
    val status: MemberStatus? = null,
    val page: Int = 0,
    val size: Int = 20,
) {
    fun normalized(): AdminMemberSearchRequest {
        return copy(
            keyword = keyword?.trim()?.takeIf { it.isNotBlank() },
            page = page.coerceAtLeast(0),
            size = size.coerceIn(1, 50),
        )
    }
}

data class AdminMemberStatusChangeRequest(
    val adminMemberId: Long,
    val memberId: Long,
    val status: MemberStatus,
    val reason: String,
)
