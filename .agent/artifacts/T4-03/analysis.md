# Analysis

```text
Task: T4-03
Phase: 4
Date: 2026-09-09
```

## 요청

T4-02 다음. JVM 큐에만 있고 INSERT 전인 이벤트가 집계에 없는 창을 **재현**한다. Kafka·내구성 큐는 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-11 분리 + Consumer 장애 후 재처리. 재처리는 Phase 완료
DESIGN        4절·12.4: 메모리 큐+워커. 프로세스 유실이 한계
ROADMAP       기술은 결과가 필요성을 말할 때만
TASKS         T4-02 DONE. Kafka / Consumer 재처리는 아직 하지 않음
ADR           005 남은 한계: 프로세스 종료 시 큐 유실
측정 T4-02    persistDelayMs=400 postMs=2
```

문서 충돌:

```text
없음. FR-11 재처리를 T4-03에서 Kafka로 채우면 ADR 005와 ROADMAP을 건너뛴다.
T4-03은 유실 창이 있다는 것만 고정한다.
```

현재 코드:

```text
accept() 는 pending.put 후 201
워커가 take → (optional delay) → save
delay 중·큐에만 있으면 Dashboard COUNT는 0
shutdownNow 하면 큐 유실
```

## 제약

```text
해도 되는 것
  POST 직후 Dashboard impressions=0 을 테스트로 재현 (워커 지연)
  accepted=1 persisted=0 을 measurement.txt에 남김
  Human Gate 전에 Kafka / 파일 큐 / Outbox 넣지 않음

하면 안 되는 것
  Kafka / Redis / 재처리 Consumer
  멱등 FR-12
  Frequency Cap / Budget 변경
  유실을 없애는 해법 (파일, DB outbox, 브로커)
```

## 하지 않는 이유

유실을 지금 막으면 재현이 사라진다. T3-02가 한도 준수로 고치지 않은 것과 같다.

## Exit

```text
Valid: yes
다음: plan.md
```
