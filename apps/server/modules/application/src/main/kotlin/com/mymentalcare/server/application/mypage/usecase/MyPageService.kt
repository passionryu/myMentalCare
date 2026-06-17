package com.mymentalcare.server.application.mypage.usecase

import com.mymentalcare.server.application.mypage.port.*
import com.mymentalcare.server.application.mypage.response.*

import com.mymentalcare.server.application.member.MemberNotFoundException
import com.mymentalcare.server.application.aichat.port.AiChatReportRepository
import com.mymentalcare.server.application.aichat.port.AiChatRoomRepository
import com.mymentalcare.server.application.aichat.port.ChatMessageRepository
import com.mymentalcare.server.application.member.port.MemberNotificationSettingRepository
import com.mymentalcare.server.application.member.port.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Service
internal class MyPageService(
    private val memberRepository: MemberRepository,
    private val aiChatRoomRepository: AiChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val aiChatReportRepository: AiChatReportRepository,
    private val notificationSettingRepository: MemberNotificationSettingRepository,
) : MyPageInputPort {
    @Transactional(readOnly = true)
    override fun readSummary(memberId: Long): MyPageSummaryResponse {
        memberRepository.findById(memberId) ?: throw MemberNotFoundException()

        val todayRoom = aiChatRoomRepository.findTodayRoom(
            memberId = memberId,
            chatbotCode = "DEFAULT_EMPATHY",
            conversationDate = LocalDate.now(ZoneId.of("Asia/Seoul")),
        )
        val latestReport = aiChatReportRepository.findLatestByMemberId(memberId)
        val notificationSetting = notificationSettingRepository.findByMemberId(memberId)

        return MyPageSummaryResponse(
            hasTodayChat = todayRoom != null,
            todayMessageCount = todayRoom?.let { chatMessageRepository.countByRoomId(it.id) } ?: 0,
            recentChatAt = todayRoom?.let { chatMessageRepository.findLatestByRoomId(it.id)?.createdAt },
            reportCount = aiChatReportRepository.countByMemberId(memberId),
            latestReportAt = latestReport?.createdAt,
            latestReportDate = latestReport?.conversationDate,
            notificationEnabled = notificationSetting?.enabled ?: false,
            notificationTime = (notificationSetting?.notificationTime ?: LocalTime.of(21, 0)).toString(),
        )
    }
}
