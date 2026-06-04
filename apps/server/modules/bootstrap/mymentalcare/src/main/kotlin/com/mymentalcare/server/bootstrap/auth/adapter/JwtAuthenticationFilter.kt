package com.mymentalcare.server.bootstrap.auth.adapter

import com.mymentalcare.server.bootstrap.config.JwtProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.nio.charset.StandardCharsets

@Component
class JwtAuthenticationFilter(
    private val jwtProperties: JwtProperties,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val memberId = resolveMemberId(request)
        if (memberId != null && SecurityContextHolder.getContext().authentication == null) {
            SecurityContextHolder.getContext().authentication =
                UsernamePasswordAuthenticationToken(memberId, null, emptyList())
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveMemberId(request: HttpServletRequest): Long? {
        val token = request.getHeader("Authorization")
            ?.takeIf { it.startsWith("Bearer ") }
            ?.removePrefix("Bearer ")
            ?: return null

        return runCatching {
            val key = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
                .subject
                .toLong()
        }.getOrNull()
    }
}
