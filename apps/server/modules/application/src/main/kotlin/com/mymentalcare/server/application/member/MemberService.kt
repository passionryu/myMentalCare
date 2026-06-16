package com.mymentalcare.server.application.member

import com.mymentalcare.server.application.common.extension.logWarn
import com.mymentalcare.server.application.port.MemberNotificationSettingRepository
import com.mymentalcare.server.application.port.MemberRepository
import com.mymentalcare.server.application.port.RefreshTokenStore
import com.mymentalcare.server.application.port.SocialAccountRepository
import com.mymentalcare.server.domain.auth.SocialAccount
import com.mymentalcare.server.domain.member.MemberNotificationSetting
import com.mymentalcare.server.domain.member.Member
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime

@Service
internal class MemberService(
    private val memberRepository: MemberRepository,
    private val notificationSettingRepository: MemberNotificationSettingRepository,
    private val refreshTokenStore: RefreshTokenStore,
    private val socialAccountRepository: SocialAccountRepository,
    private val passwordEncoder: PasswordEncoder,
) : MemberInputPort {
    // 회원가입
    @Transactional
    override fun signUp(request: SignUpMemberRequest): SignUpMemberResponse {
        if (memberRepository.existsByLoginId(request.loginId)) {
            logWarn {
                "[회원가입] 로그인 아이디 중복으로 회원가입 실패. " +
                    "who=anonymous, " +
                    "what=POST /api/members/signup, " +
                    "requestData=loginId:${request.loginId},email:${request.email ?: "none"}, " +
                    "reason=duplicateLoginId"
            }
            throw DuplicateLoginIdException()
        }

        val member = memberRepository.save(
            Member(
                id = 0,
                loginId = request.loginId,
                email = request.email?.takeIf { it.isNotBlank() },
                password = passwordEncoder.encode(request.password),
                name = request.name,
                phone = request.phone?.takeIf { it.isNotBlank() },
            )
        )

        return SignUpMemberResponse(
            memberId = member.id,
            loginId = member.loginId,
            name = member.name,
        )
    }

    // 내 프로필 조회
    @Transactional(readOnly = true)
    override fun readMyProfile(memberId: Long): MyProfileResponse {
        val member = memberRepository.findById(memberId)
            ?: throw MemberNotFoundException()

        return member.toMyProfileResponse()
    }

    // 내 프로필 수정
    @Transactional
    override fun updateMyProfile(memberId: Long, request: UpdateMyProfileRequest): MyProfileResponse {
        val member = memberRepository.findById(memberId)
            ?: throw MemberNotFoundException()

        val nextEmail = request.email.normalizeBlank()
        if (nextEmail != null) {
            val emailOwner = memberRepository.findByEmail(nextEmail)
            if (emailOwner != null && emailOwner.id != memberId) {
                throw DuplicateEmailException()
            }
        }

        val updatedMember = memberRepository.save(
            member.copy(
                email = nextEmail,
                name = request.name.trim(),
                phone = request.phone.normalizeBlank(),
            )
        )

        return updatedMember.toMyProfileResponse()
    }

    @Transactional(readOnly = true)
    override fun readNotificationSetting(memberId: Long): MemberNotificationSettingResponse {
        memberRepository.findById(memberId) ?: throw MemberNotFoundException()

        return (notificationSettingRepository.findByMemberId(memberId) ?: defaultNotificationSetting(memberId))
            .toNotificationSettingResponse()
    }

    @Transactional
    override fun updateNotificationSetting(memberId: Long, request: MemberNotificationSettingRequest): MemberNotificationSettingResponse {
        memberRepository.findById(memberId) ?: throw MemberNotFoundException()

        val currentSetting = notificationSettingRepository.findByMemberId(memberId)
        val savedSetting = notificationSettingRepository.save(
            MemberNotificationSetting(
                id = currentSetting?.id ?: 0,
                memberId = memberId,
                enabled = request.enabled,
                notificationTime = request.notificationTime,
                weekdays = request.weekdays.distinct(),
            )
        )

        return savedSetting.toNotificationSettingResponse()
    }

    @Transactional
    override fun withdrawMyAccount(memberId: Long, request: WithdrawMemberRequest): WithdrawMemberResponse {
        val member = memberRepository.findById(memberId)
            ?: throw MemberNotFoundException()

        if (request.confirmationText.trim() != MEMBER_WITHDRAWAL_CONFIRMATION_TEXT) {
            throw MemberWithdrawalFailedException("회원 탈퇴 확인 문구를 정확히 입력해주세요.")
        }

        if (!passwordEncoder.matches(request.password, member.password)) {
            throw MemberWithdrawalFailedException("비밀번호가 일치하지 않습니다.")
        }

        memberRepository.withdraw(member)
        refreshTokenStore.deleteRefreshToken(memberId)

        return WithdrawMemberResponse(withdrawn = true)
    }

    @Transactional(readOnly = true)
    override fun readLoginMethods(memberId: Long): MemberLoginMethodsResponse {
        val member = memberRepository.findById(memberId)
            ?: throw MemberNotFoundException()
        val socialAccounts = socialAccountRepository.findByMemberId(memberId)
        val passwordLoginEnabled = member.isPasswordLoginEnabled(socialAccounts)

        return MemberLoginMethodsResponse(
            passwordLoginEnabled = passwordLoginEnabled,
            canChangePassword = passwordLoginEnabled,
            socialAccounts = socialAccounts.map {
                MemberSocialAccountResponse(
                    provider = it.provider.name,
                    email = it.email,
                    linkedAt = it.linkedAt.toString(),
                )
            },
        )
    }

    @Transactional
    override fun changePassword(memberId: Long, request: ChangeMemberPasswordRequest): ChangeMemberPasswordResponse {
        val member = memberRepository.findById(memberId)
            ?: throw MemberNotFoundException()
        val socialAccounts = socialAccountRepository.findByMemberId(memberId)

        if (!member.isPasswordLoginEnabled(socialAccounts)) {
            throw MemberPasswordChangeFailedException("카카오 로그인 전용 계정은 이 화면에서 비밀번호를 변경할 수 없습니다.")
        }

        if (!passwordEncoder.matches(request.currentPassword, member.password)) {
            throw MemberPasswordChangeFailedException("현재 비밀번호가 일치하지 않습니다.")
        }

        val normalizedNewPassword = request.newPassword.trim()
        if (normalizedNewPassword.length < 8) {
            throw MemberPasswordChangeFailedException("새 비밀번호는 8자 이상 입력해주세요.")
        }

        memberRepository.save(member.copy(password = passwordEncoder.encode(normalizedNewPassword)))
        refreshTokenStore.deleteRefreshToken(memberId)

        return ChangeMemberPasswordResponse(changed = true)
    }

    private fun Member.toMyProfileResponse(): MyProfileResponse {
        return MyProfileResponse(
            memberId = id,
            loginId = loginId,
            email = email,
            name = name,
            phone = phone,
        )
    }

    private fun defaultNotificationSetting(memberId: Long): MemberNotificationSetting {
        return MemberNotificationSetting(
            id = 0,
            memberId = memberId,
            enabled = false,
            notificationTime = LocalTime.of(21, 0),
            weekdays = listOf("MON", "TUE", "WED", "THU", "FRI"),
        )
    }

    private fun MemberNotificationSetting.toNotificationSettingResponse(): MemberNotificationSettingResponse {
        return MemberNotificationSettingResponse(
            enabled = enabled,
            notificationTime = notificationTime.toString(),
            weekdays = weekdays,
        )
    }

    private fun String?.normalizeBlank(): String? = this?.trim()?.takeIf { it.isNotBlank() }

    private fun Member.isPasswordLoginEnabled(socialAccounts: List<SocialAccount>): Boolean {
        return socialAccounts.isEmpty() || !loginId.startsWith("kakao_")
    }

    private companion object {
        const val MEMBER_WITHDRAWAL_CONFIRMATION_TEXT = "회원 탈퇴"
    }
}
