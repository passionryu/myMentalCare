package com.mymentalcare.server.application.auth

class LoginFailedException(
    message: String = "로그인 정보를 다시 확인해주세요.",
) : RuntimeException(message)
