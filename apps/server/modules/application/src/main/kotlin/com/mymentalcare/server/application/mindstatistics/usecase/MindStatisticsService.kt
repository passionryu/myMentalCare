package com.mymentalcare.server.application.mindstatistics.usecase

import com.mymentalcare.server.application.aichat.port.AiChatReportRepository
import com.mymentalcare.server.application.aichat.port.AiChatRoomRepository
import com.mymentalcare.server.application.aichat.port.ChatMessageRepository
import com.mymentalcare.server.application.mindstatistics.port.MindStatisticsInputPort
import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsCalendarDayResponse
import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsCalendarResponse
import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsDayDetailResponse
import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsEmotionDistributionResponse
import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsEmotionTrendResponse
import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsMessageResponse
import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsOverviewResponse
import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsReportResponse
import com.mymentalcare.server.domain.aichat.AiChatReport
import com.mymentalcare.server.domain.aichat.ChatMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

private val KOREA_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
private const val MIN_RANGE_WEEKS = 1
private const val MAX_RANGE_WEEKS = 4
private const val MESSAGE_PREVIEW_LIMIT = 90
private const val MAX_RAW_EMOTION_INTENSITY = 5
private const val MAX_STATISTICS_EMOTION_SCORE = 100
private const val UNKNOWN_EMOTION_LABEL = "미분류"

