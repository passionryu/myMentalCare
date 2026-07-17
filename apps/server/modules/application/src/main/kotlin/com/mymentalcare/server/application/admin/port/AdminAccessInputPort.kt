package com.mymentalcare.server.application.admin.port

import com.mymentalcare.server.application.admin.response.AdminProfileResponse

interface AdminAccessInputPort {
    fun readAdminProfile(memberId: Long): AdminProfileResponse
}
