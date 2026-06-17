package com.mymentalcare.server.application.mypage.port

import com.mymentalcare.server.application.mypage.response.*

interface MyPageInputPort {
    fun readSummary(memberId: Long): MyPageSummaryResponse
}
