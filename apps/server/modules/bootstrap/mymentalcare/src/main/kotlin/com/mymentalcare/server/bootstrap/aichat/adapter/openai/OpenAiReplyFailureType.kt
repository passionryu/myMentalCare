package com.mymentalcare.server.bootstrap.aichat.adapter.openai

enum class OpenAiReplyFailureType {
    API_KEY_MISSING,
    TIMEOUT,
    UNAUTHORIZED,
    RATE_LIMIT,
    SERVER_ERROR,
    UNKNOWN,
}
