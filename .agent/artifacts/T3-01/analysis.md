# Analysis

```text
Task: T3-01
Phase: 3
Date: 2026-09-07
```

## 요청

Phase 3를 연다. 첫 작업은 잔여 예산이 없으면 그 캠페인을 고르지 않는 순차 경로다. 동시 요청 Overspending은 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-10 잔여 예산 없으면 미노출. 소진 시 BUDGET_EXHAUSTED.
              동시성 Overspending 0은 Phase 완료 조건이지 T3-01이 아니다
DESIGN        4절: Budget 차감 / BUDGET_EXHAUSTED 는 아직 코드에 없음
              spentBudget 필드는 있고 항상 0
              8절 선택 규칙 3번: 예산이 남아 있는가
              12.3: 초기 PostgreSQL. Overspending 재현은 그다음
ROADMAP       Phase 3: PostgreSQL로 먼저. Lock / Redis는 측정 후 Human Gate
TASKS         Phase 2 DONE. 다음: Phase 3는 개발자가 요청할 때 → 이번 요청
ADR           001: Phase 진행 ≠ Phase 전체. FR-10을 한 Task로 구현하지 않는다
              003: Frequency Cap은 GET 시점 원자적 INCR. Budget 해법이 아니다
```

문서 충돌:

```text
없음. ROADMAP의 "남은 노출 100 vs 동시 1,000"은 Phase 3 질문이다.
T3-01은 순차에서 spent >= budget 이면 제외하는 것만 다룬다.
Redis / Lock / Lua는 DESIGN 12.3 비교 후보이고 현재 구조가 아니다.
```

현재 코드(문서 4절과 일치):

```text
budget / spentBudget 필드는 Campaign에 있다. spentBudget는 생성 시 0, 이후 안 오른다
BUDGET_EXHAUSTED enum은 있다. 자동 전이는 없다. isActiveAt는 ACTIVE만 본다
GET /ads는 활성/기간/연령/장르/Priority/선택 시점 Frequency Cap만 본다
Impression은 AdEvent INSERT만 한다. 예산을 건드리지 않는다
Dashboard는 노출/클릭/CTR만. 사용·잔여 예산 없음
```

## 제약

```text
이 Phase에서 해도 되는 것
  순차 GET /ads에서 spentBudget >= budget 이면 그 캠페인 제외
  POST /events/impression 이 spentBudget를 1 올린다 (1 Impression = 1원)
  올린 뒤 spent >= budget 이면 상태를 BUDGET_EXHAUSTED 로 바꾼다
  다른 후보가 있으면 기존 Priority 규칙으로 그 광고를 고른다
  순차 테스트로 고정

하면 안 되는 것
  Redis / Kafka / Lua / Distributed Lock / Optimistic Lock을 한도 보장으로 넣기
  동시 요청 Overspending 0을 이 Task 완료 조건으로 고정
  Frequency Cap 계약 변경 (GET 시점 INCR)
  Simulator / SSE / k6 / Prometheus / Grafana
  Mock Ad Exchange / Kubernetes / AWS / MongoDB
  운영 AI
  DESIGN에 없는 API
  Dashboard 실시간 스트림 / Player 대규모 개편

영향 파일 후보
  Campaign, AdSelector, AdEventService
  AdSelectionTest, Impression/서빙 API 테스트
  (승인 후 Summary) TASKS / DESIGN 4절·선택 규칙 3번
```

## 하지 않는 이유

Race를 코드 단계에서 추측으로 막지 않는다. ROADMAP이 PostgreSQL 단순 차감 뒤에 Overspending을 재현하라고 한다.
GET 시점에 예산을 원자적으로 깎으면 T3-02에서 초과를 재현하기 어렵다. T2-01이 Impression을 캡 카운터로 둔 것과 같다.
Dashboard 사용·잔여 예산 표시와 Player에서 소진 후 광고 변경은 Phase 3 완료 조건이다. T3-01은 순차 선택 규칙만 고정한다.

## Exit

```text
Valid: yes
다음: plan.md
```
