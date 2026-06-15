package com.mymentalcare.server.application.auth

class OAuthExchangeCodeInvalidException(
    message: String = "카카오 로그인 결과를 확인할 수 없습니다.",
) : RuntimeException(message)
