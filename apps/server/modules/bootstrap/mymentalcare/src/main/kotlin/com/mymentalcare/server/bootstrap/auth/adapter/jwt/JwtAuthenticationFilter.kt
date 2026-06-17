package com.mymentalcare.server.bootstrap.auth.adapter.jwt

import com.mymentalcare.server.bootstrap.config.JwtProperties
import io.jsonwebtoken.Claims
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

private const val TOKEN_TYPE_CLAIM = "token_type"
private const val ACCESS_TOKEN_TYPE = "access"

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

        val claims = parseClaims(token) ?: return null
        if (claims[TOKEN_TYPE_CLAIM] != ACCESS_TOKEN_TYPE) {
            return null
        }

        return claims.subject.toLongOrNull()
    }

    private fun parseClaims(token: String): Claims? {
        return runCatching {
            val key = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
        }.getOrNull()
    }
}
