# Plan

```text
Task: T4-03
Phase: 4
HITL: approved
```

## 완료 조건

```text
POST /events/impression 201 직후 Dashboard 노출이 아직 0일 수 있다
그 창(accepted=1, persisted=0)을 테스트로 재현하고 남긴다
Kafka / Outbox / 재처리 Consumer는 포함하지 않는다
Human Gate 전에 유실을 없애지 않는다
```

## Red Tests

```text
1. EventQueueLossTest.dashboardDoesNotSeeEventImmediatelyAfterAccept
   persist-delay-ms=400
   POST /events/impression → 201
   그 직후 GET /dashboard/campaigns/{id} 의 impressions == 0
   (워커가 save 하기 전)
   구현 전 기대가 “즉시 1”이면 실패. 현재 코드는 이미 0일 수 있음.

   Red를 실패로 고정하려면:
   즉시 impressions == 1 을 assert → 지금 실패 (워커 지연)
```

가짜 테스트: 워커가 끝난 뒤 0이라고 assert. 그건 유실이 아니라 미저장.

## Green 최소 구현

```text
EventQueueLossTest 만
  delay=400, POST 직후 impressions=0
  이후 폴링하면 1 (워커가 살아 있으면 유실이 아니라 지연)
  measurement.txt
    accepted=1 persistedImmediately=0 persistedAfterWait=1

프로덕션 AdEventService 변경 없음
유실을 막는 저장소 없음
```

지연 창이 “장애 시 유실될 내용”이다. 프로세스 kill은 이 테스트에서 강제하지 않는다 (같은 JVM).

## 검증 명령

```text
./gradlew test --tests EventQueueLossTest
./gradlew test
```

## 하지 않는 것

```text
Kafka, Redis, Outbox, 파일 큐, 멱등, 새 API
```

## HITL

구현 전에 개발자 승인. 승인 문장 예: `승인` / `T4-03 승인`

Trade-off: delay 프로퍼티로 창을 벌린다. 운영 delay=0 이면 창이 짧다. 그래도 큐에만 있는 동안은 디스크에 없다.

재현 후 후보만 보고 구현하지 않는다.

```text
A  유실 감수. JVM 큐 유지 (ADR 005 한계 수용)
B  DB Outbox / 같은 PostgreSQL에 먼저 쓰기
C  Kafka 등 브로커 (T4-01 후보 B)
```
