package com.mymentalcare.server.application.notification.port

import com.mymentalcare.server.application.aichat.response.AiReplyFailureNotification

interface OperatorNotificationPort {
    // AI 응답 생성 실패처럼 운영자가 알아야 하는 장애성 이벤트를 알린다.
    fun notifyAiReplyFailure(notification: AiReplyFailureNotification)
}
