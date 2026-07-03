package com.mymentalcare.server.application.mindstatistics.port

import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsCalendarResponse
import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsDayDetailResponse
import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsOverviewResponse
import java.time.LocalDate
import java.time.YearMonth

interface MindStatisticsInputPort {
    fun readCalendar(memberId: Long, month: YearMonth): MindStatisticsCalendarResponse

    fun readDayDetail(memberId: Long, date: LocalDate): MindStatisticsDayDetailResponse

    fun readOverview(memberId: Long, rangeWeeks: Int): MindStatisticsOverviewResponse
}
