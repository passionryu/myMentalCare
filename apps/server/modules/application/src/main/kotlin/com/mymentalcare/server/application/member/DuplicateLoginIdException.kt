package com.mymentalcare.server.application.member

class DuplicateLoginIdException(
    message: String = "이미 존재하는 로그인 아이디입니다.",
) : RuntimeException(message)
