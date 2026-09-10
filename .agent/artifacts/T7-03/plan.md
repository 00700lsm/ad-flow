# Plan

```text
Task: T7-03
Phase: 7
HITL: approved
```

## 완료 조건

```text
POST /simulations 로 concurrentUsers를 받는다
POST /simulations/{id}/start 가 그 횟수만큼 GET /ads 와 같은 선택을 한다
POST /simulations/{id}/stop 이 start 전이면 요청을 안 낸다
k6 / 분포 UI / Kafka는 포함하지 않는다
```

## Red Tests

```text
1. SimulationApiTest.createReturnsId
   POST /simulations { "concurrentUsers": 2 } → 201, id · concurrentUsers=2
   지금 404

2. SimulationApiTest.startSelectsAdsThatManyTimes
   캠페인+크리에이티브 후 start
   userId=1 contentId=1 로 2회 선택
   requestCount=2 (또는 spentBudget이 2회 선택만큼)
   지금 start 매핑 없음

3. SimulationApiTest.stopBeforeStartIssuesNoRequests
   create → stop → start 하면 선택 0
   지금 실패
```

가짜 테스트: 201만 주고 start가 GET /ads를 안 부름.
가짜 테스트: 테스트에서 MockMvc GET /ads 2회 = Simulator.

SimulatorGapTest: `/simulations` 없음 기대는 구현 후 깨진다. 화면(simulator.html · index) 공백만 남긴다.

## Green 최소 구현

```text
POST /simulations          메모리에 id, concurrentUsers, CREATED
POST .../start             샘플 User 1 · Content 1 로 AdServingService.select 를
                           concurrentUsers번 순차 호출. COMPLETED, requestCount
POST .../stop              CREATED → STOPPED. 이후 start는 요청 0
PostgreSQL 테이블 없음
simulator.html 없음
```

## 검증 명령

```text
./gradlew test --tests SimulationApiTest --tests SimulatorGapTest
./gradlew test
```

## 하지 않는 것

```text
simulator.html, 연령·장르 분포
가상 User INSERT, Impression/Click 루프
비동기 워커 / Redis / Kafka / k6 / SSE
FR-14 전체, Experiment RPS
Frequency Cap / Budget / UNIQUE 변경
```

## HITL

구현 전에 개발자 승인. 승인 문장 예: `승인` / `T7-03 승인`

STOP. 승인 전에 POST /simulations 코드를 쓰지 않는다.
