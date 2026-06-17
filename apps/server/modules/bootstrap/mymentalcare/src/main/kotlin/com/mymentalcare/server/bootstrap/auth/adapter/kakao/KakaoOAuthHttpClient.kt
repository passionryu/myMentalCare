package com.mymentalcare.server.bootstrap.auth.adapter.kakao

import com.fasterxml.jackson.databind.JsonNode
import com.mymentalcare.server.application.auth.KakaoAuthFailedException
import com.mymentalcare.server.application.auth.response.KakaoProfile
import com.mymentalcare.server.application.auth.response.KakaoTokenResponse
import com.mymentalcare.server.application.auth.port.KakaoOAuthClient
import com.mymentalcare.server.bootstrap.config.KakaoOAuthProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.util.UriComponentsBuilder
import java.net.http.HttpClient

@Component
class KakaoOAuthHttpClient(
    restClientBuilder: RestClient.Builder,
    private val kakaoOAuthProperties: KakaoOAuthProperties,
) : KakaoOAuthClient {
    private val restClient = restClientBuilder
        .requestFactory(
            JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                    .connectTimeout(kakaoOAuthProperties.timeout)
                    .build()
            ).apply {
                setReadTimeout(kakaoOAuthProperties.timeout)
            }
        )
        .build()

    // 카카오 OAuth 인증 화면 URL을 서버 설정값 기준으로 생성한다.
    override fun buildAuthorizationUrl(state: String): String {
        if (kakaoOAuthProperties.clientId.isBlank()) {
            throw KakaoAuthFailedException("카카오 로그인 설정이 준비되지 않았습니다.")
        }

        return UriComponentsBuilder
            .fromUriString(kakaoOAuthProperties.authorizeUrl)
            .queryParam("response_type", "code")
            .queryParam("client_id", kakaoOAuthProperties.clientId)
            .queryParam("redirect_uri", kakaoOAuthProperties.redirectUri)
            .queryParam("state", state)
            .build()
            .toUriString()
    }

    // 카카오 인가 코드를 access token으로 교환한다.
    override fun fetchToken(code: String): KakaoTokenResponse {
        val form = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", kakaoOAuthProperties.clientId)
            add("redirect_uri", kakaoOAuthProperties.redirectUri)
            add("code", code)
            if (kakaoOAuthProperties.clientSecret.isNotBlank()) {
                add("client_secret", kakaoOAuthProperties.clientSecret)
            }
        }

        try {
            val response = restClient.post()
                .uri(kakaoOAuthProperties.tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(JsonNode::class.java)

            val accessToken = response?.path("access_token")?.asText(null)
            if (accessToken.isNullOrBlank()) {
                throw KakaoAuthFailedException()
            }

            return KakaoTokenResponse(accessToken = accessToken)
        } catch (e: RestClientException) {
            throw KakaoAuthFailedException("카카오 토큰 요청이 실패했습니다.")
        }
    }

    // 카카오 access token으로 서비스 회원 식별에 필요한 최소 프로필만 조회한다.
    override fun fetchProfile(accessToken: String): KakaoProfile {
        try {
            val response = restClient.get()
                .uri(kakaoOAuthProperties.profileUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(JsonNode::class.java)

            val providerUserId = response?.path("id")?.asText(null)
            if (providerUserId.isNullOrBlank()) {
                throw KakaoAuthFailedException()
            }

            return KakaoProfile(
                providerUserId = providerUserId,
                email = response.path("kakao_account").path("email").asText(null),
                nickname = response.path("properties").path("nickname").asText(null),
            )
        } catch (e: RestClientException) {
            throw KakaoAuthFailedException("카카오 프로필 요청이 실패했습니다.")
        }
    }
}
