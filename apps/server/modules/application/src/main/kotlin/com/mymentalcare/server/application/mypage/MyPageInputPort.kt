package com.mymentalcare.server.application.mypage

interface MyPageInputPort {
    fun readSummary(memberId: Long): MyPageSummaryResponse
}
