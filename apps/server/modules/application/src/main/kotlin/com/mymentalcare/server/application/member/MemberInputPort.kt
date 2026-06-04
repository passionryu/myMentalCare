package com.mymentalcare.server.application.member

interface MemberInputPort {
    fun signUp(request: SignUpMemberRequest): SignUpMemberResponse
}