@Service
class MindStatisticsService(
    private val aiChatRoomRepository: AiChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val aiChatReportRepository: AiChatReportRepository,
) : MindStatisticsInputPort {
    // 월 단위 마음 대화와 리포트 기록을 달력에 표시할 수 있게 조회한다.
    @Transactional(readOnly = true)
    override fun readCalendar(memberId: Long, month: YearMonth): MindStatisticsCalendarResponse {
        val startDate = month.atDay(1)
        val endDate = month.atEndOfMonth()

        val rooms = aiChatRoomRepository.findByMemberIdAndDateRange(memberId, startDate, endDate)
        val messagesByRoomId = chatMessageRepository.findByRoomIds(rooms.map { it.id }).groupBy { it.roomId }
        val latestReportByDate = readLatestReportsByDate(memberId, startDate, endDate)
        val roomsByDate = rooms.groupBy { it.conversationDate }

        val days = (0 until month.lengthOfMonth()).map { dayOffset ->
            val date = startDate.plusDays(dayOffset.toLong())
            val dayRooms = roomsByDate[date].orEmpty()
            val messageCount = dayRooms.sumOf { room -> messagesByRoomId[room.id].orEmpty().size }
            val report = latestReportByDate[date]

            MindStatisticsCalendarDayResponse(
                date = date,
                hasConversation = dayRooms.isNotEmpty() && messageCount > 0,
                hasReport = report != null,
                messageCount = messageCount,
                primaryEmotion = report?.primaryEmotion,
                emotionIntensity = report?.emotionIntensity?.toStatisticsEmotionScore(),
                todaySentence = report?.todaySentence,
            )
        }

        return MindStatisticsCalendarResponse(
            month = month.toString(),
            totalConversationDays = days.count { it.hasConversation },
            totalReportDays = days.count { it.hasReport },
            days = days,
        )
    }

    // 특정 날짜의 마음 대화와 리포트 상세 내용을 조회한다.
    @Transactional(readOnly = true)
    override fun readDayDetail(memberId: Long, date: LocalDate): MindStatisticsDayDetailResponse {
        val room = aiChatRoomRepository.findByMemberIdAndDateRange(memberId, date, date).firstOrNull()
        val messages = room?.let { chatMessageRepository.findByRoomId(it.id) }.orEmpty()
        val report = aiChatReportRepository.findByMemberIdAndDateRange(memberId, date, date)
            .maxByOrNull { it.createdAt ?: LocalDateTime.MIN }

        return MindStatisticsDayDetailResponse(
            date = date,
            roomId = room?.id,
            hasConversation = room != null && messages.isNotEmpty(),
            messageCount = messages.size,
            messages = messages.map { it.toStatisticsMessageResponse() },
            report = report?.toStatisticsReportResponse(),
        )
    }

    // 최근 1~4주 동안의 감정 분포와 주간 흐름을 조회한다.
    @Transactional(readOnly = true)
    override fun readOverview(memberId: Long, rangeWeeks: Int): MindStatisticsOverviewResponse {
        val normalizedRangeWeeks = rangeWeeks.coerceIn(MIN_RANGE_WEEKS, MAX_RANGE_WEEKS)
        val endDate = LocalDate.now(KOREA_ZONE_ID)
        val startDate = endDate.minusWeeks(normalizedRangeWeeks.toLong()).plusDays(1)

        val rooms = aiChatRoomRepository.findByMemberIdAndDateRange(memberId, startDate, endDate)
        val messages = chatMessageRepository.findByRoomIds(rooms.map { it.id })
        val reports = aiChatReportRepository.findByMemberIdAndDateRange(memberId, startDate, endDate)
        val latestReports = reports
            .groupBy { it.conversationDate }
            .values
            .mapNotNull { it.maxByOrNull { report -> report.createdAt ?: LocalDateTime.MIN } }

        return MindStatisticsOverviewResponse(
            rangeWeeks = normalizedRangeWeeks,
            startDate = startDate,
            endDate = endDate,
            totalConversationDays = rooms.map { it.conversationDate }.distinct().size,
            totalMessages = messages.size,
            totalReports = latestReports.size,
            emotionDistribution = buildEmotionDistribution(latestReports),
            emotionTrend = buildEmotionTrend(latestReports, startDate, endDate, normalizedRangeWeeks),
        )
    }

    // 날짜별 최신 리포트만 남겨 달력에 표시할 감정 정보를 정리한다.
    private fun readLatestReportsByDate(memberId: Long, startDate: LocalDate, endDate: LocalDate): Map<LocalDate, AiChatReport> {
        return aiChatReportRepository.findByMemberIdAndDateRange(memberId, startDate, endDate)
            .groupBy { it.conversationDate }
            .mapValues { (_, reports) -> reports.maxBy { it.createdAt ?: LocalDateTime.MIN } }
    }

    // 감정 이름별 리포트 비율을 계산한다.
    private fun buildEmotionDistribution(reports: List<AiChatReport>): List<MindStatisticsEmotionDistributionResponse> {
        if (reports.isEmpty()) {
            return emptyList()
        }

        return reports
            .groupingBy { it.primaryEmotion.ifBlank { UNKNOWN_EMOTION_LABEL } }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { (emotion, count) ->
                MindStatisticsEmotionDistributionResponse(
                    emotion = emotion,
                    count = count,
                    ratio = (count.toDouble() / reports.size.toDouble()).roundToTwoDecimals(),
                )
            }
    }

    // 주 단위 평균 감정 강도를 계산해 추세 그래프용 데이터로 변환한다.
    private fun buildEmotionTrend(
        reports: List<AiChatReport>,
        startDate: LocalDate,
        endDate: LocalDate,
        rangeWeeks: Int,
    ): List<MindStatisticsEmotionTrendResponse> {
        return (0 until rangeWeeks).map { weekIndex ->
            val weekStart = startDate.plusWeeks(weekIndex.toLong())
            val weekEnd = minOf(weekStart.plusDays(6), endDate)
            val weekReports = reports.filter { !it.conversationDate.isBefore(weekStart) && !it.conversationDate.isAfter(weekEnd) }
            val intensities = weekReports.mapNotNull { it.emotionIntensity?.toStatisticsEmotionScore() }

            MindStatisticsEmotionTrendResponse(
                weekStartDate = weekStart,
                weekEndDate = weekEnd,
                averageEmotionIntensity = intensities.takeIf { it.isNotEmpty() }?.average()?.roundToTwoDecimals(),
                reportCount = weekReports.size,
            )
        }
    }

    // 메시지 본문을 화면 통계에서 쓰기 좋은 길이로 줄인다.
    private fun ChatMessage.toStatisticsMessageResponse(): MindStatisticsMessageResponse {
        return MindStatisticsMessageResponse(
            messageId = id,
            senderType = senderType.name,
            contentPreview = content.toPreview(),
            messageOrder = messageOrder,
            isCrisisDetected = isCrisisDetected,
            createdAt = createdAt,
        )
    }

    // 마음 리포트를 통계 상세 응답으로 변환한다.
    private fun AiChatReport.toStatisticsReportResponse(): MindStatisticsReportResponse {
        return MindStatisticsReportResponse(
            reportId = id,
            reportType = reportType.name,
            primaryEmotion = primaryEmotion,
            emotionIntensity = emotionIntensity?.toStatisticsEmotionScore(),
            mainCause = mainCause,
            summary = summary,
            emotionalFlow = emotionalFlow,
            todaySentence = todaySentence,
            createdAt = createdAt,
        )
    }

    // 긴 메시지를 카드 안에서 읽기 쉬운 미리보기로 줄인다.
    private fun String.toPreview(): String {
        return if (length <= MESSAGE_PREVIEW_LIMIT) this else take(MESSAGE_PREVIEW_LIMIT).trimEnd() + "..."
    }

    // 소수점 값을 화면 표시용 두 자리로 반올림한다.
    private fun Double.roundToTwoDecimals(): Double {
        return BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    // 리포트의 원본 감정 강도 값을 통계 화면에서 쓰는 0~100점으로 변환한다.
    private fun Int.toStatisticsEmotionScore(): Int {
        return ((coerceIn(0, MAX_RAW_EMOTION_INTENSITY).toDouble() / MAX_RAW_EMOTION_INTENSITY.toDouble()) * MAX_STATISTICS_EMOTION_SCORE)
            .toInt()
    }

}
