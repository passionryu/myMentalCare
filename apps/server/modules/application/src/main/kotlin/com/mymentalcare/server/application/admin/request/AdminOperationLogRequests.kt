package com.mymentalcare.server.application.admin.request

import com.mymentalcare.server.domain.admin.AdminIncidentImpact
import java.time.LocalDate

data class AdminOperationLogSearchRequest(
    val impact: AdminIncidentImpact? = null,
    val keyword: String? = null,
    val page: Int = 0,
    val size: Int = 20,
) {
    fun normalized(): AdminOperationLogSearchRequest {
        return copy(
            keyword = keyword?.trim()?.takeIf { it.isNotBlank() },
            page = page.coerceAtLeast(0),
            size = size.coerceIn(1, 50),
        )
    }
}

data class AdminOperationLogSummaryRequest(
    val from: LocalDate? = null,
    val to: LocalDate? = null,
)

data class AdminIncidentCreateRequest(
    val adminMemberId: Long,
    val title: String,
    val description: String?,
    val impact: AdminIncidentImpact,
    val action: String?,
)
