# Plan

```text
Task: T3-03
Phase: 3
HITL: approved
```

## 완료 조건

```text
동일 캠페인의 동시 GET /ads가 남기는 spentBudget은 budget을 넘지 않는다
한도에 걸린 캠페인은 다음 후보가 있으면 그 광고를 고른다
차감은 GET /ads에서 원자적으로 오른다 (PostgreSQL, spent < budget 일 때만 +1)
도달 시 BUDGET_EXHAUSTED
Impression INSERT는 예산 카운터가 아니다
budget = 0 이면 고르지 않는다
Redis / Lua / Lock / 후보 B / 후보 C / 새 HTTP API는 포함하지 않는다
Player·Dashboard 화면 확인은 이 Task가 아니다
```

이 승인 = Human Gate에서 GET 시점 PostgreSQL 원자적 차감을 고른다.
T3-02 후보 A(Impression만 조건부 UPDATE) / B(구간 Lock) / C(Redis)는 고르지 않는다.

## Red Tests

실패해야 하는 테스트 목록.

```text
1. BudgetRaceTest.concurrentSelectDoesNotExceedBudget
   기존 concurrentSelectThenImpressionExceedsBudget 을 대체한다
   budget=1, frequencyCap=0, 캠페인 1개, 16 스레드 동시 GET /ads
   200인 선택 횟수 ≤ 1
   GET /campaigns/{id} 의 spentBudget ≤ 1
   Impression을 예산 초과 증거로 쓰지 않는다
   @Transactional 클래스 테스트 금지

2. BudgetApiTest (순차)
   budget=2 고우선 + 저우선. GET을 budget번 하면 다음 GET은 저우선
   POST /events/impression 으로 예산을 채우지 않는다
   마지막 GET 후 고우선 status == BUDGET_EXHAUSTED, spentBudget == budget

3. AdSelectionTest / BudgetApiTest
   budget=0 이면 고르지 않는다 (기존 유지)
   spent < budget 이면 고를 수 있다

4. BudgetApiTest.clickDoesNotIncreaseSpentBudget
   GET 1회 후 spent=1. Click은 spent를 올리지 않는다
   Impression도 spent를 올리지 않는다
```

가짜 테스트: 스레드를 직렬화만 하고 한도를 assert하지 않음.
현재 Race 테스트는 초과를 기대하므로, 기대를 바꾸기 전엔 새 테스트가 실패해야 한다.

## Green 최소 구현

```text
CampaignRepository
  spent_budget < budget 인 행만 spent_budget +1, 도달 시 BUDGET_EXHAUSTED
  영향 행 수로 성공 여부

AdServingService
  후보를 Priority 순으로
  tryCharge가 성공한 캠페인을 반환
  실패면 다음 후보
  Impression 경로의 chargeImpression은 호출하지 않음

AdEventService
  IMPRESSION은 이벤트 INSERT만. 예산을 건드리지 않음

AdSelector
  hasRemainingBudget 필터는 유지 가능. 원자적 소비는 리포지토리 책임
```

미래 Phase 코드는 적지 않는다. FrequencyCapCounter는 건드리지 않는다.

## 검증 명령

```text
./gradlew test --tests BudgetRaceTest --tests BudgetApiTest --tests AdSelectionTest --tests CampaignTest
./gradlew test
린트: ./gradlew compileJava compileTestJava
수동 Demo: 이 Task 범위 아님
```

## 하지 않는 것

```text
Redis, Lua, DECR, Distributed Lock, GET+Impression 구간 직렬화
Impression 조건부 차감만으로 한도를 고정 (T3-02 후보 A)
Frequency Cap 로직 변경
Kafka / Simulator / SSE / 새 엔드포인트
Dashboard 사용·잔여 UI / Player
```

## HITL

구현 전에 개발자 승인. 승인 전에는 프로덕션 코드와 테스트 변경을 작성하지 않는다.
승인 직후 ADR을 남긴다 (Frequency Cap ADR 003과 같은 자리).

승인 문장 예: `승인` / `T3-03 승인`

Trade-off: GET만 하고 Impression이 없어도 예산은 줄어든다. ADR 003과 같다.
