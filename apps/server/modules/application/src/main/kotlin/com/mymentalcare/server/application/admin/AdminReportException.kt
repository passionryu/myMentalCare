package com.mymentalcare.server.application.admin

class AdminReportNotFoundException : RuntimeException("관리 대상 리포트를 찾을 수 없습니다.")

class AdminChatRoomNotFoundException : RuntimeException("관리 대상 대화방을 찾을 수 없습니다.")

class AdminSensitiveAccessReasonRequiredException : RuntimeException("원문 대화 조회 사유가 필요합니다.")
