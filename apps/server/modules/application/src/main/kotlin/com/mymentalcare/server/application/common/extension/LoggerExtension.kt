package com.mymentalcare.server.application.common.extension

import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("ApplicationLogger")

fun Any.logWarn(message: () -> String) {
    logger.warn(message())
}
