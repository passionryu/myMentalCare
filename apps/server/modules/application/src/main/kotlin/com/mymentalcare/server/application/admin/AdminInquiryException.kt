package com.mymentalcare.server.application.admin

class AdminInquiryNotFoundException : RuntimeException("관리 대상 문의를 찾을 수 없습니다.")

class AdminInquiryInvalidRequestException(message: String) : RuntimeException(message)
