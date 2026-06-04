package com.mymentalcare.server.application.member

class MemberNotFoundException(
    message: String = "회원 정보를 찾을 수 없습니다.",
) : RuntimeException(message)
