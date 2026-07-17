package com.mymentalcare.server.application.admin.port

import java.time.LocalDate

interface AdminDashboardQueryRepository {
    fun countActiveMembers(): Long

    fun countMembersCreatedOn(date: LocalDate): Long

    fun countConversationsOn(date: LocalDate): Long

    fun countReportsOn(date: LocalDate): Long
}
