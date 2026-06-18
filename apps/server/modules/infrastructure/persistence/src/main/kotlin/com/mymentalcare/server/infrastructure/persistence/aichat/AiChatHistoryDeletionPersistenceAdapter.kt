package com.mymentalcare.server.infrastructure.persistence.aichat

import com.mymentalcare.server.application.aichat.port.AiChatHistoryDeletionRepository
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository

@Repository
class AiChatHistoryDeletionPersistenceAdapter(
    private val entityManager: EntityManager,
) : AiChatHistoryDeletionRepository {
    override fun deleteChatRoom(memberId: Long, roomId: Long): Int {
        if (!existsOwnedRoom(memberId, roomId)) {
            return 0
        }

        var deletedCount = 0
        deletedCount += execute(
            "DELETE FROM ai_chat_report_songs WHERE report_id IN (SELECT id FROM ai_chat_reports WHERE room_id = :roomId AND member_id = :memberId)",
            mapOf("memberId" to memberId, "roomId" to roomId),
        )
        deletedCount += execute(
            "DELETE FROM ai_chat_reports WHERE room_id = :roomId AND member_id = :memberId",
            mapOf("memberId" to memberId, "roomId" to roomId),
        )
        deletedCount += execute(
            """
            DELETE FROM ai_chat_check_ins
            WHERE segment_id IN (
                SELECT id FROM ai_chat_segments
                WHERE room_id IN (
                    SELECT id FROM ai_chat_rooms WHERE id = :roomId AND member_id = :memberId
                )
            )
            """.trimIndent(),
            mapOf("memberId" to memberId, "roomId" to roomId),
        )
        deletedCount += execute(
            "DELETE FROM crisis_detection_events WHERE room_id = :roomId AND member_id = :memberId",
            mapOf("memberId" to memberId, "roomId" to roomId),
        )
        deletedCount += execute(
            "DELETE FROM ai_chat_room_summaries WHERE room_id = :roomId AND member_id = :memberId",
            mapOf("memberId" to memberId, "roomId" to roomId),
        )
        deletedCount += execute(
            "DELETE FROM chat_messages WHERE room_id IN (SELECT id FROM ai_chat_rooms WHERE id = :roomId AND member_id = :memberId)",
            mapOf("memberId" to memberId, "roomId" to roomId),
        )
        deletedCount += execute(
            "DELETE FROM ai_chat_segments WHERE room_id IN (SELECT id FROM ai_chat_rooms WHERE id = :roomId AND member_id = :memberId)",
            mapOf("memberId" to memberId, "roomId" to roomId),
        )
        deletedCount += execute(
            "DELETE FROM ai_chat_rooms WHERE id = :roomId AND member_id = :memberId",
            mapOf("memberId" to memberId, "roomId" to roomId),
        )
        return deletedCount
    }

    override fun deleteReport(memberId: Long, reportId: Long): Int {
        val deletedSongs = entityManager.createNativeQuery(
            "DELETE FROM ai_chat_report_songs WHERE report_id IN (SELECT id FROM ai_chat_reports WHERE id = :reportId AND member_id = :memberId)"
        )
            .setParameter("reportId", reportId)
            .setParameter("memberId", memberId)
            .executeUpdate()
        val deletedReports = entityManager.createNativeQuery("DELETE FROM ai_chat_reports WHERE id = :reportId AND member_id = :memberId")
            .setParameter("reportId", reportId)
            .setParameter("memberId", memberId)
            .executeUpdate()

        return deletedSongs + deletedReports
    }

    override fun deleteCheckIn(memberId: Long, checkInId: Long): Int {
        return entityManager.createNativeQuery(
            """
            DELETE FROM ai_chat_check_ins
            WHERE id = :checkInId
              AND segment_id IN (
                SELECT segment_id FROM (
                    SELECT c.segment_id
                    FROM ai_chat_check_ins c
                    JOIN ai_chat_segments s ON c.segment_id = s.id
                    JOIN ai_chat_rooms r ON s.room_id = r.id
                    WHERE c.id = :checkInId AND r.member_id = :memberId
                ) owned_check_ins
              )
            """.trimIndent()
        )
            .setParameter("checkInId", checkInId)
            .setParameter("memberId", memberId)
            .executeUpdate()
    }

    private fun execute(sql: String, parameters: Map<String, Any>): Int {
        val query = entityManager.createNativeQuery(sql)
        parameters.forEach { (name, value) ->
            query.setParameter(name, value)
        }
        return query.executeUpdate()
    }

    private fun existsOwnedRoom(memberId: Long, roomId: Long): Boolean {
        val count = entityManager.createNativeQuery("SELECT COUNT(*) FROM ai_chat_rooms WHERE id = :roomId AND member_id = :memberId")
            .setParameter("roomId", roomId)
            .setParameter("memberId", memberId)
            .singleResult as Number

        return count.toLong() > 0
    }
}
