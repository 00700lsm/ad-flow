# Analysis

```text
Task: T3-04
Phase: 3
Date: 2026-09-08
```

## 요청

T3-03까지 API·테스트로 예산 한도를 고정했다. Phase 3 남은 완료 조건은 Dashboard에서 예산이 줄고 캠페인이 `BUDGET_EXHAUSTED`가 되는 것을 화면으로 확인하는 것이다.

## 근거 문서

```text
REQUIREMENTS  FR-10: 잔여 없으면 미노출. 소진 시 BUDGET_EXHAUSTED
              제품 목표: 화면으로 확인할 수 있는 서비스
DESIGN        4절: GET 원자적 차감 (ADR 004). Player·Dashboard 확인은 남음
              6.1: 예산 소진 시 광고 변경 확인
              6.3: Dashboard에 사용 예산 / 잔여 예산
              12.3: 결과는 Dashboard에서 예산 감소와 BUDGET_EXHAUSTED
ROADMAP       Phase 3 완료: FR-10. Dashboard에서 확인
              현재 위치: 다음 = 남은 화면 확인은 개발자가 요청할 때
TASKS         T3-03 DONE. 다음: Phase 3 다음 Task는 개발자가 요청할 때
              이 요청이 그 요청 → T3-04
ADR           001: Task 하나. 004: Player·Dashboard는 T3-03이 아님
```

문서 충돌:

```text
구현 차단 충돌 없음.
README Current Status는 Phase 2 / Budget 없음.
DESIGN 6.3의 Ad Requests/sec · Kafka Lag · SSE는 Phase 6. T3-04가 넣지 않는다.
GET /campaigns 는 이미 spentBudget·status를 준다. Dashboard API·화면은 아직 Impression/CTR만.
```

현재 코드:

```text
GET /ads: spent < budget 이면 원자적 +1, 도달 시 BUDGET_EXHAUSTED
Dashboard JSON: campaignId, name, impressions, clicks, ctr
dashboard.html: 노출 / 클릭 / CTR. 예산·상태 없음
console.html: 목록에 status. spent/잔여는 없음
player.html: 세션 노출 이력 있음 (T2-04). 예산 시연 문구는 없음
```

## 제약

```text
이 Phase에서 해도 되는 것
  기존 GET /dashboard/* 응답에 budget / spentBudget / remaining / status 를 붙인다
  dashboard.html에서 사용·잔여·상태를 읽는다
  Player는 기존 이력으로 소진 후 다른 광고를 읽는다 (새 이력 UI 없음)
  README / index에 GET 슬롯 소비 계약으로 예산 시연 경로를 적는다
  브라우저(또는 동일 경로)로 확인하고 artifacts/T3-04에 관찰을 남긴다

하면 안 되는 것
  Redis / Kafka / Lua / Distributed Lock
  새 HTTP 경로
  예산 차감 로직 재작성, Impression을 다시 카운터로
  Frequency Cap 계약 변경
  Simulator / SSE / k6 / RPS / Lag
  Playwright 등 브라우저 자동화 스택
  샘플 캠페인 시드로 Console 시연을 우회

영향 파일 후보
  CampaignStats / DashboardService
  dashboard.html, index.html
  README.md (Current Status, 예산 시연 경로)
  AdEventDashboardApiTest 또는 DashboardPageTest
  (Summary) TASKS T3-04, DESIGN 4절·12.3 화면 남음 문구
```

## 하지 않는 이유

BudgetRaceTest를 한 번 더 돌리는 것은 Dashboard 확인이 아니다. T3-01·T3-03이 선택을 이미 고정한다.
사용·잔여 표시는 새 서빙 규칙이 아니라 DESIGN 6.3·12.3을 화면에서 읽기 위한 최소 필드다.
SSE·초당 지표는 FR-13이다.

## Exit

```text
Valid: yes
다음: plan.md
```
