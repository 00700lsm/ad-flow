# Plan

```text
Task: T5-03
Phase: 5
HITL: pending
```

## 완료 조건

```text
같은 eventId로 POST /events/impression 3회면 Dashboard 노출은 1이다
입력 3 / 유효 1 / 집계 증가량 1
차감은 PostgreSQL UNIQUE(eventId). 충돌 행은 집계에 안 넣는다
Kafka / Redis / 앱 exists-only 스킵은 포함하지 않는다
HTTP 201은 접수(큐)로 둔다 (ADR 005·006)
```

이 승인 = T5-01 후보 B, T5-02 C. ADR 008을 009로 대체한다.

## Red Tests

```text
1. DuplicateEventIdAggregationTest 를 멱등 기대로 바꾸거나
   EventIdIdempotentAggregationTest.sameEventIdPostedThriceAggregatesOnce
   동일 eventId 3 POST → 201
   워커 저장 후 impressions == 1
   지금 실패 (aggregated=3)
```

가짜 테스트: 한 번만 POST하고 1을 기대.
가짜 테스트: 워커 전에 0.

T5-01 테스트가 impressions==3 이면 Green 후 스위트 실패. Green에서 그 assert를 1로 맞춘다.

## Green 최소 구현

```text
AdEvent
  eventId unique, not null

AdEventService.drain
  save 시 DataIntegrityViolationException / 제약 위반이면 그 건만 건너뜀
  워커 루프는 계속

테스트
  3 POST 후 impressions == 1
  measurement.txt
    posted=3 uniqueEventIds=1 aggregated=1

ADR 009
  UNIQUE(eventId). 008 대체
```

Kafka·exists 선행 조회 없음.

## 검증 명령

```text
./gradlew test --tests DuplicateEventIdAggregationTest
./gradlew test
```

## 하지 않는 것

```text
Kafka, Redis, Outbox, 새 API, 201 계약 변경
Frequency Cap / Budget
동시 중복 전용 부하 테스트 (UNIQUE가 동시도 막음. 별도 Task 아님)
```

## HITL

구현 전에 개발자 승인. 승인 문장 예: `승인` / `T5-03 승인`

Trade-off: 세 번 201이어도 집계는 1. 클라이언트는 201을 디스크 기록으로 읽으면 안 된다 (ADR 006). 워커 로그에 충돌이 남을 수 있다. Impression과 Click이 같은 eventId면 한 행만 산다.

STOP. 승인 전에 UNIQUE·워커 예외 처리를 쓰지 않는다.
