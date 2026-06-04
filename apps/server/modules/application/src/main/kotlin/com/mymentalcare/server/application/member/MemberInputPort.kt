package com.mymentalcare.server.application.member

interface MemberInputPort {
    fun readMyProfile(memberId: Long): MyProfileResponse
}
