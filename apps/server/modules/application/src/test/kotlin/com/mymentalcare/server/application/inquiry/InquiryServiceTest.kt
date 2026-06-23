package com.mymentalcare.server.application.inquiry

import com.mymentalcare.server.application.aichat.response.AiReplyFailureNotification
import com.mymentalcare.server.application.inquiry.port.*
import com.mymentalcare.server.application.inquiry.request.*
import com.mymentalcare.server.application.inquiry.response.*
import com.mymentalcare.server.application.inquiry.usecase.*

import com.mymentalcare.server.application.inquiry.port.InquiryRepository
import com.mymentalcare.server.application.notification.port.OperatorNotificationPort
import com.mymentalcare.server.application.notification.response.InquiryReceivedNotification
import com.mymentalcare.server.domain.inquiry.Inquiry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InquiryServiceTest {
    @Test
    fun `문의 접수 시 로그인 회원과 문의 내용이 저장되고 운영자 알림이 요청된다`() {
        val repository = FakeInquiryRepository()
        val notificationPort = FakeOperatorNotificationPort()
        val service = InquiryService(
            inquiryRepository = repository,
            inquiryOperatorNotifier = InquiryOperatorNotifier(notificationPort),
        )

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
        assertEquals(1L, notificationPort.inquiryNotifications.single().inquiryId)
        assertEquals(7L, notificationPort.inquiryNotifications.single().memberId)
        assertEquals("계정", notificationPort.inquiryNotifications.single().category)
    }

    @Test
    fun `운영자 문의 알림 전송이 실패해도 문의 접수는 성공한다`() {
        val repository = FakeInquiryRepository()
        val service = InquiryService(
            inquiryRepository = repository,
            inquiryOperatorNotifier = InquiryOperatorNotifier(FailingOperatorNotificationPort()),
        )

        val response = service.createInquiry(
            memberId = 7L,
            request = CreateInquiryRequest(
                category = "서비스",
                content = "문의 알림 실패 격리 테스트입니다.",
            ),
        )

        assertEquals(1L, response.inquiryId)
        assertEquals("RECEIVED", response.status)
        assertEquals("서비스", repository.savedInquiry?.category)
    }

    private class FakeInquiryRepository : InquiryRepository {
        var savedInquiry: Inquiry? = null

        override fun save(inquiry: Inquiry): Inquiry {
            val saved = inquiry.copy(id = 1L)
            savedInquiry = saved
            return saved
        }
    }

    private class FakeOperatorNotificationPort : OperatorNotificationPort {
        val inquiryNotifications = mutableListOf<InquiryReceivedNotification>()

        override fun notifyAiReplyFailure(notification: AiReplyFailureNotification) = Unit

        override fun notifyInquiryReceived(notification: InquiryReceivedNotification) {
            inquiryNotifications.add(notification)
        }
    }

    private class FailingOperatorNotificationPort : OperatorNotificationPort {
        override fun notifyAiReplyFailure(notification: AiReplyFailureNotification) = Unit

        override fun notifyInquiryReceived(notification: InquiryReceivedNotification) {
            throw IllegalStateException("Discord 전송 실패")
        }
    }
}
