# myMentalCare

에이전트 및 하네스 공부도 하면서, 혼자 멘탈 케어를 꾸준히 기록하고 관리하기 위해 만드는 개인 서비스입니다.

## Product Direction

- AI 챗봇
- RAG
- 디스코드 웹 훅

## Tech Stack

- Frontend: Next.js 16, React 19, TypeScript, Tailwind CSS
- Backend: Kotlin 1.9.25, Spring Boot 3.5.8, Java 21
- Database: MariaDB 11
- Cache/Token Store: Redis 7
- API Docs: springdoc-openapi Swagger UI
- Harness: `/Users/rsy/Desktop/myPlayGround/harness`

## Structure

```text
myMentalCare/
├── apps/
│   ├── web/      # Next.js frontend
│   └── server/   # Kotlin/Spring Boot backend
├── packages/
│   └── shared/   # shared types/contracts
├── docs/
│   ├── product/
│   └── api/
└── .github/
    └── ISSUE_TEMPLATE/
```

## Local Commands

```bash
pnpm install
pnpm dev:web
pnpm infra:up
pnpm dev:server
```

기본 포트:

- Web: `http://localhost:3000`
- API: `http://localhost:3001`
- Swagger: `http://localhost:3001/swagger-ui/index.html`
- MariaDB: `localhost:3310`
- Redis: `localhost:6380`

## Harness Workflow

하네스는 GitHub issue와 로컬 repo를 기준으로 동작한다.

```bash
cd /Users/rsy/Desktop/myPlayGround/harness
harness sync --issue {issue_number}
harness plan --issue {issue_number}
harness approve --issue {issue_number} --stage plan --approved-by rsy
harness develop --issue {issue_number}
harness approve --issue {issue_number} --stage dev --approved-by rsy
harness qa --issue {issue_number}
```
