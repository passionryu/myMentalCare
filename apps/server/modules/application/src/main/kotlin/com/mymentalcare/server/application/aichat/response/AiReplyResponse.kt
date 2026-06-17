package com.mymentalcare.server.application.aichat.response

data class AiReplyResponse(
    val content: String,
    val failed: Boolean = false,
)
