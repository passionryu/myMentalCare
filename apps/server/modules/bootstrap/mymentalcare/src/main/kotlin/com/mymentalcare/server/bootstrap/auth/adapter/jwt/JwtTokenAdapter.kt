package com.mymentalcare.server.bootstrap.auth.adapter.jwt

import com.mymentalcare.server.application.auth.port.JwtTokenIssuer
import com.mymentalcare.server.bootstrap.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date

private const val TOKEN_TYPE_CLAIM = "token_type"
private const val ACCESS_TOKEN_TYPE = "access"
private const val REFRESH_TOKEN_TYPE = "refresh"

@Component
class JwtTokenAdapter(
    private val jwtProperties: JwtProperties,
) : JwtTokenIssuer {
    override fun issueAccessToken(memberId: Long): String {
        return issueToken(memberId, jwtProperties.accessTokenExpiration.seconds, ACCESS_TOKEN_TYPE)
    }

    override fun issueRefreshToken(memberId: Long): String {
        return issueToken(memberId, jwtProperties.refreshTokenExpiration.seconds, REFRESH_TOKEN_TYPE)
    }

    override fun readMemberIdFromRefreshToken(refreshToken: String): Long? {
        val claims = parseClaims(refreshToken) ?: return null
        if (claims[TOKEN_TYPE_CLAIM] != REFRESH_TOKEN_TYPE) {
            return null
        }

        return claims.subject.toLongOrNull()
    }

    private fun issueToken(memberId: Long, expirationSeconds: Long, tokenType: String): String {
        val now = Instant.now()
        val key = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))

        return Jwts.builder()
            .setSubject(memberId.toString())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusSeconds(expirationSeconds)))
            .claim(TOKEN_TYPE_CLAIM, tokenType)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
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
