# Analysis

```text
Task: T4-02
Phase: 4
Date: 2026-09-09
```

## 요청

T4-01 Human Gate에서 후보 A를 골랐다. 이벤트 API는 접수만 하고 INSERT는 같은 JVM 워커가 한다. Kafka는 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-11 분리. 재처리·대량은 Phase 완료. T4-02는 요청 경로 분리만
DESIGN        4절: 동기 INSERT. 12.4: Kafka는 목표 그림
ROADMAP       기술은 측정이 말할 때만. B는 이 측정으로 안 고름
TASKS         T4-01 DONE. Kafka/Consumer는 Gate 전 금지 → 이제 A만 허가
ADR           001: Task 하나. 005: JVM 큐 (작성)
T4-01         human-gate A
```

문서 충돌:

```text
없음. 12.4 Kafka는 현재 구조가 아니다.
워커도 같은 DataSource라 풀 결합은 남을 수 있다. 그건 A의 한계로 ADR에 적는다.
```

현재 코드:

```text
EventController → AdEventService.record → save 후 HTTP 201
ServingEventCouplingTest: record 안에서 sleep 하면 GET이 기다림
```

## 제약

```text
해도 되는 것
  BlockingQueue + 단일 persist 워커
  POST는 큐 적재 후 201
  대시보드는 워커 이후 수치 (테스트는 대기)
  T4-01 테스트를 POST가 persist를 기다리지 않게 변경

하면 안 되는 것
  Kafka / Redis / @KafkaListener
  멱등 (FR-12)
  Frequency Cap / Budget 변경
  새 HTTP 경로
```

## 하지 않는 이유

Kafka는 후보 B. 재처리는 유실 한계를 측정한 다음이다.

## Exit

```text
Valid: yes
다음: plan.md
```
