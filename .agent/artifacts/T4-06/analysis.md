# Analysis

```text
Task: T4-06
Phase: 4
Date: 2026-09-10
```

## 요청

T4-05 다음. Phase 4에서 FR-11 중 무엇이 충족이고 무엇이 한계인지 **문서로 고정**한다. Kafka / 풀 분리는 이 Task에서 구현하지 않는다.

## 근거 문서

```text
REQUIREMENTS  FR-11 분리 + 대량 + Consumer 장애 후 재처리
DESIGN        4절·12.4: JVM 큐+워커. Kafka 없음
              GET /ads 와 워커는 같은 DataSource
ROADMAP       Phase 4 질문: 적재가 느리면 선택도 느린가. 기술은 측정 후
TASKS         T4-01~T4-05 DONE
              하지 않는 것: Kafka / Redis / Consumer 재처리
              Phase 완료 칸: FR-11 재처리 / Kafka 미체크
ADR           005: 접수를 JVM 큐로 Serving 요청과 저장을 나눔
              006: 데모에서 유실·재처리 미충족 감수 (T4-03·T4-05 Human Gate A)
측정 T4-02    persistDelayMs=400 postMs=2. POST는 INSERT를 안 기다림
측정 T4-04    workerHoldMs=400 getAdsWaitMs=431 pool=1. 풀 분리 Human Gate 후보 대기
측정 T4-05    replayed=0. 재처리 없음
```

문서 충돌:

```text
구현 차단 충돌 없음. T4-06에서 Kafka·별도 DataSource를 넣으면 ADR 006·TASKS 금지를 건너뛴다.
FR-11 전체를 이 Task에서 충족한다고 쓰면 ADR 006과 충돌한다.
T4-04 Human Gate는 아직 미선택. Phase를 닫으면 풀 공유도 한계로 남는다.
Phase 5(FR-12)는 Phase 4를 닫은 뒤에만 연다.
```

현재 코드 / 측정으로 본 FR-11:

```text
분리 (요청 스레드)  T4-02: POST 201은 큐 접수. INSERT는 워커
분리 (풀)          T4-04: 워커 save가 풀을 붙잡으면 GET /ads 대기. 미해결
대량               k6/RPS 측정 없음. TASKS가 k6 금지
재처리             T4-05 + ADR 006 A. 미충족
```

## 제약

```text
해도 되는 것
  충족/미충족을 TASKS·DESIGN·ROADMAP·ADR에 고정
  Phase 4를 닫을지 Human Gate에서 고름
  T4-04 풀 공유 후보를 이 Gate에서 같이 고름 (미선택이면 Phase를 닫을 수 없음)

하면 안 되는 것
  Kafka / Redis / Outbox / 별도 DataSource 구현
  FR-12 멱등
  REQUIREMENTS FR-11 문장 자체를 승인 없이 지우기
  Phase 5 Task를 이 요청에서 시작

영향 파일 후보
  (승인·후보 후 Summary) TASKS 포인터·Phase 4 칸, DESIGN 4절·12.4, ROADMAP 현재 위치
  ADR: Phase 완료 범위를 바꾸면 신규. T4-04만 A면 풀 한계를 ADR에 남김
```

## 하지 않는 이유

Kafka는 DESIGN 12.4 목표이지 현재 구조가 아니다. T4-05에서 이미 A를 골랐다.
풀을 나누는 것은 T4-04 후보 B다. 측정은 풀=1 테스트이지 운영 RPS가 아니다.

## Exit

```text
Valid: yes
다음: plan.md
```
