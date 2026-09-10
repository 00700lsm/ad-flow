# ADR 010 - Phase 5 데모는 eventId UNIQUE까지다

## 문제

FR-12는 같은 eventId의 집계가 한 번만 증가해야 한다. T5-03이 테스트로 3/1/1을 맞췄고 T5-05가 Dashboard·README로 같은 식을 보여 줬다. Phase 5를 닫을지 고른다.

## 현재 Context

```text
관련 Phase      5
관련 측정       T5-03 posted=3 uniqueEventIds=1 aggregated=1
                T5-05 POST 3×201, GET dashboard impressions=1
현재 코드       ad_events.eventId UNIQUE. 워커는 충돌 건너뜀. Kafka 없음
개발자 선택     T5-06 후보 A (2026-09-10)
```

## 검토한 대안

```text
A  Phase 5 닫기. FR-12 = T5-03 + T5-05
B  Phase 5 유지. Click 같은 eventId 시연
C  Phase 5 유지. 동시 중복 전용 테스트
```

## 선택

대안 A.

ROADMAP 완료 식은 T5-03으로 이미 맞다. T5-05는 같은 식을 화면 경로로 고정한다. Click은 같은 UNIQUE 테이블이라 별도 시연이 FR-12를 바꾸지 않는다. SSE는 Phase 6(FR-13)이다.

측정·관찰 없이 “멱등이 됐다”고 쓰지 않는다. 이 Task에서 코드 변경 없음.

## 결과

```text
하는 것      Phase 5를 닫음. FR-12는 T5-03 테스트 + T5-05 시연
하지 않는 것 SSE, Kafka, UNIQUE 재구현, FR-12 문장 삭제
남은 한계    HTTP 201은 접수 (ADR 006). Click 화면 시연은 안 함
             Kafka 멱등 Consumer 없음. Player는 매번 새 eventId
다음에 볼 때 Phase 6(FR-13)는 개발자가 요청할 때
```
