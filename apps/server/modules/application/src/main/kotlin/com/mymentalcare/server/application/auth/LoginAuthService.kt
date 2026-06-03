package com.mymentalcare.server.application.auth

class LoginAuthService(
    private val authPolicyChecker: AuthPolicyChecker,
) {
    fun loginAuth(command: LoginAuthCommand): LoginAuthResult {
        authPolicyChecker.validateAuthCanLogin(command)

        TODO("LoginAuth 유스케이스의 조회, 수행, 기록, 반환 흐름을 구현해야 합니다.")
    }
}
