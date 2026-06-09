package com.mymentalcare.server.bootstrap.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

private const val REQUEST_ID_HEADER = "X-Request-Id"
private const val TRACE_ID_MDC_KEY = "traceId"
private const val REQUEST_ID_MDC_KEY = "requestId"

@Component
class RequestIdLoggingFilter : OncePerRequestFilter() {
    private val requestLogger = LoggerFactory.getLogger(javaClass)

    // 요청 단위 식별자를 로그 MDC와 응답 헤더에 남긴다.
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestId = request.getHeader(REQUEST_ID_HEADER)?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        val startedAt = System.currentTimeMillis()

        MDC.put(TRACE_ID_MDC_KEY, requestId)
        MDC.put(REQUEST_ID_MDC_KEY, requestId)
        response.setHeader(REQUEST_ID_HEADER, requestId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            val elapsedMs = System.currentTimeMillis() - startedAt
            requestLogger.info(
                "[HTTP 요청] API 요청 처리 완료. who=anonymous, what={} {}, requestData=requestId:{}, reason=status:{},elapsedMs:{}",
                request.method,
                request.requestURI,
                requestId,
                response.status,
                elapsedMs,
            )
            MDC.remove(TRACE_ID_MDC_KEY)
            MDC.remove(REQUEST_ID_MDC_KEY)
        }
    }
}
