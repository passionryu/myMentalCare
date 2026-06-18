package com.mymentalcare.server.application.member.usecase

import com.mymentalcare.server.application.auth.port.RefreshTokenStore
import com.mymentalcare.server.application.member.MemberNotFoundException
import com.mymentalcare.server.application.member.MemberPasswordChangeFailedException
import com.mymentalcare.server.application.member.MemberWithdrawalFailedException
import com.mymentalcare.server.application.member.isPasswordLoginEnabled
import com.mymentalcare.server.application.member.port.MemberRepository
import com.mymentalcare.server.application.member.port.MemberSecurityInputPort
import com.mymentalcare.server.application.member.port.SocialAccountRepository
import com.mymentalcare.server.application.member.request.ChangeMemberPasswordRequest
import com.mymentalcare.server.application.member.request.ChangeMemberPasswordResponse
import com.mymentalcare.server.application.member.request.WithdrawMemberRequest
import com.mymentalcare.server.application.member.request.WithdrawMemberResponse
import com.mymentalcare.server.application.member.response.MemberLoginMethodsResponse
import com.mymentalcare.server.application.member.response.MemberSocialAccountResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class MemberSecurityService(
    private val memberRepository: MemberRepository,
    private val refreshTokenStore: RefreshTokenStore,
    private val socialAccountRepository: SocialAccountRepository,
    private val passwordEncoder: PasswordEncoder,
) : MemberSecurityInputPort {
    // 비밀번호와 확인 문구를 검증한 뒤 회원을 탈퇴 처리한다.
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

    private companion object {
        const val MEMBER_WITHDRAWAL_CONFIRMATION_TEXT = "회원 탈퇴"
    }
}
