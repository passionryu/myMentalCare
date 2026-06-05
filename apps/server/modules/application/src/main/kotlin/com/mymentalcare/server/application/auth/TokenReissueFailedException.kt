package com.mymentalcare.server.application.auth

class TokenReissueFailedException(
    message: String = "로그인이 만료되었습니다. 다시 로그인해주세요.",
) : RuntimeException(message)
