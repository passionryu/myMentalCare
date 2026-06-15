package com.mymentalcare.server.application.auth

class KakaoAuthFailedException(
    message: String = "카카오 로그인 처리 중 문제가 발생했습니다.",
) : RuntimeException(message)
