package com.mymentalcare.server.application.admin.port

import com.mymentalcare.server.application.admin.request.AdminMemberSearchRequest
import com.mymentalcare.server.application.admin.request.AdminMemberStatusChangeRequest
import com.mymentalcare.server.application.admin.response.AdminMemberDetailResponse
import com.mymentalcare.server.application.admin.response.AdminMemberPageResponse

interface AdminMemberQueryInputPort {
    fun readMembers(request: AdminMemberSearchRequest): AdminMemberPageResponse

    fun readMember(adminMemberId: Long, memberId: Long): AdminMemberDetailResponse
}

interface AdminMemberCommandInputPort {
    fun changeMemberStatus(request: AdminMemberStatusChangeRequest): AdminMemberDetailResponse
}
