package com.mymentalcare.server.application.member.port

import com.mymentalcare.server.application.member.request.SignUpMemberRequest
import com.mymentalcare.server.application.member.response.SignUpMemberResponse

interface MemberRegistrationInputPort {
    fun signUp(request: SignUpMemberRequest): SignUpMemberResponse
}
