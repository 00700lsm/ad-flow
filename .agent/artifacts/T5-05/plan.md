# Plan

```text
Task: T5-05
Phase: 5
HITL: approved
```

## 완료 조건

```text
같은 eventId Impression을 세 번내면 Dashboard 노출이 1이다
전환은 Dashboard 노출 숫자로 읽는다
시연 경로는 README에 같은 eventId POST 3회로 적힌다
관찰을 artifacts/T5-05에 남긴다
Player는 재생마다 새 eventId라 이 시연의 입력이 아님을 적는다
UNIQUE / 새 HTTP / SSE는 포함하지 않는다
```

## Red Tests

```text
1. DashboardPageTest.dashboardHtmlMentionsDuplicateEventIdCountsOnce
   GET /dashboard.html
   같은 eventId는 노출이 한 번이라는 안내가 있다
   구현 전 실패
```

가짜 테스트: DuplicateEventIdAggregationTest만 다시 돌리기. 화면이 아니다.
Playwright는 도입하지 않는다. 화면 확인은 수동 Demo다.

## Green 최소 구현

```text
dashboard.html
  muted 한 줄: 같은 eventId는 노출·클릭 집계가 한 번 (ADR 009)

index.html
  Phase 5 시연 한 줄

README.md
  같은 eventId로 POST /events/impression 3회
  /dashboard.html 해당 캠페인 노출 = 1
  Player 반복 재생은 키가 달라서 FR-12 시연이 아님

artifacts/T5-05/observation.md
  승인 후 curl + Dashboard(또는 동일 경로)로 확인한 결과만
  확인하지 않고 시연됐다고 쓰지 않는다

AdEvent / AdEventService 변경 없음
```

## 검증 명령

```text
./gradlew test --tests DashboardPageTest
./gradlew test
수동 Demo:
  docker compose + bootRun
  캠페인·크리에이티브 1개 (Console 또는 기존)
  curl POST /events/impression 동일 eventId 3회 → 201
  /dashboard.html 노출 1
```

## 하지 않는 것

```text
SSE, Kafka, Redis, 새 엔드포인트, UNIQUE 변경
Player uuid 고정
Playwright / Simulator
```

## HITL

구현 전에 개발자 승인. 승인 문장 예: `승인` / `T5-05 승인`

Trade-off: 시연 입력은 curl이다. Player는 매번 새 키라 노출이 늘어 보이는 것이 정상이다.

STOP. 승인 전에 HTML·README를 쓰지 않는다.
