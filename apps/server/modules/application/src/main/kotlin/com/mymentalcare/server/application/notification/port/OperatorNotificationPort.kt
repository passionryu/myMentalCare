package com.mymentalcare.server.application.notification.port

import com.mymentalcare.server.application.aichat.response.AiReplyFailureNotification
import com.mymentalcare.server.application.notification.response.InquiryReceivedNotification

interface OperatorNotificationPort {
    // AI 응답 생성 실패처럼 운영자가 알아야 하는 장애성 이벤트를 알린다.
    fun notifyAiReplyFailure(notification: AiReplyFailureNotification)

    // 사용자가 문의를 접수하면 운영자가 빠르게 확인할 수 있도록 알린다.
    fun notifyInquiryReceived(notification: InquiryReceivedNotification)
}
