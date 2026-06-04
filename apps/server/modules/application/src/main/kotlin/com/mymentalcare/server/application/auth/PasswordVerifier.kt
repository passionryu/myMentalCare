package com.mymentalcare.server.application.auth

import com.mymentalcare.server.application.common.extension.logWarn
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordVerifier(
    private val passwordEncoder: PasswordEncoder,
) {
    // 입력 비밀번호가 저장된 해시와 일치하지 않으면 로그인 실패로 처리한다.
    fun verifyPasswordMatches(rawPassword: String, encodedPassword: String) {
        if (passwordEncoder.matches(rawPassword, encodedPassword)) {
            return
        }

        logWarn {
            "[로그인] 비밀번호 검증 실패. " +
                "who=anonymous, " +
                "what=POST /api/auth/login, " +
                "requestData=password:masked, " +
                "reason=password_mismatch"
        }
        throw LoginFailedException()
    }
}
