package com.mymentalcare.server.bootstrap.auth.web

import com.mymentalcare.server.bootstrap.config.KakaoOAuthProperties
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class KakaoOAuthRedirectResponseFactory(
    private val kakaoOAuthProperties: KakaoOAuthProperties,
) {
    fun redirectToWebCallback(
        code: String? = null,
        errorCode: String? = null,
        redirectTo: String? = null,
    ): ResponseEntity<Void> {
        val builder = UriComponentsBuilder.fromUriString(kakaoOAuthProperties.webCallbackUrl)
        if (!code.isNullOrBlank()) {
            builder.queryParam("code", code)
        }
        if (!errorCode.isNullOrBlank()) {
            builder.queryParam("error", errorCode)
        }
        if (!redirectTo.isNullOrBlank()) {
            builder.queryParam("redirectTo", redirectTo)
        }

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(builder.build().toUri())
            .build()
    }
}
