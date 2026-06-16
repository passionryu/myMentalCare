package com.mymentalcare.server.application.auth

class OAuthStateInvalidException(
    message: String = "로그인 요청 시간이 만료되었습니다.",
) : RuntimeException(message)
