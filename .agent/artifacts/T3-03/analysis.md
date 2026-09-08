# Analysis

```text
Task: T3-03
Phase: 3
Date: 2026-09-08
```

## 요청

T3-02 다음. 동시 GET /ads에서 예산 Overspending을 한도로 고정한다. Redis / Lock은 측정 비교 전에 고르지 않는다.

## 근거 문서

```text
REQUIREMENTS  FR-10 잔여 없으면 미노출. 동시 Overspending 없어야 함
DESIGN        4절: Impression이 spent +1. 동시 한도 없음
              12.3: PostgreSQL 재현 후 Lock/Redis는 Human Gate
ROADMAP       Phase 3: 재현 뒤 후보. 완료는 FR-10 + Dashboard/Player
TASKS         T3-02 DONE. 후보 B/C(Lock/Redis)는 이 Phase에서 선점 금지
              다음: 개발자 요청 → 이번 요청
ADR           001: Task 하나. FR-10 한 번에 구현 금지
              003: Frequency Cap은 GET 원자적 INCR. Budget 저장소 선례는 PostgreSQL
```

문서 충돌:

```text
없음. T3-02 Human Gate 후보 A(Impression 조건부 UPDATE)만 쓰면
GET 16건은 그대로 200이 된다. FR-10의 「노출되지 않는다」와 어긋난다.
노출 한도는 GET에서 잔여를 소비해야 한다. Redis는 DESIGN 12.3 후보지 현재 구조가 아니다.
Dashboard/Player 소진 확인은 Phase 완료 항목이다. T3-03 완료 조건이 아니다.
```

현재 코드(문서 4절과 일치):

```text
GET /ads 는 spent를 읽기만 한다
POST /events/impression 이 spentBudget +1
BudgetRaceTest: requests=16 selected=16 impressions=16 budget=1 spent=16 overflow=15
Frequency Cap은 이미 GET 시점 PostgreSQL 원자적 INCR (ADR 003)
```

추천 해법 (승인 = 이 선택):

```text
GET /ads가 spent < budget 일 때만 spentBudget을 원자적으로 +1
실패하면 다음 우선순위 캠페인
도달 시 BUDGET_EXHAUSTED
Impression은 Dashboard 집계. 예산 카운터가 아님
저장소는 기존 Campaign 행 (PostgreSQL). Redis / SELECT FOR UPDATE 전 구간 락 아님
```

T3-01 계약 변경: 예산은 Impression이 아니라 GET이 채운다. Frequency Cap과 시점이 같아진다.

## 제약

```text
이 Phase에서 해도 되는 것
  PostgreSQL에서 spent < budget 일 때만 +1
  동시 GET에서 선택 횟수·spent ≤ budget 을 테스트로 고정
  T3-01 순차 테스트를 새 계약에 맞게 수정
  BudgetRaceTest 기대를 한도 준수로 변경

하면 안 되는 것
  Redis / Lua / DECR
  GET+Impression 구간 Distributed Lock / synchronized
  T3-02 후보 A만 (Impression 한도, 노출은 그대로)
  Frequency Cap 계약 변경
  Simulator / SSE / k6 / 새 HTTP API
  Dashboard 사용·잔여 UI / Player 개편

영향 파일 후보
  Campaign / CampaignRepository (조건부 UPDATE)
  AdServingService (선택 후 소비, 실패 시 다음)
  AdEventService (Impression에서 chargeImpression 제거)
  BudgetRaceTest, BudgetApiTest, CampaignTest
```

## 하지 않는 이유

Redis는 12.3 비교 후보다. T2-03과 같이 이미 쓰는 PostgreSQL로 한도를 먼저 고정한다.
Impression만 막으면 선택(노출) 초과가 남는다.
Player/Dashboard는 동시 한도가 테스트로 고정된 다음이다.

## Exit

```text
Valid: yes
다음: plan.md
```
