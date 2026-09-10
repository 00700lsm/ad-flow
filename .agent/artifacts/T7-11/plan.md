# Plan

```text
Task: T7-11
Phase: 7
HITL: approved (A)
```

## 완료 조건

```text
FR-14 중 충족(HTTP Simulator·시연)과 한계(연속 부하·k6·Kafka)를 문서로 고정한다
Phase 7를 닫을지 Human Gate에서 고른다
k6 / Kafka / 연속 루프 구현은 포함하지 않는다
```

## Red Tests

```text
없음. 재현·해법 코드 Task가 아니다.
가짜 테스트로 Phase를 닫지 않는다.
```

## Green 최소 구현

후보를 고른 뒤에만 문서를 맞춘다. 프로덕션 Java 변경 없음.

```text
A  Phase 7 DONE
   데모 = POST /simulations · 폼 · start 한 번 · Dashboard (T7-10)
   FR-14 칸: 데모. 전문(연속 부하·k6·Kafka Event)은 미충족
   TASKS / ROADMAP / DESIGN Phase 7 DONE
   ADR 015 데모 범위
   다음: Experiment는 개발자가 요청할 때

B  Phase 7 IN PROGRESS
   다음: start 연속 루프. 이 Task에서 구현하지 않음

C  Phase 7 IN PROGRESS
   다음: k6. ROADMAP Experiment를 Phase 7로 당김. 이 Task에서 구현하지 않음
```

## 검증 명령

```text
문서만. 코드 변경이 없으면 ./gradlew test 재실행 의무 없음
```

## 하지 않는 것

```text
k6, Kafka, Redis, SSE, 연속 루프
Experiment Task
FR-14 문장 삭제
```

## HITL

문서 확정 전에 개발자 승인. 승인 문장 예: `승인 A` / `T7-11 A`

```text
A  Phase 7 닫기. 데모 HTTP Simulator. k6·Kafka·루프는 한계
B  Phase 7 유지. 다음은 start 연속 루프
C  Phase 7 유지. 다음은 k6
```

추천: A. ROADMAP은 RPS를 Phase 이후 Experiment로 둔다. T7-10이 화면 경로다. FR-14 전문은 충족이 아니다.

STOP. 후보 선택 전에 코드·k6·Experiment를 쓰지 않는다.
