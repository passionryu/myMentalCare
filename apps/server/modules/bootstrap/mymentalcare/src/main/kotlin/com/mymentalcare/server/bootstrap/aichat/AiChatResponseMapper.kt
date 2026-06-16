package com.mymentalcare.server.bootstrap.aichat

import com.mymentalcare.server.application.aichat.AiChatCheckInResponse as ApplicationAiChatCheckInResponse
import com.mymentalcare.server.application.aichat.AiChatHistoryRoomDetailResponse as ApplicationAiChatHistoryRoomDetailResponse
import com.mymentalcare.server.application.aichat.AiChatHistoryRoomResponse as ApplicationAiChatHistoryRoomResponse
import com.mymentalcare.server.application.aichat.AiChatMessageResponse as ApplicationAiChatMessageResponse
import com.mymentalcare.server.application.aichat.AiChatReportReadinessResponse as ApplicationAiChatReportReadinessResponse
import com.mymentalcare.server.application.aichat.AiChatReportResponse as ApplicationAiChatReportResponse
import com.mymentalcare.server.application.aichat.AiChatReportSongResponse as ApplicationAiChatReportSongResponse
import com.mymentalcare.server.application.aichat.AiChatSegmentResponse as ApplicationAiChatSegmentResponse
import com.mymentalcare.server.application.aichat.SendAiChatMessageResponse as ApplicationSendAiChatMessageResponse
import com.mymentalcare.server.application.aichat.StartAiChatSegmentResponse as ApplicationStartAiChatSegmentResponse
import com.mymentalcare.server.application.aichat.TodayAiChatRoomResponse as ApplicationTodayAiChatRoomResponse

fun ApplicationTodayAiChatRoomResponse.toBootstrapResponse(): TodayAiChatRoomResponse {
    return TodayAiChatRoomResponse(
        roomId = roomId,
        chatbotCode = chatbotCode,
        chatbotName = chatbotName,
        conversationDate = conversationDate,
        status = status,
        hasConversation = hasConversation,
        activeSegmentId = activeSegmentId,
        segments = segments.map { it.toBootstrapResponse() },
        messages = messages.map { it.toBootstrapResponse() },
    )
}

fun ApplicationAiChatHistoryRoomResponse.toBootstrapResponse(): AiChatHistoryRoomResponse {
    return AiChatHistoryRoomResponse(
        roomId = roomId,
        conversationDate = conversationDate,
        status = status,
        messageCount = messageCount,
        latestMessage = latestMessage,
        latestMessageAt = latestMessageAt,
    )
}

fun ApplicationAiChatHistoryRoomDetailResponse.toBootstrapResponse(): AiChatHistoryRoomDetailResponse {
    return AiChatHistoryRoomDetailResponse(
        roomId = roomId,
        chatbotCode = chatbotCode,
        chatbotName = chatbotName,
        conversationDate = conversationDate,
        status = status,
        messages = messages.map { it.toBootstrapResponse() },
    )
}

fun ApplicationSendAiChatMessageResponse.toBootstrapResponse(): SendAiChatMessageResponse {
    return SendAiChatMessageResponse(
        room = room.toBootstrapResponse(),
        userMessage = userMessage.toBootstrapResponse(),
        assistantMessage = assistantMessage.toBootstrapResponse(),
        crisisDetected = crisisDetected,
        crisisGuideMessage = crisisGuideMessage,
        aiReplyFailed = aiReplyFailed,
        aiReplyErrorMessage = aiReplyErrorMessage,
    )
}

fun ApplicationStartAiChatSegmentResponse.toBootstrapResponse(): StartAiChatSegmentResponse {
    return StartAiChatSegmentResponse(
        room = room.toBootstrapResponse(),
        segment = segment.toBootstrapResponse(),
        checkIn = checkIn?.toBootstrapResponse(),
        assistantMessage = assistantMessage.toBootstrapResponse(),
        crisisDetected = crisisDetected,
        crisisGuideMessage = crisisGuideMessage,
        aiReplyFailed = aiReplyFailed,
        aiReplyErrorMessage = aiReplyErrorMessage,
    )
}

fun ApplicationAiChatReportReadinessResponse.toBootstrapResponse(): AiChatReportReadinessResponse {
    return AiChatReportReadinessResponse(
        ready = ready,
        reason = reason,
        userMessageCount = userMessageCount,
        userTextLength = userTextLength,
        requiredUserMessageCount = requiredUserMessageCount,
        requiredUserTextLength = requiredUserTextLength,
        unmetRequirements = unmetRequirements,
        guideMessage = guideMessage,
    )
}

fun ApplicationAiChatReportResponse.toBootstrapResponse(): AiChatReportResponse {
    return AiChatReportResponse(
        reportId = reportId,
        roomId = roomId,
        reportType = reportType,
        conversationDate = conversationDate,
        summary = summary,
        primaryEmotion = primaryEmotion,
        emotionIntensity = emotionIntensity,
        mainCause = mainCause,
        emotionalFlow = emotionalFlow,
        todaySentence = todaySentence,
        songs = songs.map { it.toBootstrapResponse() },
        saved = saved,
        createdAt = createdAt,
    )
}

private fun ApplicationAiChatSegmentResponse.toBootstrapResponse(): AiChatSegmentResponse {
    return AiChatSegmentResponse(
        segmentId = segmentId,
        segmentOrder = segmentOrder,
        startType = startType,
        title = title,
        startedAt = startedAt,
        checkIn = checkIn?.toBootstrapResponse(),
    )
}

private fun ApplicationAiChatReportSongResponse.toBootstrapResponse(): AiChatReportSongResponse {
    return AiChatReportSongResponse(
        title = title,
        artist = artist,
        reason = reason,
        youtubeUrl = youtubeUrl,
    )
}

private fun ApplicationAiChatCheckInResponse.toBootstrapResponse(): AiChatCheckInResponse {
    return AiChatCheckInResponse(
        checkInId = checkInId,
        templateType = templateType,
        summaryText = summaryText,
    )
}

private fun ApplicationAiChatMessageResponse.toBootstrapResponse(): AiChatMessageResponse {
    return AiChatMessageResponse(
        messageId = messageId,
        segmentId = segmentId,
        senderType = senderType,
        content = content,
        messageOrder = messageOrder,
        isCrisisDetected = isCrisisDetected,
        createdAt = createdAt,
    )
}
