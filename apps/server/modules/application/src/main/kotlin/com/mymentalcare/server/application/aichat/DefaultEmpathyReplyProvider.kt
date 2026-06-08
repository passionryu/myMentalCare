package com.mymentalcare.server.application.aichat

class DefaultEmpathyReplyProvider : AiReplyProvider {
    // OpenAI 연동을 사용하지 않는 환경에서 기본 공감형 응답을 생성한다.
    override fun generateReply(request: AiReplyRequest): AiReplyResponse {
        return AiReplyResponse(replyFor(request.messageId.toInt(), crisisDetected = false))
    }

    // 메시지 순서를 기준으로 기본 공감형 응답을 순환 제공한다.
    fun replyFor(messageOrder: Int, crisisDetected: Boolean): String {
        if (crisisDetected) {
            return SAFETY_GUIDE_MESSAGE
        }

        return replies[((messageOrder - 1) / 2).mod(replies.size)]
    }

    private val replies = listOf(
        "그 마음을 꺼내놓아줘서 고마워요. 지금 느끼는 감정을 천천히 바라봐도 괜찮아요.",
        "오늘 많이 애썼겠어요. 잠깐 숨을 고르고, 가장 크게 남아 있는 감정부터 같이 살펴봐요.",
        "그런 순간에는 마음이 무거워질 수 있어요. 지금 여기에서 할 수 있는 작은 돌봄부터 찾아봐요.",
        "당신의 감정은 충분히 말해도 되는 감정이에요. 조금 더 자세히 들려줘도 괜찮아요.",
        "혼자 정리하기 어려운 마음일 수 있어요. 제가 차분히 함께 들어볼게요.",
        "지금 마음의 온도를 알아차린 것만으로도 중요한 시작이에요.",
        "완벽하게 설명하지 않아도 괜찮아요. 떠오르는 말 그대로 적어도 충분합니다.",
        "그 상황에서 당신이 느낀 감정은 자연스러울 수 있어요. 스스로를 너무 몰아붙이지 않았으면 해요.",
        "잠시 멈춰서 몸과 마음이 보내는 신호를 확인해봐요. 지금 필요한 건 평가보다 돌봄일 수 있어요.",
        "오늘의 마음을 기록해둔 것만으로도 나를 챙기는 행동을 한 거예요.",
    )
}
