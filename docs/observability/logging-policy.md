# 백엔드 로그 출력 및 민감정보 로깅 정책

## 목적

myMentalCare API 서버의 장애 원인을 로컬에서 빠르게 확인할 수 있도록 파일 로그를 남긴다.
로그는 운영자와 개발자가 문제를 추적하기 위한 기록이며, 사용자의 민감한 대화나 인증 정보를 노출하지 않는다.

## 로그 파일

- 기본 위치: `logs/mymentalcare-api.log`
- 롤링 정책: 일 단위 + 20MB 단위 압축
- 보관 기간: 14일
- 총 보관 용량: 1GB

## 로그 식별자

모든 HTTP 요청은 `X-Request-Id`를 가진다.
요청자가 헤더를 보내지 않으면 서버가 새 ID를 만들고 응답 헤더에도 같은 값을 반환한다.

로그 패턴에는 다음 값이 포함된다.

- `traceId`
- `requestId`
- logger
- message

## 허용하는 로그 정보

- API path
- HTTP method
- HTTP status
- 처리 시간
- 회원 ID처럼 CS 확인에 필요한 식별자
- 실패 사유를 설명하는 안전한 코드성 메시지

## 금지하는 로그 정보

- 비밀번호 원문
- Access Token / Refresh Token 원문
- OpenAI API Key / Discord Webhook URL
- 사용자의 전체 채팅 원문
- 민감한 감정 기록 전문

## 실패 로그 작성 규칙

실패 로그는 가능하면 아래 key를 유지한다.

- `who`: 행위 주체
- `what`: 수행한 API 또는 메서드
- `requestData`: 요청 정보. 민감정보는 마스킹하거나 생략한다.
- `reason`: 실패 원인

예상 가능한 검증 실패는 `warn`, 시스템 장애나 추적이 필요한 예외는 `error`로 남긴다.
