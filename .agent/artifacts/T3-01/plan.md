# Plan

```text
Task: T3-01
Phase: 3
HITL: pending
```

## 완료 조건

```text
동일 캠페인의 spentBudget이 budget에 도달하면 GET /ads가 그 캠페인을 고르지 않는다 (순차)
다른 후보가 있으면 그 광고를 고른다
POST /events/impression 1건당 spentBudget +1 (1원)
spentBudget >= budget 이 되면 상태는 BUDGET_EXHAUSTED
budget = 0 인 캠페인은 잔여가 없으므로 고르지 않는다
동시 요청에서 Overspending이 없는 것은 이 Task가 아니다
```

## Red Tests

실패해야 하는 테스트 목록.

```text
1. AdSelectionTest.skipsCampaignWhenSpentBudgetReachesBudget
   budget=2, spentBudget=2 인 캠페인은 선택되지 않는다
   같은 조건의 다른 캠페인이 선택된다

2. AdSelectionTest.allowsCampaignWhenSpentBudgetBelowBudget
   budget=2, spentBudget=1 이면 해당 캠페인을 고를 수 있다

3. AdSelectionTest.skipsCampaignWhenBudgetIsZero
   budget=0, spentBudget=0 이면 고르지 않는다

4. AdEvent 또는 MockMvc
   Impression을 budget만큼 넣은 뒤
   spentBudget == budget 이고 status == BUDGET_EXHAUSTED
   이후 GET /ads 는 그 캠페인을 반환하지 않는다
   (대체 캠페인이 있으면 그쪽 creative)

5. AdEvent
   CLICK은 spentBudget를 올리지 않는다
```

가짜 테스트: 동시 요청에서 한도를 지킨다고 assert하는 것. 그건 이후 Task다.

## Green 최소 구현

```text
AdSelector
  spentBudget >= budget 이면 제외
  기존 활성/기간/연령/장르/Frequency Cap/Priority는 유지

Campaign
  Impression 기록 시 spentBudget +1
  도달하면 BUDGET_EXHAUSTED
  서빙 시점에 DECR하지 않는다

AdEventService
  type == IMPRESSION 일 때만 Campaign에 위임
  새 HTTP API 없음
```

미래 Phase 코드는 적지 않는다. GET /ads의 Frequency Cap INCR은 건드리지 않는다.

## 검증 명령

```text
./gradlew test
린트: ./gradlew compileJava compileTestJava
수동 Demo는 이 Task 필수가 아니다. Dashboard/Player 확인은 이후 Task
```

## 하지 않는 것

```text
Redis, Lua, Lock, 원자적 DECR
동시 요청 테스트를 성공 조건으로 고정
Frequency Cap 로직 변경
Kafka, Simulator, SSE, 새 엔드포인트
Dashboard 사용·잔여 예산 UI
Player 개편
```

## HITL

구현 전에 개발자 승인. 승인 전에는 프로덕션 코드와 테스트 변경을 작성하지 않는다.

승인 문장 예: `승인` / `T3-01 승인`

Trade-off: Impression이 예산을 채운다. GET만 하고 Impression이 없으면 예산은 안 줄어든다. Frequency Cap(ADR 003, GET 시점 소비)과 카운터 시점이 다르다. 동시 GET+Impression Overspending은 다음 재현 Task에서 본다.
