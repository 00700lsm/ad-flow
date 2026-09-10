# Plan

```text
Task: T6-02
Phase: 6
HITL: approved (A)
```

## 완료 조건

```text
FR-13 중 재현(T6-01 폴링 공백)과 한계(ADR 011)를 문서로 고정한다
Phase 6를 닫을지 Human Gate에서 고른다
SSE / 초당 지표 / Kafka는 포함하지 않는다
```

## Red Tests

```text
없음. 재현·해법 코드 Task가 아니다.
가짜 테스트로 Phase를 닫지 않는다.
```

## Green 최소 구현

후보를 고른 뒤에만 문서를 맞춘다. 프로덕션 Java 변경 없음.

```text
A  Phase 6 DONE
   3초 폴링 누적만 데모. 초당·SSE는 한계 (ADR 011)
   TASKS / ROADMAP / DESIGN Phase 6 DONE
   FR-13 칸: 데모 미충족 (ADR 011)
   ADR: Phase 6 데모 범위 (필요 시 012)
   다음: Phase 7는 개발자가 요청할 때

B  Phase 6 IN PROGRESS
   다음: 기존 GET에 impressionsPerSec. ADR 011 번복. 이 Task에서 구현하지 않음

C  Phase 6 IN PROGRESS
   다음: SSE / WebSocket. ADR 011 번복. 이 Task에서 구현하지 않음
```

## 검증 명령

```text
문서만. 코드 변경이 없으면 ./gradlew test 재실행 의무 없음
```

## 하지 않는 것

```text
SSE, WebSocket, impressionsPerSec, Kafka, Redis, Prometheus
Phase 7 Task
Simulator / k6
```

## HITL

구현·문서 확정 전에 개발자 승인. 승인 문장 예: `승인 A` / `T6-02 A`

```text
A  Phase 6 닫기. FR-13는 ADR 011
B  Phase 6 유지. 다음은 GET 초당 지표 — ADR 011을 다시 연다
C  Phase 6 유지. 다음은 SSE — ADR 011을 다시 연다
```

추천: A. T6-01에서 이미 데모 폴링을 감수했다. FR-13 전문은 충족이 아니다. Simulator는 Phase 7이다.

STOP. 후보 선택 전에 코드·SSE·Phase 7을 쓰지 않는다.
