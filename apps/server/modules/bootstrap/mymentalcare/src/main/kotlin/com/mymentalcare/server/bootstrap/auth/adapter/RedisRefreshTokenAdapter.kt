package com.mymentalcare.server.bootstrap.auth.adapter

import com.mymentalcare.server.application.port.RefreshTokenStore
import com.mymentalcare.server.bootstrap.config.JwtProperties
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisRefreshTokenAdapter(
    private val redisTemplate: StringRedisTemplate,
    private val jwtProperties: JwtProperties,
) : RefreshTokenStore {
    override fun storeRefreshToken(memberId: Long, refreshToken: String) {
        redisTemplate.opsForValue().set(
            "auth:refresh-token:$memberId",
            refreshToken,
            jwtProperties.refreshTokenExpiration,
        )
    }

    override fun readRefreshToken(memberId: Long): String? {
        return redisTemplate.opsForValue().get("auth:refresh-token:$memberId")
    }
}
