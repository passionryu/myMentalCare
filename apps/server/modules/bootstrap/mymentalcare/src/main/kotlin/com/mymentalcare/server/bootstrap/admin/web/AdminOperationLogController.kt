package com.mymentalcare.server.bootstrap.admin.web

import com.mymentalcare.server.application.admin.port.AdminOperationLogInputPort
import com.mymentalcare.server.application.admin.request.AdminIncidentCreateRequest
import com.mymentalcare.server.application.admin.request.AdminOperationLogSearchRequest
import com.mymentalcare.server.application.admin.request.AdminOperationLogSummaryRequest
import com.mymentalcare.server.bootstrap.admin.web.request.AdminIncidentCreatePayload
import com.mymentalcare.server.bootstrap.admin.web.response.AdminIncidentCreateResponse
import com.mymentalcare.server.bootstrap.admin.web.response.AdminOperationLogPageResponse
import com.mymentalcare.server.bootstrap.admin.web.response.AdminOperationLogResponse
import com.mymentalcare.server.bootstrap.admin.web.response.AdminOperationLogSummaryResponse
import com.mymentalcare.server.domain.admin.AdminIncidentImpact
import io.swagger.v3.oas.annotations.Operation
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/admin")
class AdminOperationLogController(
    private val adminOperationLogInputPort: AdminOperationLogInputPort,
) {
    @Operation(summary = "관리자 운영 로그 목록 조회", description = "운영자가 수동 기록한 장애/에러 기록을 조회합니다.")
    @GetMapping("/operation-logs")
    fun readOperationLogs(
        @RequestParam(required = false, name = "level") impact: AdminIncidentImpact?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): AdminOperationLogPageResponse {
        return adminOperationLogInputPort.readOperationLogs(
            AdminOperationLogSearchRequest(
                impact = impact,
                keyword = keyword,
                page = page,
                size = size,
            ),
        ).toBootstrapResponse()
    }

    @Operation(summary = "관리자 운영 로그 요약 조회", description = "지정 기간의 장애/에러 기록 요약을 조회합니다.")
    @GetMapping("/operation-logs/summary")
    fun readOperationLogSummary(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
    ): AdminOperationLogSummaryResponse {
        return adminOperationLogInputPort.readOperationLogSummary(AdminOperationLogSummaryRequest(from = from, to = to)).toBootstrapResponse()
    }

    @Operation(summary = "관리자 장애 기록 생성", description = "운영자가 장애 또는 에러 상황을 수동 기록합니다.")
    @PostMapping("/incidents")
    fun createIncident(
        @AuthenticationPrincipal adminMemberId: Long,
        @RequestBody payload: AdminIncidentCreatePayload,
    ): AdminIncidentCreateResponse {
        return adminOperationLogInputPort.createIncident(
            AdminIncidentCreateRequest(
                adminMemberId = adminMemberId,
                title = payload.title,
                description = payload.description,
                impact = payload.impact,
                action = payload.action,
            ),
        ).toBootstrapResponse()
    }
}

private fun com.mymentalcare.server.application.admin.response.AdminOperationLogPageResponse.toBootstrapResponse(): AdminOperationLogPageResponse {
    return AdminOperationLogPageResponse(
        logs = logs.map {
            AdminOperationLogResponse(
                id = it.id,
                level = it.level,
                title = it.title,
                description = it.description,
                action = it.action,
                createdByMemberId = it.createdByMemberId,
                createdAt = it.createdAt,
            )
        },
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )
}

private fun com.mymentalcare.server.application.admin.response.AdminOperationLogSummaryResponse.toBootstrapResponse(): AdminOperationLogSummaryResponse {
    return AdminOperationLogSummaryResponse(
        from = from,
        to = to,
        totalIncidents = totalIncidents,
        highImpactIncidents = highImpactIncidents,
        criticalIncidents = criticalIncidents,
        actionRequiredIncidents = actionRequiredIncidents,
        latestIncidentAt = latestIncidentAt,
    )
}

private fun com.mymentalcare.server.application.admin.response.AdminIncidentCreateResponse.toBootstrapResponse(): AdminIncidentCreateResponse {
    return AdminIncidentCreateResponse(
        incidentId = incidentId,
        createdAt = createdAt,
    )
}
