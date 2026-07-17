package com.mymentalcare.server.bootstrap.admin.web.request

import com.mymentalcare.server.domain.admin.AdminIncidentImpact

data class AdminIncidentCreatePayload(
    val title: String,
    val description: String?,
    val impact: AdminIncidentImpact,
    val action: String?,
)
