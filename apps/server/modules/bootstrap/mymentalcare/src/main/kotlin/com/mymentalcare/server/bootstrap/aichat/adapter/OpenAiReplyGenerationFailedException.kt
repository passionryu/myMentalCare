package com.mymentalcare.server.bootstrap.aichat.adapter

class OpenAiReplyGenerationFailedException(
    val failureType: OpenAiReplyFailureType,
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
