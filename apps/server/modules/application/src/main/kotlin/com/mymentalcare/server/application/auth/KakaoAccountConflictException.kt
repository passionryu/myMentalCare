package com.mymentalcare.server.application.auth

class KakaoAccountConflictException(
    message: String = "이미 같은 이메일로 가입된 계정이 있습니다.",
) : RuntimeException(message)
