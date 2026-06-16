package com.mymentalcare.server.application.member

class DuplicateEmailException : RuntimeException("이미 사용 중인 이메일입니다.")
