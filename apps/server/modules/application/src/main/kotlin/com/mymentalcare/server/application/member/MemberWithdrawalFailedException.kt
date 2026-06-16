package com.mymentalcare.server.application.member

class MemberWithdrawalFailedException(
    message: String = "회원 탈퇴 확인 정보를 다시 확인해주세요.",
) : RuntimeException(message)
