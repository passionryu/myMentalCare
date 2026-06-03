package com.mymentalcare.server.bootstrap.auth.adapter

import com.mymentalcare.server.application.port.JwtTokenIssuer
import com.mymentalcare.server.bootstrap.config.JwtProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date

@Component
class JwtTokenAdapter(
    private val jwtProperties: JwtProperties,
) : JwtTokenIssuer {
    override fun issueAccessToken(memberId: Long): String {
        return issueToken(memberId, jwtProperties.accessTokenExpiration.seconds)
    }

    override fun issueRefreshToken(memberId: Long): String {
        return issueToken(memberId, jwtProperties.refreshTokenExpiration.seconds)
    }

    private fun issueToken(memberId: Long, expirationSeconds: Long): String {
        val now = Instant.now()
        val key = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))

        return Jwts.builder()
            .setSubject(memberId.toString())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusSeconds(expirationSeconds)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }
}
