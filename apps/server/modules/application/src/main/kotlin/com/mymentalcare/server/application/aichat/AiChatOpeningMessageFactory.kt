package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.domain.aichat.AiChatCheckInTemplateType
import org.springframework.stereotype.Component

@Component
internal class AiChatOpeningMessageFactory {
    // 바로 상담을 시작한 사용자에게 부담 없는 첫 입력을 유도한다.
    fun buildDirectOpeningMessage(): String {
        return "안녕하세요, 마음이에요. 지금 마음에 떠오르는 말을 한 문장부터 편하게 적어주세요."
    }

    // 체크인 결과를 반영하되 사용자가 말하지 않은 원인을 단정하지 않는 첫 응답을 만든다.
    fun buildCheckInOpeningMessage(templateType: AiChatCheckInTemplateType, summaryText: String): String {
        return when (templateType) {
            AiChatCheckInTemplateType.BASIC_EMOTION ->
                "${summaryText}로 느껴지는 상태군요. 지금 제일 크게 마음에 걸리는 부분부터 같이 정리해볼까요?"

            AiChatCheckInTemplateType.CONVERSATION_START ->
                "$summaryText 쪽으로 이야기하고 싶군요. 원하는 방식에 맞춰 천천히 들어볼게요."

            AiChatCheckInTemplateType.CONDITION ->
                "${summaryText}의 영향을 받고 있군요. 지금 에너지에 맞춰 무리하지 않는 선에서 이야기해도 괜찮아요."

            AiChatCheckInTemplateType.DAY_REVIEW ->
                "${summaryText}로 오늘을 마무리하고 싶군요. 오늘 남은 마음을 짧게 정리해보는 것부터 시작해볼게요."
        }
    }
}
