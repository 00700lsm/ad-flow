# Plan

```text
Task: T7-06
Phase: 7
HITL: pending
```

## 완료 조건

```text
POST /simulations/{id}/start 가 선택마다 Impression을 남긴다
Dashboard 노출이 그 횟수만큼 오른다
stop 후 start면 Impression을 안 낸다
Click 확률 / 30대 시드 / k6 / Kafka는 포함하지 않는다
```

## Red Tests

실패해야 하는 테스트 목록.

```text
1. SimulationApiTest.startRecordsImpressionsOnDashboard
   캠페인 후 start concurrentUsers=2
   requestCount=2, spentBudget=2
   (워커 대기) dashboard impressions=2
   지금 impressions=0

2. SimulationApiTest.stopBeforeStartIssuesNoImpressions
   create → stop → start
   requestCount=0, impressions=0
   지금 start가 선택을 안 하므로 이미 0일 수 있음
   start가 선택을 하면서 Impression만 빼먹으면 이 테스트는 통과하고
   1번이 실패해야 한다. 1번이 가짜가 되지 않게 Impression을 테스트가 POST하지 않는다
```

가짜 테스트: 테스트가 `/events/impression`을 직접 2회 호출.
가짜 테스트: impressions 필드 존재만 보고 값이 0이어도 통과.

기존 startSelectsAdsThatManyTimes의 spentBudget=2는 유지.

## Green 최소 구현

```text
SimulationService.start
  select 후 AdEventService.accept(고유 eventId, campaignId, creativeId, userId, contentId, IMPRESSION)
  Player와 같은 이벤트 경로. 새 엔드포인트 없음
Click 없음
simulator.html 변경 없음
```

## 검증 명령

```text
./gradlew test --tests SimulationApiTest --tests SimulatorPageTest
./gradlew test
```

## 하지 않는 것

```text
Click 확률, 30대 시드
Kafka / k6 / SSE / Redis
FR-14 전체, Experiment RPS
Frequency Cap / Budget / UNIQUE 변경
새 /events 계약
```

## HITL

구현 전에 개발자 승인. 승인 문장 예: `승인` / `T7-06 승인`

```text
하는 것  start → select마다 Impression → Dashboard 노출
하지 않는 것 Click, 30대, k6
```

STOP. 승인 전에 Impression 루프 코드를 쓰지 않는다.
