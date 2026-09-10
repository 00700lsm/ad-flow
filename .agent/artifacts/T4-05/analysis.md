# Analysis

```text
Task: T4-05
Phase: 4
Date: 2026-09-10
```

## 요청

T4-04 다음. FR-11의 남은 절 — Consumer(워커) 장애 뒤에 접수한 이벤트를 다시 처리할 수 있는가 — 를 **재현**한다. Kafka / Outbox는 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-11 분리 + 대량 + Consumer 장애 후 재처리
              분리는 T4-02. 재처리는 ADR 006으로 데모 미충족
DESIGN        4절·12.4: 메모리 큐+워커. Kafka 없음
              목표 그림의 Kafka는 현재 구조가 아님
ROADMAP       Phase 4 질문: 서빙이 이벤트 적재에 묶이는가
              기술은 결과가 필요성을 말할 때만. Kafka 선제 도입 금지
TASKS         T4-01~T4-04 DONE
              하지 않는 것: Kafka / Redis / Consumer 재처리
              Phase 완료 칸: FR-11 재처리 / Kafka (데모 A, ADR 006) 미체크
ADR           005: Kafka 없음. 프로세스 종료 시 큐 유실
              006: 데모에서 유실 감수. 재처리는 아직 충족하지 않음
              다음에 볼 때: 유실이 제품 문제면 B 또는 C를 다시 Human Gate
측정 T4-03    accepted=1 persistedImmediately=0 persistedAfterWait=1
              워커가 살아 있으면 지연 후 1. 장애 후 재처리가 아님
측정 T4-04    workerHoldMs=400 getAdsWaitMs=431. 풀 분리 Human Gate는 후보 대기
```

문서 충돌:

```text
구현 차단 충돌 없음. T4-05에서 Kafka를 넣으면 ADR 006·ROADMAP·TASKS 금지를 건너뛴다.
TASKS Phase 완료의 “Kafka” 칸은 이 Task의 구현 허가가 아니다.
T4-04 풀 분리 Human Gate는 별건. 한 요청에 섞지 않는다.
Phase 5(FR-12)는 Phase 4를 닫기 전이다.
```

현재 코드:

```text
accept()        pending.put 후 201. 디스크 없음
drain()         take → persistDelayMs sleep → save
take 이후       큐에서 빠짐. interrupt 되면 save 없이 종료
stopWorker()    shutdownNow. 힙 큐를 디스크에 안 씀
startWorker()   빈 drain. take로 이미 뺀 이벤트는 다시 안 옴
재처리 정본     없음 (토픽·Outbox·오프셋 없음)
```

T4-03과의 차이:

```text
T4-03  워커가 살아 있는 동안의 집계 공백 (이후 1)
T4-05  take 이후 워커가 죽으면 재시작해도 집계 0 (재처리 없음)
```

## 제약

```text
해도 되는 것
  persist delay 중 워커 interrupt 후 재시작해도 Dashboard 노출이 0인 것을 테스트로 재현
  accepted=1 replayed=0 을 measurement.txt에 남김
  Human Gate 전에 Kafka / Outbox / 별도 Consumer를 넣지 않음

하면 안 되는 것
  Kafka / Redis / Outbox / 재처리 Consumer / 토픽
  별도 DataSource (T4-04 해법)
  Frequency Cap / Budget 변경
  FR-12 멱등
  201 계약 변경
  유실을 없애는 해법
  Phase 4 완료 조건을 REQUIREMENTS에서 바꾸기

영향 파일 후보
  테스트: EventWorkerCrashNoReplayTest (같은 패키지에서 stopWorker / startWorker)
  (승인 후 Summary) TASKS T4-05, DESIGN 12.4에 재처리 불가 재현만
```

## 하지 않는 이유

Kafka는 FR-11 후보이자 DESIGN 12.4 목표다. T4-03은 지연만 증명했다. 장애 후 재처리 불가는 아직 테스트가 없다.
지금 브로커를 넣으면 재현이 아니라 해법이다. 유실을 막으면 재현이 사라진다.

## Exit

```text
Valid: yes
다음: plan.md
```
