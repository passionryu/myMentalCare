package com.mymentalcare.server.bootstrap.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class RequestIdLoggingFilterTest {

    @Test
    fun `요청 ID가 없으면 응답 헤더에 새 요청 ID를 남긴다`() {
        val request = MockHttpServletRequest("GET", "/actuator/health")
        val response = MockHttpServletResponse()

        RequestIdLoggingFilter().doFilter(request, response, MockFilterChain())

        assertNotNull(response.getHeader("X-Request-Id"))
    }

    @Test
    fun `요청 ID 헤더가 있으면 같은 값을 응답 헤더에 남긴다`() {
        val request = MockHttpServletRequest("GET", "/actuator/health")
        request.addHeader("X-Request-Id", "request-123")
        val response = MockHttpServletResponse()

        RequestIdLoggingFilter().doFilter(request, response, MockFilterChain())

        assertEquals("request-123", response.getHeader("X-Request-Id"))
    }
}
