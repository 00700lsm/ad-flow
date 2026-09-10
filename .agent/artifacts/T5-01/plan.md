# Plan

```text
Task: T5-01
Phase: 5
HITL: approved
```

## 완료 조건

```text
같은 eventId로 POST /events/impression 이 세 번 오면 Dashboard 노출이 3일 수 있다
입력 3 / 유효 키 1 / 집계 +3 을 테스트로 재현하고 남긴다
unique / upsert / Kafka 멱등은 포함하지 않는다
Human Gate 전에 한 번만 집계되도록 고치지 않는다
```

## Red Tests

```text
1. DuplicateEventIdAggregationTest.sameEventIdPostedThriceIncreasesImpressionsThrice
   persist-delay 없이 (또는 워커 저장을 기다린 뒤)
   POST /events/impression 동일 eventId 3회 → 각 201
   GET /dashboard/campaigns/{id} impressions
   멱등을 기대하면 == 1 → 지금 실패 (행이 3이면 3)
```

가짜 테스트: 다른 eventId 3개로 3을 기대. 그건 정상 적재다.
가짜 테스트: 워커 전에 0을 기대 (T4-03). 중복 집계가 아니다.

## Green 최소 구현

```text
DuplicateEventIdAggregationTest 만
  같은 eventId 3 POST
  워커 save 후 impressions == 3
  measurement.txt
    posted=3 uniqueEventIds=1 aggregated=3

프로덕션 AdEvent / Dashboard 변경 없음
UNIQUE 인덱스 없음
```

## 검증 명령

```text
./gradlew test --tests DuplicateEventIdAggregationTest
./gradlew test
```

## 하지 않는 것

```text
Kafka, Redis, UNIQUE, upsert, 멱등 필터, 새 API
Frequency Cap / Budget 변경
FR-11 Outbox
```

## HITL

구현 전에 개발자 승인. 승인 문장 예: `승인` / `T5-01 승인`

Trade-off: 비동기 워커라 폴링으로 INSERT를 기다린다. 운영 delay=0이어도 큐에 세 건이 들어가면 집계는 3이다.

재현 후 후보만 보고 구현하지 않는다.

```text
A  감수. 같은 eventId도 행을 늘림
B  PostgreSQL UNIQUE(eventId) / INSERT 무시
C  앱에서 조회 후 스킵 (레이스 남음)
```

STOP. 승인 전에 프로덕션 코드·멱등을 쓰지 않는다.
