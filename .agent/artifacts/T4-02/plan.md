# Plan

```text
Task: T4-02
Phase: 4
HITL: approved
```

Human Gate 선택 A. 개발자: `A로 고고`.

## 완료 조건

```text
POST /events/* 는 INSERT가 끝나기 전에 201을 줄 수 있다
저장은 같은 JVM 워커가 한다
Kafka 없음
프로세스 유실·풀 결합 잔여는 ADR 005 한계로 남긴다
```

## Red Tests

```text
1. ServingEventCouplingTest.eventApiReturnsBeforePersistFinishes
   persist가 400ms 걸릴 때 POST /events/impression 경과 < 150ms
   지금 record()가 save를 기다리므로 실패
```

가짜 테스트: 빈 큐 인터페이스만 있고 HTTP는 여전히 save 후 201.

## Green 최소 구현

```text
AdEventService.accept  큐에 넣고 AdEvent 반환 (save 없음)
워커 스레드           take 후 repository.save
EventController        accept
Dashboard 테스트       INSERT 반영까지 짧게 폴링
adflow.event.persist-delay-ms  기본 0. 결합 테스트에서만 워커 지연
```

## 검증 명령

```text
./gradlew test --tests ServingEventCouplingTest --tests AdEventDashboardApiTest
./gradlew test
```

## 하지 않는 것

```text
Kafka, Redis, 멱등, 새 API, Cap/Budget 변경
```

## HITL

`A로 고고` = 후보 A Plan 승인.
