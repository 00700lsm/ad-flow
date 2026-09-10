# Analysis

```text
Task: T4-04
Phase: 4
Date: 2026-09-10
```

## 요청

T4-03 다음. ADR 005·006에 남은 한계 — 워커 INSERT가 DataSource 커넥션을 붙잡으면 GET `/ads`가 같이 기다리는지 — 를 **재현**한다. Kafka·풀 분리는 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-11 분리 + 대량 + 재처리. 재처리는 ADR 006으로 데모 미충족
DESIGN        4절·12.4: 워커와 GET은 같은 DataSource. Kafka 없음
ROADMAP       Phase 4 질문: 이벤트 적재가 느리면 선택 API도 느린가
              기술은 결과가 필요성을 말할 때만
TASKS         T4-01~T4-03 DONE. Kafka / Consumer 재처리는 Phase 4 금지
              FR-11 재처리 칸: 데모 A, ADR 006
ADR           005·006 남은 한계: 워커 INSERT와 GET /ads 같은 DataSource
측정 T4-01    eventHoldMs=400 getAdsWaitMs=409 pool=1 (요청 스레드 INSERT)
측정 T4-02    persistDelayMs=400 postMs=2 (POST만 끊김)
```

문서 충돌:

```text
없음. FR-11 Kafka를 지금 넣으면 ADR 006·ROADMAP을 건너뛴다.
T4-01은 POST 스레드가 커넥션을 잡을 때다. T4-02 이후 POST는 안 잡는다.
워커 save가 잡을 때는 아직 측정이 없다.
Phase 5(FR-12)는 Phase 4를 닫기 전이다.
```

현재 코드:

```text
accept()           pending.put 후 반환. 커넥션 없음
drain()            take → persistDelayMs sleep → events.save
persistDelayMs     save 전 Thread.sleep. 이 구간은 커넥션을 안 씀
GET /ads           같은 DataSource
풀이 1이고 save가 커넥션을 붙잡으면 GET은 기다릴 수 있다
```

## 제약

```text
해도 되는 것
  워커 persist가 커넥션을 붙잡은 동안 GET /ads 대기를 테스트로 재현
  테스트에서만 풀 크기 1, 커넥션 점유 지연
  대기 시간을 artifacts/T4-04/measurement.txt 에 남김
  Human Gate 전에 풀 분리 / Kafka / Outbox 넣지 않음

하면 안 되는 것
  Kafka / Redis / Outbox / 재처리 Consumer
  별도 DataSource, 풀 증설을 해법으로 넣기
  Frequency Cap / Budget 변경
  FR-12 멱등
  201 계약 변경
  persistDelayMs를 커넥션 점유로 바꿔 다른 테스트 의미를 깨기

영향 파일 후보
  테스트: ServingEventWorkerPoolCouplingTest (풀=1, 워커 save 중 hold)
  (승인 후 Summary) TASKS T4-04, DESIGN 12.4에 풀 공유 재현만
```

## 하지 않는 이유

Kafka는 ADR 006이 데모에서 고르지 않았다. 풀을 나누거나 INSERT를 빠르게 하면 재현이 사라진다.
save 전 sleep만으로는 풀 결합이 안 보인다. 점유는 save/트랜잭션 안에서만 재현한다.

## Exit

```text
Valid: yes
다음: plan.md
```
