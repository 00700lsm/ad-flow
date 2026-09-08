# Plan

```text
Task: T3-04
Phase: 3
HITL: approved
```

## 완료 조건

```text
Player에서 예산을 소진하면 그 캠페인은 안 나오고, 다른 후보가 있으면 그 광고가 나온다
Dashboard에서 spentBudget이 늘고 잔여가 줄며, 소진 캠페인 상태가 BUDGET_EXHAUSTED다
전환은 Dashboard(상태·예산)와 Player 세션 이력(캠페인 이름)에서 읽는다
시연 경로는 README에 T3-03 계약(GET /ads가 예산을 소비)으로 적힌다
관찰을 artifacts/T3-04에 남긴다
Redis / 새 HTTP 경로 / 예산 로직 변경은 포함하지 않는다
```

## Red Tests

실패해야 하는 테스트 목록.

```text
1. DashboardPageTest.dashboardHtmlShowsBudgetAndStatus
   GET /dashboard.html
   사용 예산·잔여·상태를 보여줄 영역이 있다
   (예: spentBudget / remainingBudget 또는 동일 의미 라벨, status)
   구현 전 실패. 컬럼만 있고 JSON을 안 그리면 Summary에서 반려

2. AdEventDashboardApiTest.selectAd_thenDashboardShowsSpentAndRemaining
   budget=1 캠페인으로 GET /ads 1회
   GET /dashboard/campaigns/{id}
     spentBudget=1, remainingBudget=0, status=BUDGET_EXHAUSTED
   구현 전 CampaignStats에 필드가 없어 실패
```

가짜 테스트: `/campaigns/{id}`만 assert하고 Dashboard를 안 보는 것. T3-01 중복이며 화면이 아니다.
브라우저 E2E 프레임워크는 이 Task에서 도입하지 않는다. 화면 확인은 아래 수동 Demo다.

## Green 최소 구현

```text
CampaignStats / DashboardService
  budget, spentBudget, remainingBudget(budget − spent), status
  엔드포인트 경로는 그대로

dashboard.html
  사용 예산 / 잔여 예산 / 상태를 표에 그린다
  SSE·초당 지표는 넣지 않는다

index.html
  Phase 3 시연 한 줄: 예산 소진 → Dashboard 상태, Player에서 다른 광고

README.md
  Current Status를 T3-03 + T3-04 확인으로
  Budget 없음 문구 삭제
  Console: 같은 타겟, budget=1 고우선 + 예산 큰 저우선, frequencyCap=0
  Player: 사용자 1 + 콘텐츠 1을 두 번 재생 → 이력이 다른 캠페인
  Dashboard: 고우선 spent=1 잔여=0 BUDGET_EXHAUSTED

artifacts/T3-04/observation.md
  승인 후 브라우저(또는 동일 경로 curl + 화면)로 확인한 결과만
  확인하지 않고 Phase 3 DONE이라고 쓰지 않는다
```

AdServingService / CampaignRepository 차감 SQL은 이 Task에서 수정하지 않는다.
Player 이력 UI는 T2-04를 재사용한다.

## 검증 명령

```text
./gradlew test --tests DashboardPageTest --tests AdEventDashboardApiTest
./gradlew test
린트: ./gradlew compileJava compileTestJava
수동 Demo:
  docker compose + bootRun
  /console.html  스포츠·연령 맞는 캠페인 2개
                 (priority 높음 budget=1 cap=0, 낮음 budget 큼 cap=0)
  /player.html   사용자 A · 축구 하이라이트 → 재생 → 다시 재생
  이력 1번째 ≠ 2번째 캠페인명
  /dashboard.html 고우선 사용 1 / 잔여 0 / BUDGET_EXHAUSTED
```

## 하지 않는 것

```text
Redis, Kafka, Lua, Lock, 새 엔드포인트
예산을 Impression으로 되돌리기
Frequency Cap 로직 변경
Simulator / SSE / k6 / Playwright
샘플 Campaign 자동 삽입
RPS / Lag / p99 위젯
```

## HITL

구현 전에 개발자 승인. 승인 전에는 프로덕션 코드와 테스트 변경을 작성하지 않는다.

승인 문장 예: `승인` / `T3-04 승인`

Trade-off: GET만 하고 재생을 눌러도 예산은 줄어든다 (ADR 004). Player는 성공한 GET마다 Impression을 보내므로 시연 경로에서는 집계와 예산이 같이 움직인다.
Dashboard JSON 필드를 늘리는 것은 새 경로가 아니라 DESIGN 6.3을 기존 summary에 붙이는 것이다.
