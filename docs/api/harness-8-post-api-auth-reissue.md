# Refresh Token 기반 Access Token 재발급 API 구현 API Contract

## Endpoint

- method/path: `POST /api/auth/reissue`
- auth: 결정 필요

## Request

```json
{}
```

## Response

```json
{}
```

## Error Policy

- 사용자에게 반환되는 메시지는 한국어로 작성한다.
- 내부 구현 정보나 민감정보를 사용자 응답에 노출하지 않는다.
- 로그에는 who/what/requestData/reason 형식을 우선 사용한다.

## Implementation Boundary

- Controller는 얇게 유지한다.
- Request/Response DTO는 Controller 파일에서 분리한다.
- 유스케이스 흐름은 application service에서 읽히도록 유지한다.
- 실제 구현 전 이 contract를 사람 또는 Plan Agent가 보강해야 한다.