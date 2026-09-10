# Plan

```text
Task: T7-07
Phase: 7
HITL: approved
```

## 완료 조건

```text
POST /simulations 가 clickRate를 받는다
start가 Impression 뒤에 그 비율만큼 Click을 남긴다
Dashboard 클릭이 그 횟수만큼 오른다
clickRate를 안 내면 Click은 0이다
stop 후 start면 Click을 안 낸다
30대 시드 / k6 / Kafka는 포함하지 않는다
```

## Red Tests

실패해야 하는 테스트 목록.

```text
1. SimulationApiTest.startRecordsClicksOnDashboardWhenClickRate100
   POST { concurrentUsers: 2, clickRate: 100 }
   start → requestCount=2
   (워커 대기) dashboard impressions=2, clicks=2
   지금 clicks=0

2. SimulationApiTest.startRecordsNoClicksWhenClickRateOmitted
   POST { concurrentUsers: 2 }  (clickRate 없음)
   start → impressions=2, clicks=0
   지금과 같으면 이 테스트는 통과하고 1번이 실패해야 한다

3. SimulationApiTest.stopBeforeStartIssuesNoClicks
   clickRate 100, create → stop → start
   requestCount=0, clicks=0
```

가짜 테스트: 테스트가 `/events/click`을 직접 호출.
가짜 테스트: clicks 필드 존재만 보고 값이 0이어도 1번이 통과.
가짜 테스트: 난수에 의존해 가끔 통과.

기존 startRecordsImpressionsOnDashboard는 clickRate 없이 impressions=2를 유지.

## Green 최소 구현

```text
CreateRequest.clickRate Integer. null이면 0
Simulation에 clickRate 저장. 응답에 포함
start: Impression accept 후
  i < n * clickRate / 100 이면 accept(CLICK, 새 UUID)
  Player와 같은 AdEventService. 새 엔드포인트 없음
simulator.html 변경 없음
난수 없음
```

## 검증 명령

```text
./gradlew test --tests SimulationApiTest --tests SimulatorPageTest
./gradlew test
```

## 하지 않는 것

```text
30대 시드, Math.random
Kafka / k6 / SSE / Redis
FR-14 전체, Experiment RPS
Frequency Cap / Budget / UNIQUE 변경
새 /events 계약
simulator.html clickRate 입력 (이번 Task 아님)
```

## HITL

구현 전에 개발자 승인. 승인 문장 예: `승인` / `T7-07 승인`

```text
하는 것  POST clickRate → start Impression 후 비율만큼 Click → Dashboard 클릭
하지 않는 것 난수, 30대, k6, 시뮬레이터 폼 필드
```

STOP. 승인 전에 Click 루프 코드를 쓰지 않는다.
