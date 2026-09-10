# Plan

```text
Task: T4-06
Phase: 4
HITL: approved
```

## 완료 조건

```text
FR-11 중 충족(T4-02 접수 분리)과 한계(재처리 ADR 006, 풀 공유 T4-04)를 문서로 고정한다
Phase 4를 닫을지 Human Gate에서 고른다
Kafka / Outbox / 별도 DataSource 구현은 포함하지 않는다
```

## Red Tests

```text
없음. 재현·해법 코드 Task가 아니다.
가짜 테스트(항상 pass)를 만들어 Phase를 닫지 않는다.
```

## Green 최소 구현

후보를 고른 뒤에만 문서를 맞춘다. 프로덕션 Java 변경 없음.

```text
A  Phase 4 DONE
   접수 분리만 데모 충족. 재처리·풀 분리는 한계
   T4-04 Human Gate도 A (같은 DataSource 유지)
   TASKS / ROADMAP / DESIGN 현재 Phase 4 DONE
   FR-11 칸: 데모 범위에서 접수 분리. 재처리 미충족 (ADR 006)
   ADR: T4-04 풀 공유 감수 + Phase 4 데모 범위 (필요 시 007)

B  Phase 4 IN PROGRESS
   다음 Task는 풀 분리(T4-04 후보 B). 이 Task에서 구현하지 않음

C  Phase 4 IN PROGRESS
   재처리(Outbox 또는 Kafka). ADR 006 번복. 이 Task에서 구현하지 않음
   새 대량·내구성 측정 없이 C를 코드로 넣지 않음
```

## 검증 명령

```text
문서만. ./gradlew test 는 코드 변경이 없으면 재실행 의무 없음
```

## 하지 않는 것

```text
Kafka, Redis, Outbox, 별도 DataSource, 멱등, 새 API
Phase 5 Task
k6 / Simulator
```

## HITL

구현·문서 확정 전에 개발자 승인. 승인 문장 예: `승인 A` / `T4-06 A`

```text
A  Phase 4 닫기. T4-04 풀도 감수. FR-11 재처리는 ADR 006
B  Phase 4 유지. 다음은 풀 분리
C  Phase 4 유지. 다음은 재처리(Outbox/Kafka) — ADR 006을 다시 연다
```

추천: A. 데모에서 유실·재처리를 두 번 A로 골랐다. 풀=1 대기만으로 DataSource를 나누지 않는다. FR-11 전문은 충족이 아니다.

T4-04 Human Gate(후보 대기)는 A를 고르면 같이 닫힌다. B/C는 Phase를 열어 둔다.

STOP. 후보 선택 전에 코드·Kafka·Phase 5를 쓰지 않는다.
