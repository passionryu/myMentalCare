package com.mymentalcare.server.application.inquiry.usecase

import com.mymentalcare.server.application.common.extension.logError
import com.mymentalcare.server.application.notification.port.OperatorNotificationPort
import com.mymentalcare.server.application.notification.response.InquiryReceivedNotification
import org.springframework.stereotype.Component

@Component
internal class InquiryOperatorNotifier(
    private val operatorNotificationPort: OperatorNotificationPort,
) {
    // 문의 접수는 성공시키기 위해 운영자 알림 실패를 내부 로그로만 격리한다.
    fun notifyOperatorInquiryWasReceived(notification: InquiryReceivedNotification) {
        try {
            operatorNotificationPort.notifyInquiryReceived(notification)
        } catch (e: Exception) {
            logError(e) {
                "[문의 접수] 운영자 문의 알림 전송 실패. " +
                    "who=memberId:${notification.memberId}, " +
                    "what=InquiryOperatorNotifier.notifyOperatorInquiryWasReceived, " +
                    "requestData=inquiryId:${notification.inquiryId},category:${notification.category}, " +
                    "reason=${e.message}"
            }
        }
    }
}
