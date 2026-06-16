package com.mymentalcare.server.application.member

class MemberPasswordChangeFailedException(
    message: String = "비밀번호 변경 정보를 다시 확인해주세요.",
) : RuntimeException(message)
