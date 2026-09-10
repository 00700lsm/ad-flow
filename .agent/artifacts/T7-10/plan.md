# Plan

```text
Task: T7-10
Phase: 7
HITL: approved
```

## 완료 조건

```text
README에 simulator.html → start → Dashboard 경로가 있다
시연 후 노출·클릭 또는 spentBudget이 오른 관찰을 artifacts/T7-10에 남긴다
k6 / Kafka / 연속 루프 / Phase 닫기는 포함하지 않는다
```

## Red Tests

실패해야 하는 테스트 목록.

```text
1. SimulatorPageTest.indexMentionsSimulatorThenDashboard
   GET /index.html
   Simulator와 Dashboard가 한 흐름으로 있다
   (예: Simulator 다음에 노출·클릭 또는 Dashboard)
   지금 index 5번은 요청만 내고 숫자를 안 적음
```

가짜 테스트: SimulationApiTest만 다시 돌리기. 시연 경로가 아니다.
Playwright / k6는 도입하지 않는다. 숫자 확인은 수동 Demo + observation.md다.

## Green 최소 구현

```text
README
  캠페인 하나 (또는 기존) 후 /simulator.html
  concurrentUsers, clickRate, Simulation
  /dashboard.html 에서 해당 캠페인 노출·클릭·spentBudget
  start는 한 번 순차. 연속 부하·k6 아님

index.html
  Simulator 다음에 Dashboard에서 노출·클릭·예산을 본다는 한 줄

artifacts/T7-10/observation.md
  승인 후 앱을 띄워 확인한 값만
  확인하지 않고 시연됐다고 쓰지 않는다

Java / SimulationService 변경 없음
```

## 검증 명령

```text
./gradlew test --tests SimulatorPageTest
./gradlew test
수동 Demo:
  docker compose + bootRun
  /simulator.html Simulation
  /dashboard.html 노출 또는 spentBudget
```

## 하지 않는 것

```text
k6, 워커 루프, Kafka, SSE
Phase 7 닫기, FR-14 충족으로 쓰기
Frequency Cap / Budget / UNIQUE 변경
새 HTTP
```

## HITL

구현 전에 개발자 승인. 승인 문장 예: `승인` / `T7-10 승인`

```text
하는 것  README·index 시연 경로 + 관찰
하지 않는 것 k6, 루프, Phase 닫기
```

STOP. 승인 전에 README 시연 절을 쓰지 않는다.
