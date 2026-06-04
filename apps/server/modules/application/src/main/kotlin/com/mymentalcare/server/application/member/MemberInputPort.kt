package com.mymentalcare.server.application.member

interface MemberInputPort {
    fun signUp(request: SignUpMemberRequest): SignUpMemberResponse

    fun readMyProfile(memberId: Long): MyProfileResponse
}
