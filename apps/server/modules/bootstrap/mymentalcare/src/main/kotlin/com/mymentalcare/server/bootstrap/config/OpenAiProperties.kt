package com.mymentalcare.server.bootstrap.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "mymentalcare.ai.openai")
data class OpenAiProperties(
    val apiKey: String = "",
    val model: String = "gpt-5-nano",
    val timeout: Duration = Duration.ofSeconds(20),
)
