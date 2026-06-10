# Grafana 에러 로그 대시보드 및 알림 구성

## 목적

myMentalCare 백엔드의 장애 징후를 Grafana에서 빠르게 확인한다.
운영자는 전체 로그, ERROR/Exception, 로그인 실패, OpenAI 호출 실패, Redis/DB 연결 실패, 위기 감지 로그를 한 화면에서 확인한다.

## 사전 조건

- Loki/Grafana/Alloy 로컬 스택이 실행되어 있어야 한다.
- API 서버는 `LOG_PATH=/Users/rsy/Desktop/myPlayGround/myMentalCare/apps/server/logs`로 실행되어야 한다.
- Discord 알림을 쓰려면 로컬 환경변수 `GRAFANA_DISCORD_WEBHOOK_URL`을 실제 Discord Webhook URL로 설정한다.
- 값을 설정하지 않으면 Grafana 기동 검증을 위해 `http://localhost:9` placeholder가 사용된다.

## 실행

```bash
cd /Users/rsy/Desktop/myPlayGround/myMentalCare/apps/server
docker compose -f docker-compose.monitoring.yml up -d
```

## 접속

- Grafana: http://localhost:3002
- Dashboard: `myMentalCare Observability / myMentalCare 에러 로그 모니터링`

## 주요 LogQL

전체 백엔드 로그를 본다.

```logql
{service="mymentalcare-api"}
```

ERROR 로그를 본다.

```logql
{service="mymentalcare-api"} |= "ERROR"
```

예외 로그를 본다.

```logql
{service="mymentalcare-api"} |= "Exception"
```

로그인 실패 로그를 본다.

```logql
{service="mymentalcare-api"} |= "[로그인]" |= "실패"
```

OpenAI 호출 실패 로그를 본다.

```logql
{service="mymentalcare-api"} |~ "OpenAI|AI 마음 대화" |~ "실패|ERROR|Exception"
```

Redis/DB 연결 실패 로그를 본다.

```logql
{service="mymentalcare-api"} |~ "Redis|Hikari|MariaDB|JDBC|database|Connection" |~ "ERROR|Exception|failed|실패"
```

위기 감지 운영 로그를 본다.

```logql
{service="mymentalcare-api"} |~ "위기|crisis|Crisis"
```

## Discord 알림

Grafana alerting provisioning은 `GRAFANA_DISCORD_WEBHOOK_URL`을 참조한다.
이 값은 코드에 커밋하지 않는다.
값이 비어 있으면 Grafana가 alerting provisioning 단계에서 실패할 수 있으므로, docker compose에는 무해한 placeholder 기본값을 둔다.

```bash
export GRAFANA_DISCORD_WEBHOOK_URL="Discord Webhook URL"
docker compose -f docker-compose.monitoring.yml up -d
```

## 민감정보 정책

대시보드와 알림은 로그 원문을 기반으로 동작한다.
따라서 애플리케이션 로그에는 사용자 채팅 원문, 감정 기록 원문, 토큰, 비밀번호, OpenAI API Key, Discord Webhook URL을 남기지 않는다.

## 검증 방법

1. API 서버를 실행한다.
2. `GET /actuator/health`, 로그인 실패 요청 등 예시 요청을 만든다.
3. Grafana 대시보드에서 로그가 조회되는지 확인한다.
4. ERROR 테스트 로그를 남긴 뒤 alert rule이 평가되는지 확인한다.
