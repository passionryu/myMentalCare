package com.mymentalcare.server.bootstrap.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mymentalcare.notification")
data class OperatorNotificationProperties(
    val discordWebhookUrl: String = "",
)
