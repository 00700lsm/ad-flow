# Analysis

```text
Task: T6-01
Phase: 6
Date: 2026-09-10
```

## 요청

Phase 6를 연다. 첫 작업은 운영자가 Dashboard에서 Impression/sec 같은 초당 지표와 실시간 푸시를 볼 수 있는지를 **재현**하는 것이다. SSE / 초당 지표 해법은 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-13 Impression/sec, Click/sec, CTR, Budget, (있다면) Lag / p99 / Error Rate
              Phase 완료 조건이지 T6-01이 아니다
              FR-07: 누적 노출·클릭·CTR. SSE는 Phase 1 밖
DESIGN        4절: Dashboard 집계. SSE / WebSocket 없음
              12.6: Kafka Consumer + SSE/WS는 Phase 6 목표. 현재 구조 아님
ROADMAP       Phase 6 질문: 내부 이벤트를 화면에서 실시간으로 볼 수 있는가
              완료: FR-13 — Phase 완료
TASKS         Phase 5 DONE (ADR 010). 다음: Phase 6는 개발자가 요청할 때 → 이번 요청
              Phase 6 Task는 미리 넣지 않았다. FR-13 전체를 한 Task로 구현하지 않는다
ADR           001: Phase 진행 ≠ FR-13 전체
              005·006: Kafka 없음. Lag는 브로커가 있을 때의 선택 지표
```

문서 충돌:

```text
구현 차단 충돌 없음.
DESIGN 12.6은 Kafka + SSE다. 4절과 ROADMAP은 측정 전 도입을 금지한다.
T6-01은 12.6을 구현하지 않는다. 지금 API·화면에 초당 지표·푸시가 없는지만 본다.
```

현재 코드:

```text
GET /dashboard/campaigns/{id}  CampaignStats
  campaignId name status budget spent remaining impressions clicks ctr
impressionsPerSec / clicksPerSec / kafkaLag / p99 없음
dashboard.html  setInterval(load, 3000). EventSource 없음
Kafka / Prometheus 없음 → Lag · p99 계산 대상 없음
```

## 제약

```text
해도 되는 것
  GET JSON에 초당 지표·Lag 필드가 없음을 테스트로 재현
  dashboard.html이 3초 폴링이고 SSE가 없음을 테스트로 재현
  Human Gate 전에 SSE / 초당 필드 / 새 HTTP를 넣지 않음

하면 안 되는 것
  SSE / WebSocket / Kafka / Redis
  Prometheus / Grafana / k6 / Simulator
  impressionsPerSec 구현, 새 Dashboard API
  Frequency Cap / Budget / UNIQUE 계약 변경
  FR-13 전체를 이 Task에서 충족한다고 쓰기

영향 파일 후보
  테스트: DashboardRealtimeGapTest (가칭)
  (승인 후 Summary) TASKS T6-01, DESIGN 12.6에 공백 재현만
```

## 하지 않는 이유

SSE를 지금 넣으면 공백이 사라진다. T5-01이 UNIQUE를 넣지 않은 것과 같다.
Kafka가 없어도 폴링 누적만 있으면 FR-13 초당 지표는 화면에 없다. 12.6 목표와 별개로 먼저 본다.

## Exit

```text
Valid: yes
다음: plan.md
```
