package com.mymentalcare.server.infrastructure.persistence.aichat

import com.mymentalcare.server.application.port.AiChatReportRepository
import com.mymentalcare.server.domain.aichat.AiChatReport
import org.springframework.stereotype.Repository

@Repository
class AiChatReportPersistenceAdapter(
    private val jpaAiChatReportRepository: JpaAiChatReportRepository,
    private val jpaAiChatReportSongRepository: JpaAiChatReportSongRepository,
) : AiChatReportRepository {
    override fun findLatestByRoomId(roomId: Long): AiChatReport? {
        return jpaAiChatReportRepository.findTopByRoomIdOrderByCreatedAtDesc(roomId)
            ?.let { it.toDomain(readSongs(it.id)) }
    }

    override fun findLatestByMemberId(memberId: Long): AiChatReport? {
        return jpaAiChatReportRepository.findTopByMemberIdOrderByCreatedAtDesc(memberId)
            ?.let { it.toDomain(readSongs(it.id)) }
    }

    override fun countByMemberId(memberId: Long): Int {
        return jpaAiChatReportRepository.countByMemberId(memberId)
    }

    override fun findByMemberId(memberId: Long): List<AiChatReport> {
        return jpaAiChatReportRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
            .map { it.toDomain(readSongs(it.id)) }
    }

    override fun findByIdAndMemberId(reportId: Long, memberId: Long): AiChatReport? {
        return jpaAiChatReportRepository.findByIdAndMemberId(reportId, memberId)
            ?.let { it.toDomain(readSongs(it.id)) }
    }

    override fun findByRoomIdAndClientRequestId(roomId: Long, clientRequestId: String): AiChatReport? {
        return jpaAiChatReportRepository.findByRoomIdAndClientRequestId(roomId, clientRequestId)
            ?.let { it.toDomain(readSongs(it.id)) }
    }

    override fun save(report: AiChatReport): AiChatReport {
        val savedReport = jpaAiChatReportRepository.save(report.toEntity())
        val savedSongs = report.songs
            .map { it.toEntity(savedReport.id) }
            .map { jpaAiChatReportSongRepository.save(it).toDomain() }

        return savedReport.toDomain(savedSongs)
    }

    private fun readSongs(reportId: Long) = jpaAiChatReportSongRepository.findByReportIdOrderBySongOrderAsc(reportId)
        .map { it.toDomain() }
}
