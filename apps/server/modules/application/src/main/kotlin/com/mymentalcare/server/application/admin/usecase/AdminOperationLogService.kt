package com.mymentalcare.server.application.admin.usecase

import com.mymentalcare.server.application.admin.AdminIncidentInvalidRequestException
import com.mymentalcare.server.application.admin.port.AdminIncidentPage
import com.mymentalcare.server.application.admin.port.AdminOperationLogInputPort
import com.mymentalcare.server.application.admin.port.AdminOperationLogRepository
import com.mymentalcare.server.application.admin.recorder.AdminAuditLogRecorder
import com.mymentalcare.server.application.admin.request.AdminAuditLogRecordRequest
import com.mymentalcare.server.application.admin.request.AdminIncidentCreateRequest
import com.mymentalcare.server.application.admin.request.AdminOperationLogSearchRequest
import com.mymentalcare.server.application.admin.request.AdminOperationLogSummaryRequest
import com.mymentalcare.server.application.admin.response.AdminIncidentCreateResponse
import com.mymentalcare.server.application.admin.response.AdminOperationLogPageResponse
import com.mymentalcare.server.application.admin.response.AdminOperationLogResponse
import com.mymentalcare.server.application.admin.response.AdminOperationLogSummaryResponse
import com.mymentalcare.server.application.member.MemberNotFoundException
import com.mymentalcare.server.application.member.port.MemberRepository
import com.mymentalcare.server.domain.admin.AdminAuditLogAction
import com.mymentalcare.server.domain.admin.AdminAuditLogTargetType
import com.mymentalcare.server.domain.admin.AdminIncident
import com.mymentalcare.server.domain.admin.AdminIncidentImpact
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

private val OPERATION_LOG_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")

@Service
class AdminOperationLogService(
    private val adminOperationLogRepository: AdminOperationLogRepository,
    private val memberRepository: MemberRepository,
    private val adminAuditLogRecorder: AdminAuditLogRecorder,
) : AdminOperationLogInputPort {
    @Transactional(readOnly = true)
    override fun readOperationLogs(request: AdminOperationLogSearchRequest): AdminOperationLogPageResponse {
        val normalizedRequest = request.normalized()

        return adminOperationLogRepository.findIncidents(
            impact = normalizedRequest.impact,
            keyword = normalizedRequest.keyword,
            page = normalizedRequest.page,
            size = normalizedRequest.size,
        ).toPageResponse()
    }

    @Transactional(readOnly = true)
    override fun readOperationLogSummary(request: AdminOperationLogSummaryRequest): AdminOperationLogSummaryResponse {
        val today = LocalDate.now(OPERATION_LOG_ZONE_ID)
        val from = request.from ?: today.minusDays(6)
        val to = request.to ?: today
        val incidents = adminOperationLogRepository.findIncidentsBetween(
            startAt = from.atStartOfDay(),
            endAt = to.plusDays(1).atStartOfDay(),
        )

        return AdminOperationLogSummaryResponse(
            from = from,
            to = to,
            totalIncidents = incidents.size,
            highImpactIncidents = incidents.count { it.impact == AdminIncidentImpact.HIGH },
            criticalIncidents = incidents.count { it.impact == AdminIncidentImpact.CRITICAL },
            actionRequiredIncidents = incidents.count { it.impact == AdminIncidentImpact.HIGH || it.impact == AdminIncidentImpact.CRITICAL },
            latestIncidentAt = incidents.maxByOrNull { it.createdAt ?: java.time.LocalDateTime.MIN }?.createdAt,
        )
    }

    @Transactional
    override fun createIncident(request: AdminIncidentCreateRequest): AdminIncidentCreateResponse {
        val adminMember = memberRepository.findById(request.adminMemberId) ?: throw MemberNotFoundException()
        val title = request.title.trim()
        if (title.isBlank()) {
            throw AdminIncidentInvalidRequestException("장애 기록 제목이 필요합니다.")
        }

        val savedIncident = adminOperationLogRepository.saveIncident(
            AdminIncident(
                title = title,
                description = request.description?.trim()?.takeIf { it.isNotBlank() },
                impact = request.impact,
                action = request.action?.trim()?.takeIf { it.isNotBlank() },
                createdByMemberId = adminMember.id,
            ),
        )

        adminAuditLogRecorder.record(
            AdminAuditLogRecordRequest(
                adminMemberId = adminMember.id,
                adminLoginId = adminMember.loginId,
                action = AdminAuditLogAction.SYSTEM_INCIDENT_CREATE,
                targetType = AdminAuditLogTargetType.SYSTEM,
                targetId = savedIncident.id,
                reason = "${savedIncident.impact.name}: ${savedIncident.title}",
            ),
        )

        return AdminIncidentCreateResponse(
            incidentId = savedIncident.id,
            createdAt = savedIncident.createdAt,
        )
    }
}

private fun AdminIncidentPage.toPageResponse(): AdminOperationLogPageResponse {
    return AdminOperationLogPageResponse(
        logs = incidents.map {
            AdminOperationLogResponse(
                id = it.id,
                level = it.impact.name,
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
