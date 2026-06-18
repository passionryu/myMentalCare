# Backend Controller and Service Responsibility Audit

## Purpose

This document records the responsibility audit for issue #73.

The goal is not to split files mechanically. The goal is to keep each controller and application service aligned with one clear reason to change:

- HTTP boundary mapping
- use case orchestration
- domain decision
- external system boundary
- persistence port boundary

API paths and JSON contracts must remain stable unless a separate API migration issue explicitly changes them.

## Audit Result

### AI Chat

Before:

- `AiChatController` handled room, message, check-in, history, and report endpoints.
- `AiChatService` handled room lookup, message sending, check-in recording, history deletion, report readiness, report creation, and report lookup.
- `AiChatInputPort` exposed all AI chat related use cases through one broad port.

After:

- Controllers:
  - `AiChatRoomController`
  - `AiChatMessageController`
  - `AiChatCheckInController`
  - `AiChatReportController`
  - `AiChatHistoryController`
- Input ports:
  - `AiChatRoomInputPort`
  - `AiChatMessageInputPort`
  - `AiChatCheckInInputPort`
  - `AiChatReportInputPort`
  - `AiChatHistoryInputPort`
- Services:
  - `AiChatRoomService`
  - `AiChatMessageService`
  - `AiChatCheckInService`
  - `AiChatReportService`
  - `AiChatHistoryService`

Report boundary decision:

For now, reports remain under `aichat` as `AiChatReport`.

Reason:

- Current report creation depends directly on today's AI chat room and chat messages.
- Current report readiness is calculated from room messages.
- Current report lookup API is still exposed under `/api/ai-chat`.

Move to an independent `report` domain only when reports become independently managed artifacts, such as shareable reports, cross-room reports, long-term analytics, or report-only recommendations.

### Member

Before:

- `MemberController` handled signup, profile, notification settings, withdrawal, login methods, and password changes.
- `MemberService` handled registration, profile, notification, and security/account flows.
- `MemberInputPort` exposed all member use cases through one broad port.

After:

- Controllers:
  - `MemberRegistrationController`
  - `MemberProfileController`
  - `MemberNotificationController`
  - `MemberSecurityController`
- Input ports:
  - `MemberRegistrationInputPort`
  - `MemberProfileInputPort`
  - `MemberNotificationInputPort`
  - `MemberSecurityInputPort`
- Services:
  - `MemberRegistrationService`
  - `MemberProfileService`
  - `MemberNotificationService`
  - `MemberSecurityService`

### Kakao Auth

Before:

- `KakaoAuthController` handled OAuth redirect response construction directly.
- `KakaoAuthenticationService` handled OAuth state, callback exchange, member provisioning, token issuing, and opaque token generation.

After:

- `KakaoOAuthRedirectResponseFactory` owns web callback redirect construction.
- `OAuthOpaqueTokenGenerator` owns OAuth state and one-time code token generation.
- `KakaoMemberProvisioner` owns Kakao social account lookup and member creation.
- `KakaoAuthenticationService` now focuses on Kakao OAuth flow orchestration.

### Smaller Domains

`InquiryController` / `InquiryService` and `MyPageController` / `MyPageService` remain unchanged.

Reason:

- They currently have small method counts and a single clear use case boundary.
- Splitting them now would add indirection without reducing meaningful responsibility.

## Verification

Run:

```bash
./gradlew :modules:application:compileKotlin :modules:bootstrap:mymentalcare:compileKotlin
./gradlew test
```

Both commands passed during issue #73 implementation.
