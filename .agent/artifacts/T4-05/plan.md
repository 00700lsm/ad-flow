# Plan

```text
Task: T4-05
Phase: 4
HITL: approved
```

## 완료 조건

```text
워커가 take 이후 INSERT 전에 죽으면, 재시작해도 그 Impression은 집계에 안 남는다
재처리 불가를 테스트로 재현하고 시간을 남긴다
Kafka / Outbox / 별도 Consumer는 포함하지 않는다
Human Gate 전에 재처리를 넣지 않는다
```

## Red Tests

```text
1. com.adflow.event.EventWorkerCrashNoReplayTest
   .impressionDoesNotReplayAfterWorkerInterruptDuringPersistDelay
   persist-delay-ms=400
   POST /events/impression → 201
   delay 중 AdEventService.stopWorker() (take 이후, save 전 interrupt)
   startWorker() 후 폴링
   구현 전(재처리를 기대하면) impressions == 1 → 지금 실패 (큐에서 이미 빠짐)
```

가짜 테스트: delay만 두고 워커를 안 죽인 채 0을 기대 (그건 T4-03).
가짜 테스트: context 전체를 닫아 H2까지 날린 뒤 0 (재처리 공백이 아니라 DB 소멸).

## Green 최소 구현

```text
EventWorkerCrashNoReplayTest 만
  같은 패키지에서 stopWorker / startWorker 호출 (프로덕션 훅 추가 없음)
  POST 201 후 sleep으로 drain이 take+delay에 들어가게 함
  interrupt → 재시작 → 대기 후에도 impressions == 0
  measurement.txt
    accepted=1 afterCrashPersisted=0 replayed=0 persistDelayMs=400

프로덕션 AdEventService 변경 없음
Kafka / Outbox 없음
```

## 검증 명령

```text
./gradlew test --tests EventWorkerCrashNoReplayTest
./gradlew test
```

## 하지 않는 것

```text
Kafka, Redis, Outbox, 파일 큐, Consumer 그룹, 멱등, 새 API
T4-04 풀 분리
Phase 4 완료 체크를 승인 없이 닫기
```

## HITL

구현 전에 개발자 승인. 승인 문장 예: `승인` / `T4-05 승인`

Trade-off: delay 중 interrupt로 “take 이후 유실”만 본다. 프로세스 kill·별도 JVM은 이 테스트가 아니다. 운영 delay=0 이면 창이 짧다. take와 save 사이가 없어도, 큐가 힙에만 있으면 프로세스 종료 시 같은 한계다 (ADR 006).

재현 후 후보만 보고 구현하지 않는다.

```text
A  감수. ADR 006 유지. Phase 4를 재처리 미충족으로 남기거나 닫기
B  같은 PostgreSQL Outbox / 선 INSERT
C  Kafka 등 브로커 (ADR 006에서 데모 미선택. 이 재현만으로 자동 도입 아님)
```

T4-04 풀 공유 Human Gate는 이 Task가 아니다. 후보는 그대로 대기.

STOP. 승인 전에 프로덕션 코드·Kafka를 쓰지 않는다.
