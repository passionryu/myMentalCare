package com.mymentalcare.server.application.admin

class AdminMemberNotFoundException : RuntimeException("관리 대상 회원을 찾을 수 없습니다.")

class AdminMemberInvalidStatusException(message: String) : RuntimeException(message)
