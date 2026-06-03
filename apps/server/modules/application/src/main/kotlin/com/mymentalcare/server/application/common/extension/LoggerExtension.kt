package com.mymentalcare.server.application.common.extension

import org.slf4j.LoggerFactory

fun <T : Any> T.logWarn(message: String) {
    LoggerFactory.getLogger(this::class.java).warn(message)
}

inline fun <T : Any> T.logWarn(closure: () -> String) {
    logWarn(closure())
}

fun <T : Any> T.logError(message: String, throwable: Throwable? = null) {
    val logger = LoggerFactory.getLogger(this::class.java)
    if (throwable == null) {
        logger.error(message)
    } else {
        logger.error(message, throwable)
    }
}

inline fun <T : Any> T.logError(throwable: Throwable, closure: () -> String) {
    logError(closure(), throwable)
}
