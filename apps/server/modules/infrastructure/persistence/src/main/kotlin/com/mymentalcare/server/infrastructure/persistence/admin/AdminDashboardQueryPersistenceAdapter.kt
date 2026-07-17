package com.mymentalcare.server.infrastructure.persistence.admin

import com.mymentalcare.server.application.admin.port.AdminDashboardQueryRepository
import com.mymentalcare.server.domain.member.MemberStatus
import com.mymentalcare.server.infrastructure.persistence.aichat.JpaAiChatReportRepository
import com.mymentalcare.server.infrastructure.persistence.aichat.JpaAiChatRoomRepository
import com.mymentalcare.server.infrastructure.persistence.member.JpaMemberRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class AdminDashboardQueryPersistenceAdapter(
    private val jpaMemberRepository: JpaMemberRepository,
    private val jpaAiChatRoomRepository: JpaAiChatRoomRepository,
    private val jpaAiChatReportRepository: JpaAiChatReportRepository,
) : AdminDashboardQueryRepository {
    override fun countActiveMembers(): Long {
        return jpaMemberRepository.countByStatus(MemberStatus.ACTIVE)
    }

    override fun countMembersCreatedOn(date: LocalDate): Long {
        val startAt = date.atStartOfDay()
        val endAt = date.plusDays(1).atStartOfDay()

        return jpaMemberRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(startAt, endAt)
    }

    override fun countConversationsOn(date: LocalDate): Long {
        return jpaAiChatRoomRepository.countByConversationDate(date)
    }

    override fun countReportsOn(date: LocalDate): Long {
        return jpaAiChatReportRepository.countByConversationDate(date)
    }
}
