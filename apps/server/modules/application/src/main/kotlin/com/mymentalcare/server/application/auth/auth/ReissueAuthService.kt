package com.mymentalcare.server.application.auth.auth

class ReissueAuthService(
    private val authPolicyChecker: AuthPolicyChecker,
) {
    // Auth 유스케이스를 처리한다.
    fun reissueAuth(command: ReissueAuthCommand): ReissueAuthResult {
        authPolicyChecker.validateAuthCanReissue(command)

        TODO("ReissueAuth 유스케이스의 조회, 수행, 기록, 반환 흐름을 구현해야 합니다.")
    }
}
