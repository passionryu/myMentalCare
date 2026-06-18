package com.mymentalcare.server.application.member.port

import com.mymentalcare.server.application.member.request.ChangeMemberPasswordRequest
import com.mymentalcare.server.application.member.request.ChangeMemberPasswordResponse
import com.mymentalcare.server.application.member.request.WithdrawMemberRequest
import com.mymentalcare.server.application.member.request.WithdrawMemberResponse
import com.mymentalcare.server.application.member.response.MemberLoginMethodsResponse

interface MemberSecurityInputPort {
    fun withdrawMyAccount(memberId: Long, request: WithdrawMemberRequest): WithdrawMemberResponse

    fun readLoginMethods(memberId: Long): MemberLoginMethodsResponse

    fun changePassword(memberId: Long, request: ChangeMemberPasswordRequest): ChangeMemberPasswordResponse
}
