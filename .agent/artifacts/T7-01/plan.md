# Plan

```text
Task: T7-01
Phase: 7
HITL: approved
```

## 완료 조건

```text
POST /simulations 가 없다
시뮬레이터 화면이 없다 (연령·장르 분포 입력 없음)
공백을 테스트로 재현하고 남긴다
Simulator 구현 / k6 / Kafka는 포함하지 않는다
Human Gate 전에 가상 트래픽을 넣지 않는다
```

## Red Tests

실패해야 하는 테스트 목록.

```text
1. SimulatorGapTest.postSimulationsExists
   POST /simulations → 2xx/201 을 기대하면 지금 실패 (매핑 없음 / 404)

2. 같은 클래스
   GET /simulator.html 200 과 동시 사용자·연령·장르 UI를 기대하면 지금 실패
   실제 고정은 404 + index.html에 Simulator 링크 없음
```

가짜 테스트: GET /ads 가 있다고 Simulator다. FR-05다.
가짜 테스트: 테스트에서 동시 GET을 돌렸다고 가상 사용자 분포다. T2-03/T3-03이다.
가짜 테스트: k6 스크립트가 있다고 제품 Simulator다. Experiment다.

## Green 최소 구현

```text
SimulatorGapTest 만
  POST /simulations → 404
  GET /simulator.html → 404
  index.html 에 Simulator / /simulations 없음
  measurement.txt
    simulationsEndpoint=0 simulatorHtml=0 k6=0

프로덕션 API / HTML 변경 없음
가상 사용자 루프 없음
```

## 검증 명령

```text
./gradlew test --tests SimulatorGapTest
./gradlew test
```

## 하지 않는 것

```text
POST /simulations 구현, start/stop
가상 사용자 수·연령·장르로 GET /ads 루프
k6 / Prometheus / Grafana / Kafka / Redis
SSE / impressionsPerSec
Frequency Cap / Budget / UNIQUE 변경
FR-14 전체, Experiment RPS
```

## HITL

구현 전에 개발자 승인. 승인 문장 예: `승인` / `T7-01 승인`

재현 후 후보만 보고 이 Task에서 해법을 넣지 않는다.

```text
A  감수. Player 수동만 (FR-14 미충족은 다음 Task에서 고정)
B  최소: 기존 GET /ads를 N번 치는 로컬 스크립트 (분포 UI 없음)
C  DESIGN 9.5 POST /simulations + start/stop, 가상 사용자 수·분포
D  k6 시나리오 (ROADMAP은 RPS를 Phase 7 이후 Experiment로 둠)
```

추천: 승인 후 테스트로 공백만 남긴다. B/C/D는 측정·재현 다음 Human Gate다.
D는 이 Phase 본선이 아니다. Kafka 이벤트 증가는 브로커가 없어 C에도 넣지 않는다.

STOP. 승인 전에 프로덕션 코드·Simulator·k6를 쓰지 않는다.
