# Plan

```text
Task: T5-02
Phase: 5
HITL: approved
```

## 완료 조건

```text
FR-12 중 재현(T5-01 중복 집계)과 한계(ADR 008)를 문서로 고정한다
Phase 5를 닫을지 Human Gate에서 고른다
UNIQUE / upsert / Kafka 멱등은 포함하지 않는다
```

## Red Tests

```text
없음. 재현·해법 코드 Task가 아니다.
가짜 테스트(항상 pass)를 만들어 Phase를 닫지 않는다.
```

## Green 최소 구현

후보를 고른 뒤에만 문서를 맞춘다. 프로덕션 Java 변경 없음.

```text
A  Phase 5 DONE
   중복 집계 재현만. 멱등은 한계 (ADR 008)
   TASKS / ROADMAP / DESIGN Phase 5 DONE
   FR-12 칸: 데모 미충족 (ADR 008)
   ADR: Phase 5 데모 범위 (필요 시 009)

B  Phase 5 IN PROGRESS
   다음 Task는 동시 중복 재현. 이 Task에서 구현하지 않음

C  Phase 5 IN PROGRESS
   UNIQUE / 멱등. ADR 008 번복. 이 Task에서 구현하지 않음
```

## 검증 명령

```text
문서만. 코드 변경이 없으면 ./gradlew test 재실행 의무 없음
```

## 하지 않는 것

```text
UNIQUE, upsert, Kafka, Redis, 새 API
Phase 6 Task
SSE / Simulator / k6
```

## HITL

구현·문서 확정 전에 개발자 승인. 승인 문장 예: `승인 A` / `T5-02 A`

```text
A  Phase 5 닫기. FR-12는 ADR 008
B  Phase 5 유지. 다음은 동시 중복 재현
C  Phase 5 유지. 다음은 UNIQUE — ADR 008을 다시 연다
```

추천: A. T5-01 Human Gate에서 이미 중복 집계를 감수했다. FR-12 전문은 충족이 아니다.

STOP. 후보 선택 전에 코드·멱등·Phase 6을 쓰지 않는다.
