package com.mymentalcare.server.application.admin.request

import java.time.LocalDate

data class AdminReportSearchRequest(
    val memberId: Long? = null,
    val date: LocalDate? = null,
    val keyword: String? = null,
    val page: Int = 0,
    val size: Int = 20,
) {
    fun normalized(): AdminReportSearchRequest {
        return copy(
            keyword = keyword?.trim()?.takeIf { it.isNotBlank() },
            page = page.coerceAtLeast(0),
            size = size.coerceIn(1, 50),
        )
    }
}

data class AdminChatMessageSearchRequest(
    val adminMemberId: Long,
    val roomId: Long,
    val reason: String?,
)
