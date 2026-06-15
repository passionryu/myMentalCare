package com.mymentalcare.server.bootstrap.auth.adapter

import com.fasterxml.jackson.databind.ObjectMapper
import com.mymentalcare.server.application.auth.OAuthLoginResult
import com.mymentalcare.server.application.auth.OAuthLoginState
import com.mymentalcare.server.application.port.OAuthLoginResultStore
import com.mymentalcare.server.application.port.OAuthStateStore
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisOAuthStateAdapter(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) : OAuthStateStore, OAuthLoginResultStore {
    override fun storeState(state: OAuthLoginState, ttl: Duration) {
        redisTemplate.opsForValue().set(
            stateKey(state.state),
            objectMapper.writeValueAsString(state),
            ttl,
        )
    }

    override fun consumeState(state: String): OAuthLoginState? {
        val key = stateKey(state)
        val value = redisTemplate.opsForValue().get(key) ?: return null
        redisTemplate.delete(key)
        return objectMapper.readValue(value, OAuthLoginState::class.java)
    }

    override fun storeResult(code: String, result: OAuthLoginResult, ttl: Duration) {
        redisTemplate.opsForValue().set(
            resultKey(code),
            objectMapper.writeValueAsString(result),
            ttl,
        )
    }

    override fun consumeResult(code: String): OAuthLoginResult? {
        val key = resultKey(code)
        val value = redisTemplate.opsForValue().get(key) ?: return null
        redisTemplate.delete(key)
        return objectMapper.readValue(value, OAuthLoginResult::class.java)
    }

    private fun stateKey(state: String): String = "auth:kakao:state:$state"

    private fun resultKey(code: String): String = "auth:kakao:result:$code"
}
