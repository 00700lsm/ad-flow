# Plan

```text
Task: T7-05
Phase: 7
HITL: approved
```

## 완료 조건

```text
GET /simulator.html 이 200이다
index에 Simulator 링크가 있다
화면에서 concurrentUsers·20대/40대·스포츠/드라마를 넣고
POST /simulations 와 start / stop 을 호출한다
Impression 루프 / 30대 시드 / k6 / Kafka는 포함하지 않는다
```

## Red Tests

실패해야 하는 테스트 목록.

```text
1. SimulatorPageTest.simulatorHtmlIsServed
   GET /simulator.html → 200
   지금 404. SimulatorGapTest는 파일 없음을 기대

2. SimulatorPageTest.indexLinksToSimulator
   GET / 또는 index.html 에 simulator.html
   지금 링크 없음

3. SimulatorPageTest.pagePostsSimulationsAndStartStop
   HTML에 /simulations, /start, /stop
   concurrentUsers, 20대, 40대, 스포츠, 드라마
   지금 페이지 없음
```

가짜 테스트: 빈 simulator.html만 두고 POST를 안 부름.
가짜 테스트: 테스트가 MockMvc로 API만 치고 화면은 없음.

SimulatorGapTest의 “없음” 기대는 이 Task에서 제거한다. 페이지 테스트가 대신한다.

## Green 최소 구현

```text
static/simulator.html
  폼: concurrentUsers, ageShares 20대·40대, categories 스포츠·드라마
  Simulation → POST /simulations 후 POST /{id}/start
  Stop → POST /{id}/stop
  requestCount 표시
index.html · console/player/dashboard nav에 Simulator 링크
기존 Simulation API 변경 없음
```

## 검증 명령

```text
./gradlew test --tests SimulatorPageTest --tests SimulationApiTest
./gradlew test
수동: /simulator.html 에서 start 후 Dashboard spentBudget
```

## 하지 않는 것

```text
Impression / Click 루프
30대 시드, 예능, 실시간 Ad Requests/s
Redis / Kafka / k6 / SSE
FR-14 전체, Experiment RPS
Frequency Cap / Budget / UNIQUE 변경
```

## HITL

구현 전에 개발자 승인. 승인 문장 예: `승인` / `T7-05 승인`

```text
하는 것  simulator.html이 T7-04 API를 호출
하지 않는 것 Impression, 30대, k6
```

STOP. 승인 전에 simulator.html을 쓰지 않는다.
