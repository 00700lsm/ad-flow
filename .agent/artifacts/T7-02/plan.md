# Plan

```text
Task: T7-02
Phase: 7
HITL: approved
```

## 완료 조건

```text
FR-14 중 재현(T7-01 Simulator 없음)과 다음 방향(후보 C)을 문서로 고정한다
Phase 7를 닫지 않는다
POST /simulations 구현 / k6 / Kafka는 포함하지 않는다
```

## Red Tests

```text
없음. 재현·해법 코드 Task가 아니다.
가짜 테스트(항상 pass)를 만들어 Simulator가 됐다고 쓰지 않는다.
```

## Green 최소 구현

개발자가 T7-01에서 C를 골랐다. 승인 후 문서만 맞춘다. 프로덕션 Java 변경 없음.

```text
ADR 013
  제품 HTTP: POST /simulations, /start, /stop
  가상 사용자 수·연령·장르 분포는 다음 구현 Task
  A(감수) / B(스크립트) / D(k6) 아님
  Kafka 이벤트 증가·SSE·RPS 단계 아님

TASKS / ROADMAP / DESIGN
  Phase 7 IN PROGRESS
  다음은 DESIGN 9.5 구현 Task (개발자 요청 시)
  FR-14 칸 미체크
```

## 검증 명령

```text
문서만. 코드 변경이 없으면 ./gradlew test 재실행 의무 없음
```

## 하지 않는 것

```text
POST /simulations, start/stop, 가상 사용자 루프
simulator.html
k6 / Redis / Kafka / SSE
FR-14 전체, Experiment RPS
Frequency Cap / Budget / UNIQUE 변경
```

## HITL

문서 확정 전에 개발자 승인. 승인 문장 예: `승인` / `T7-02 승인`

T7-01 후보는 이미 C다. 이 Task는 그 선택을 문서에 고정한다.

```text
C  Phase 7 유지. 다음은 POST /simulations + start/stop
```

STOP. 승인 전에 ADR·Simulator 코드·k6를 쓰지 않는다.
