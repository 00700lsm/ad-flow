# Plan

```text
Task: T5-06
Phase: 5
HITL: approved (A)
```

## 완료 조건

```text
FR-12(입력 3 / 유효 1 / 집계 +1)가 T5-03 테스트와 T5-05 시연으로 충족임을 문서로 고정한다
Phase 5를 닫을지 Human Gate에서 고른다
SSE / Kafka / UNIQUE 재구현은 포함하지 않는다
```

## Red Tests

```text
없음. 재현·해법 코드 Task가 아니다.
가짜 테스트로 Phase를 닫지 않는다.
```

## Green 최소 구현

후보를 고른 뒤에만 문서를 맞춘다. 프로덕션 Java 변경 없음.

```text
A  Phase 5 DONE
   FR-12 = T5-03 (테스트) + T5-05 (Dashboard 시연)
   TASKS / ROADMAP / DESIGN Phase 5 DONE
   다음: Phase 6는 개발자가 요청할 때
   ADR: 데모 범위 고정이 필요하면 010

B  Phase 5 IN PROGRESS
   다음: Click도 같은 eventId 3회 시연. 이 Task에서 구현하지 않음

C  Phase 5 IN PROGRESS
   다음: 동시 중복 전용 테스트. 이 Task에서 구현하지 않음
```

## 검증 명령

```text
문서만. 코드 변경이 없으면 ./gradlew test 재실행 의무 없음
```

## 하지 않는 것

```text
SSE, Kafka, Redis, Simulator, 새 API, UNIQUE 변경
Phase 6 Task
```

## HITL

구현·문서 확정 전에 개발자 승인. 승인 문장 예: `승인 A` / `T5-06 A`

```text
A  Phase 5 닫기. FR-12는 T5-03 + T5-05
B  Phase 5 유지. 다음은 Click 중복 시연
C  Phase 5 유지. 다음은 동시 중복 테스트
```

추천: A. ROADMAP 완료 식은 T5-03으로 맞고 T5-05가 화면 경로다. SSE는 Phase 6이다. Click은 같은 UNIQUE 테이블이라 별도 시연이 FR-12를 바꾸지 않는다.

STOP. 후보 선택 전에 코드·Phase 6을 쓰지 않는다.
