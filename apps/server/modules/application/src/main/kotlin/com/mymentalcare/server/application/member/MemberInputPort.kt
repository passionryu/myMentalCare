package com.mymentalcare.server.application.member

interface MemberInputPort {
    fun signUp(request: SignUpMemberRequest): SignUpMemberResponse

    fun readMyProfile(memberId: Long): MyProfileResponse

    fun updateMyProfile(memberId: Long, request: UpdateMyProfileRequest): MyProfileResponse

    fun readNotificationSetting(memberId: Long): MemberNotificationSettingResponse

    fun updateNotificationSetting(memberId: Long, request: MemberNotificationSettingRequest): MemberNotificationSettingResponse
}
