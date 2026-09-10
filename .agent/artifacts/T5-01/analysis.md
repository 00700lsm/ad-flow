# Analysis

```text
Task: T5-01
Phase: 5
Date: 2026-09-10
```

## 요청

Phase 5를 연다. 첫 작업은 같은 `eventId` Impression이 여러 번 들어오면 Dashboard 집계가 여러 번 오르는지를 **재현**하는 것이다. unique / 멱등 해법은 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-12 같은 eventId는 집계가 한 번만 증가
              Phase 완료 조건이지 T5-01이 아니다
DESIGN        4절: 워커가 ad_events INSERT. eventId unique 없음
              12.5: Kafka 재처리+멱등은 Phase 5 목표. 현재 구조 아님
ROADMAP       Phase 5 질문: 같은 eventId 세 번이면 집계가 세 번인가
              완료: 입력 3 / 유효 1 / 집계 증가량 1 — Phase 완료
TASKS         Phase 4 DONE (ADR 007). 다음: Phase 5는 개발자가 요청할 때 → 이번 요청
              FR-12 전체를 한 Task로 구현하지 않는다
ADR           001: Phase 진행 ≠ FR-12 전체
              006·007: Kafka 없음. 재처리 해법 없음. 멱등과 별개
```

문서 충돌:

```text
구현 차단 충돌 없음.
DESIGN 12.5는 Kafka Idempotent Consumer다. 4절과 ROADMAP은 측정 전 도입을 금지한다.
T5-01은 12.5를 구현하지 않는다. 중복 POST만으로 집계가 여러 번 오르는지만 본다.
```

현재 코드:

```text
accept()           eventId로 AdEvent.record 후 큐 put. 중복 검사 없음
워커               events.save 매번 INSERT
ad_events          id IDENTITY. eventId 컬럼 unique 없음
Dashboard          countByCampaignIdAndType — 행 수
같은 eventId 3 POST → 행 3 → impressions 3 이 될 수 있다
201은 접수. 집계는 워커 save 이후 (T4-03)
```

## 제약

```text
해도 되는 것
  같은 eventId Impression 3회 POST 후 Dashboard impressions==3 을 테스트로 재현
  posted=3 uniqueEventIds=1 aggregated=3 를 measurement.txt에 남김
  Human Gate 전에 unique / upsert / 무시 로직을 넣지 않음

하면 안 되는 것
  Kafka / Redis / Outbox
  eventId UNIQUE, 멱등 Consumer, 중복 드롭
  Frequency Cap / Budget 계약 변경
  SSE / Simulator / k6
  FR-11 재처리 해법 (ADR 006)

영향 파일 후보
  테스트: DuplicateEventIdAggregationTest
  (승인 후 Summary) TASKS T5-01, DESIGN 12.5에 중복 집계 재현만
```

## 하지 않는 이유

unique를 지금 넣으면 재현이 사라진다. T3-02가 한도 준수로 고치지 않은 것과 같다.
Kafka가 없어도 클라이언트가 같은 eventId를 세 번내면 같은 문제다. 12.5 목표와 별개로 먼저 본다.

## Exit

```text
Valid: yes
다음: plan.md
```
