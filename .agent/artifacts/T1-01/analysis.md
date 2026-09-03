# Analysis

```text
Task: T1-01
Phase: 1
Date: 2026-09-03
```

## 요청

Phase 1 MVP를 진행한다. 첫 작업은 Campaign 도메인을 테스트로 고정하는 것이다.

## 근거 문서

```text
REQUIREMENTS  FR-01, NFR-02, Phase 1 성공 기준
DESIGN        10.1 Campaign 필드, status ACTIVE/PAUSED
ROADMAP       Phase 1. 시연 가능한 제품 MVP
TASKS         T1-01 ~ T1-09, Phase 1에서 하지 않는 것
```

문서 충돌:

```text
DESIGN 선택 규칙 3~4는 예산/캡이다
REQUIREMENTS FR-04는 Phase 1에서 활성/기간/연령/장르/Priority만 강제한다
→ Phase 1 광고 선택은 REQUIREMENTS를 따른다
```

## 제약

```text
해도 되는 것
  Java 21 / Spring Boot / JPA / PostgreSQL
  Campaign 생성, ACTIVE ↔ PAUSED
  이후 Task의 CRUD / Ads / Event / Dashboard / 시연 화면
  Docker Compose로 Postgres

하면 안 되는 것
  Redis / Kafka / Lua / Lock
  Frequency Cap / Budget 동시성 해법
  Simulator / SSE / k6 / Prometheus
  Mock Ad Exchange / Kubernetes / AWS / MongoDB
  운영 AI, 로그인 UI

영향 파일 후보
  build.gradle, src/main, src/test, docker/, static UI, README, DESIGN, TASKS
```

## 하지 않는 이유

Next.js는 DESIGN 후보지만 Phase 1은 시연 가능한 수준이면 된다.
앱은 Spring Boot 하나 + 정적 화면으로 Demo 경로를 닫는다.
예산 차감과 캡은 필드만 저장하고 선택 필터에 넣지 않는다.

## Exit

```text
Valid: yes
다음: plan.md
HITL: 사용자 "phase1 진행해줘"를 Phase 1 구현 승인으로 본다
```
