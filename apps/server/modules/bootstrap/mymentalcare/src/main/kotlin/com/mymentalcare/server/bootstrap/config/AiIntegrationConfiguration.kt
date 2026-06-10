package com.mymentalcare.server.bootstrap.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    OpenAiProperties::class,
    OperatorNotificationProperties::class,
)
class AiIntegrationConfiguration
