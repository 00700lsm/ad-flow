# Analysis

```text
Task: T5-03
Phase: 5
Date: 2026-09-10
```

## 요청

T5-02 후보 C. 같은 `eventId` Impression이 여러 번 와도 Dashboard 집계는 한 번만 오르게 한다. T5-01 Human Gate의 B(PostgreSQL UNIQUE)다. ADR 008을 이 Task에서 대체한다.

## 근거 문서

```text
REQUIREMENTS  FR-12 같은 eventId는 집계가 한 번만 증가
DESIGN        12.5: unique 없는 INSERT. 목표 그림의 Kafka 멱등은 현재 구조 아님
ROADMAP       입력 3 / 유효 1 / 집계 증가량 1
TASKS         T5-02 C: UNIQUE로 진행. 구현은 다음 Task
ADR           008: 데모 감수. 다음에 볼 때 B 또는 C
              T5-02: 008을 다음 Task Plan HITL 후에 번복
측정 T5-01    posted=3 uniqueEventIds=1 aggregated=3
```

문서 충돌:

```text
없음. Kafka Idempotent Consumer는 12.5 목표다. 브로커가 없다.
UNIQUE는 T5-01 후보 B이자 T5-02 C다. 앱 exists 스킵(후보 C)은 고르지 않는다.
```

현재 코드:

```text
accept()     중복 검사 없이 큐
drain()      events.save 매번. 예외면 InterruptedException만 잡음
ad_events    eventId unique 없음
Dashboard    COUNT(행)
DuplicateEventIdAggregationTest  impressions == 3
```

## 제약

```text
해도 되는 것
  eventId UNIQUE
  워커가 중복 INSERT 충돌을 삼키고 다음 건을 처리
  같은 eventId 3 POST 후 impressions == 1 테스트
  T5-01 테스트 기대를 3 → 1 로 맞춤 (UNIQUE 후 3은 실패)
  ADR 009로 008 대체

하면 안 되는 것
  Kafka / Redis / Outbox
  앱 exists 후 스킵만 (T5-01 후보 C)
  Frequency Cap / Budget 변경
  201을 INSERT 완료로 되돌리기
  새 HTTP API

영향 파일 후보
  AdEvent @Column(unique=true) eventId
  AdEventService drain 충돌 시 continue
  EventIdIdempotentAggregationTest 또는 DuplicateEventIdAggregationTest 기대 변경
  ADR 009
```

## 하지 않는 이유

Kafka는 측정·인프라가 없다. exists만 하면 동시 큐에서 레이스가 남는다. UNIQUE가 T5-02 C가 가리킨 B다.

워커가 DataIntegrityViolation을 안 잡으면 두 번째 save에서 drain이 죽는다. UNIQUE만 넣고 끝내지 않는다.

## Exit

```text
Valid: yes
다음: plan.md
```
