package com.mymentalcare.server.infrastructure.persistence.inquiry

import com.mymentalcare.server.domain.inquiry.Inquiry
import com.mymentalcare.server.domain.inquiry.InquiryStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "inquiries")
class InquiryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "category", nullable = false, length = 30)
    val category: String,

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    val status: InquiryStatus,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): Inquiry {
        return Inquiry(
            id = id,
            memberId = memberId,
            category = category,
            content = content,
            status = status,
            createdAt = createdAt,
        )
    }
}

fun Inquiry.toEntity(): InquiryEntity {
    return InquiryEntity(
        id = id,
        memberId = memberId,
        category = category,
        content = content,
        status = status,
        createdAt = createdAt ?: LocalDateTime.now(),
    )
}
