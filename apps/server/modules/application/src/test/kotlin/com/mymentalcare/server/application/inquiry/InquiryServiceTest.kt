package com.mymentalcare.server.application.inquiry

import com.mymentalcare.server.application.port.InquiryRepository
import com.mymentalcare.server.domain.inquiry.Inquiry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InquiryServiceTest {
    @Test
    fun `문의 접수 시 로그인 회원과 문의 내용이 저장된다`() {
        val repository = FakeInquiryRepository()
        val service = InquiryService(repository)

        val response = service.createInquiry(
            memberId = 7L,
            request = CreateInquiryRequest(
                category = "계정",
                content = "로그인 관련 문의가 있습니다.",
            ),
        )

        assertEquals(1L, response.inquiryId)
        assertEquals("RECEIVED", response.status)
        assertEquals(7L, repository.savedInquiry?.memberId)
        assertEquals("계정", repository.savedInquiry?.category)
        assertEquals("로그인 관련 문의가 있습니다.", repository.savedInquiry?.content)
        assertTrue(response.createdAt.toString().isNotBlank())
    }

    private class FakeInquiryRepository : InquiryRepository {
        var savedInquiry: Inquiry? = null

        override fun save(inquiry: Inquiry): Inquiry {
            val saved = inquiry.copy(id = 1L)
            savedInquiry = saved
            return saved
        }
    }
}
