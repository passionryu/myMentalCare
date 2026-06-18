package com.mymentalcare.server.application.member.port

import com.mymentalcare.server.application.member.request.UpdateMyProfileRequest
import com.mymentalcare.server.application.member.response.MyProfileResponse

interface MemberProfileInputPort {
    fun readMyProfile(memberId: Long): MyProfileResponse

    fun updateMyProfile(memberId: Long, request: UpdateMyProfileRequest): MyProfileResponse
}
