package com.mymentalcare.server.infrastructure.persistence.admin

import com.mymentalcare.server.application.admin.port.AdminInquiryPage
import com.mymentalcare.server.application.admin.port.AdminInquiryRecord
import com.mymentalcare.server.application.admin.port.AdminInquiryRepository
import com.mymentalcare.server.domain.inquiry.InquiryStatus
import com.mymentalcare.server.infrastructure.persistence.inquiry.InquiryEntity
import com.mymentalcare.server.infrastructure.persistence.inquiry.JpaInquiryRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class AdminInquiryPersistenceAdapter(
    private val jpaInquiryRepository: JpaInquiryRepository,
) : AdminInquiryRepository {
    override fun findInquiries(keyword: String?, status: InquiryStatus?, page: Int, size: Int): AdminInquiryPage {
        val result = jpaInquiryRepository.findForAdmin(
            keywordLike = keyword?.lowercase()?.let { "%$it%" },
            keywordId = keyword?.toLongOrNull(),
            status = status,
            pageable = PageRequest.of(page, size),
        )

        return AdminInquiryPage(
            inquiries = result.content.map { it.toAdminInquiryRecord() },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    override fun findInquiryById(inquiryId: Long): AdminInquiryRecord? {
        return jpaInquiryRepository.findById(inquiryId)
            .map { it.toAdminInquiryRecord() }
            .orElse(null)
    }

    override fun changeStatus(
        inquiryId: Long,
        status: InquiryStatus,
        adminMemo: String?,
        handledByMemberId: Long,
    ): AdminInquiryRecord? {
        val currentInquiry = jpaInquiryRepository.findById(inquiryId).orElse(null)
            ?: return null
        val now = LocalDateTime.now()

        return jpaInquiryRepository.save(
            currentInquiry.copyForAdmin(
                status = status,
                adminMemo = adminMemo,
                handledByMemberId = handledByMemberId,
                handledAt = now,
                updatedAt = now,
            ),
        ).toAdminInquiryRecord()
    }

    override fun updateMemo(inquiryId: Long, adminMemo: String?): AdminInquiryRecord? {
        val currentInquiry = jpaInquiryRepository.findById(inquiryId).orElse(null)
            ?: return null

        return jpaInquiryRepository.save(
            currentInquiry.copyForAdmin(
                adminMemo = adminMemo,
                updatedAt = LocalDateTime.now(),
            ),
        ).toAdminInquiryRecord()
    }
}

private fun InquiryEntity.toAdminInquiryRecord(): AdminInquiryRecord {
    return AdminInquiryRecord(
        id = id,
        memberId = memberId,
        category = category,
        content = content,
        status = status,
        adminMemo = adminMemo,
        handledByMemberId = handledByMemberId,
        handledAt = handledAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

private fun InquiryEntity.copyForAdmin(
    status: InquiryStatus = this.status,
    adminMemo: String? = this.adminMemo,
    handledByMemberId: Long? = this.handledByMemberId,
    handledAt: LocalDateTime? = this.handledAt,
    updatedAt: LocalDateTime,
): InquiryEntity {
    return InquiryEntity(
        id = id,
        memberId = memberId,
        category = category,
        content = content,
        status = status,
        adminMemo = adminMemo,
        handledByMemberId = handledByMemberId,
        handledAt = handledAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
