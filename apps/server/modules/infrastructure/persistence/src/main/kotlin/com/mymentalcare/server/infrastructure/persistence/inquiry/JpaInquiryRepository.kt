package com.mymentalcare.server.infrastructure.persistence.inquiry

import com.mymentalcare.server.domain.inquiry.InquiryStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface JpaInquiryRepository : JpaRepository<InquiryEntity, Long> {
    @Query(
        """
        select inquiry
        from InquiryEntity inquiry
        where (:status is null or inquiry.status = :status)
          and (
            :keywordLike is null
            or lower(inquiry.category) like :keywordLike
            or lower(inquiry.content) like :keywordLike
            or inquiry.id = :keywordId
            or inquiry.memberId = :keywordId
          )
        order by
          case inquiry.status
            when com.mymentalcare.server.domain.inquiry.InquiryStatus.RECEIVED then 0
            when com.mymentalcare.server.domain.inquiry.InquiryStatus.IN_PROGRESS then 1
            else 2
          end,
          inquiry.createdAt desc,
          inquiry.id desc
        """,
    )
    fun findForAdmin(
        @Param("keywordLike") keywordLike: String?,
        @Param("keywordId") keywordId: Long?,
        @Param("status") status: InquiryStatus?,
        pageable: Pageable,
    ): Page<InquiryEntity>
}
