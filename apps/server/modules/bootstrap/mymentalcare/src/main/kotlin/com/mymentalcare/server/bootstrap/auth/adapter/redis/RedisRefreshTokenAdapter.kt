package com.mymentalcare.server.bootstrap.auth.adapter.redis

import com.mymentalcare.server.application.auth.port.RefreshTokenStore
import com.mymentalcare.server.bootstrap.config.JwtProperties
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

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

    override fun deleteRefreshToken(memberId: Long) {
        redisTemplate.delete("auth:refresh-token:$memberId")
    }
}
