# Loki-Grafana-Alloy 로컬 로그 모니터링

## 목적

로컬 개발 중 myMentalCare API 서버 로그를 파일로 남기고, Alloy가 해당 파일을 읽어 Loki로 전송한다.
개발자는 Grafana Explore에서 로그를 검색해 장애 원인과 요청 흐름을 확인한다.

## 실행

```bash
cd /Users/rsy/Desktop/myPlayGround/myMentalCare/apps/server
mkdir -p logs
docker compose -f docker-compose.monitoring.yml up -d
```

## 종료

```bash
cd /Users/rsy/Desktop/myPlayGround/myMentalCare/apps/server
docker compose -f docker-compose.monitoring.yml down
```

## 접속 정보

- Grafana: http://localhost:3002
- ID: `admin`
- Password: `admin`
- Loki API: http://localhost:3100
- Alloy UI: http://localhost:12345

## 로그 흐름

```text
myMentalCare API
-> logs/mymentalcare-api.log
-> Alloy
-> Loki
-> Grafana Explore
```

## LogQL 예시

전체 API 로그를 확인한다.

```logql
{service="mymentalcare-api"}
```

에러 로그를 확인한다.

```logql
{service="mymentalcare-api"} |= "ERROR"
```

예외 로그를 확인한다.

```logql
{service="mymentalcare-api"} |= "Exception"
```

특정 요청 ID를 확인한다.

```logql
{service="mymentalcare-api"} |= "requestId="
```

## 주의

- 모니터링 스택은 API/Web/MariaDB/Redis 실행과 분리된다.
- 모니터링 스택이 실패해도 애플리케이션 실행을 막지 않는다.
- `logs/*.log`는 Git에 올리지 않는다.
- 비밀번호, 토큰, OpenAI API Key, Discord Webhook URL, 사용자 채팅 원문은 로그에 남기지 않는다.
