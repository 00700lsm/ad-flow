# Plan

```text
Task: T7-09
Phase: 7
HITL: pending
```

## 완료 조건

```text
simulator.html에 30대 %와 clickRate가 있다
POST /simulations body에 ageShares 30대와 clickRate가 들어간다
기존 20대·40대·장르·start/stop은 유지한다
새 API / k6 / Kafka는 포함하지 않는다
```

## Red Tests

실패해야 하는 테스트 목록.

```text
1. SimulatorPageTest.pagePostsThirtiesShareAndClickRate
   GET /simulator.html
   HTML에 30대, clickRate
   fetch body에 '30대' 와 clickRate
   지금 20대·40대만, clickRate 키 없음
```

기존 pagePostsSimulationsAndStartStop의 20대·40대·스포츠·드라마·start/stop은 유지.

가짜 테스트: 주석에만 30대.
가짜 테스트: 테스트가 MockMvc로 POST /simulations 만 치고 화면은 안 봄.

## Green 최소 구현

```text
simulator.html
  30대 % 입력 (기본 0)
  clickRate 입력 (기본 0)
  POST body: ageShares['30대'], clickRate
새 컨트롤러 없음
```

## 검증 명령

```text
./gradlew test --tests SimulatorPageTest --tests SimulationApiTest
./gradlew test
```

## 하지 않는 것

```text
새 API, 시드 변경
Kafka / k6 / SSE / Redis
FR-14 전체, Experiment RPS
Phase 7 닫기
```

## HITL

구현 전에 개발자 승인. 승인 문장 예: `승인` / `T7-09 승인`

```text
하는 것  폼 30대 % · clickRate → 기존 POST
하지 않는 것 새 API, k6, Phase 닫기
```

STOP. 승인 전에 simulator.html을 고치지 않는다.
