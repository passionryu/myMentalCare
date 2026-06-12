package com.mymentalcare.server.bootstrap.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class WebCorsConfiguration {

    @Value("\${mymentalcare.cors.allowed-origins:http://localhost:3000}")
    private lateinit var allowedOriginValues: String

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            val origins = allowedOriginValues
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            if (origins.contains("*")) {
                addAllowedOriginPattern("*")
            } else {
                allowedOriginPatterns = origins.toMutableList()
            }
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("Authorization", "Content-Type")
            allowCredentials = false
            maxAge = 3600L
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/api/**", config)
        }
    }
}
