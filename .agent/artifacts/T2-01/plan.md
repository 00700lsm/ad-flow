# Plan

```text
Task: T2-01
Phase: 2
HITL: approved
```

## 완료 조건

```text
동일 사용자는 동일 캠페인을 하루 frequencyCap번까지만 선택한다 (순차 요청)
캡에 걸린 캠페인은 GET /ads 후보에서 빠진다
남은 후보가 있으면 기존 Priority 규칙으로 다른 광고를 고른다
frequencyCap = 0 은 한도 없음 (필드가 0 이상을 허용함)
동시 요청에서 한도를 넘지 않는 것은 이 Task가 아니다
```

## Red Tests

실패해야 하는 테스트 목록.

```text
1. AdSelectionTest.skipsCampaignWhenTodayImpressionsReachCap
   cap=2, 당일 IMPRESSION 2건인 캠페인은 선택되지 않는다
   같은 조건의 다른 캠페인이 선택된다

2. AdSelectionTest.allowsCampaignWhenTodayImpressionsBelowCap
   cap=2, 당일 1건이면 해당 캠페인을 고를 수 있다

3. AdSelectionTest.ignoresCapWhenFrequencyCapIsZero
   frequencyCap=0이면 건수와 관계없이 기존 필터만 적용한다

4. AdSelectionTest.countsOnlySameUserCampaignAndUtcDay
   다른 userId / 다른 날 / CLICK 은 cap 집계에 넣지 않는다

5. AdServingApi 또는 기존 MockMvc 테스트
   같은 userId로 Impression을 cap만큼 넣은 뒤 GET /ads 가 그 캠페인을 반환하지 않는다
   (대체 캠페인이 있으면 그쪽 creative를 반환)
```

가짜 테스트(무조건 pass)는 반려한다.

## Green 최소 구현

통과에 필요한 파일과 책임만 적는다.

```text
AdSelector
  선택 시 캠페인별 당일 노출 횟수를 받아
  frequencyCap > 0 이고 count >= cap 이면 제외
  기존 활성/기간/연령/장르/Priority는 유지

AdServingService
  요청 userId의 당일(UTC) IMPRESSION을 캠페인별로 GET
  AdSelector에 넘긴다
  서빙 시점에 INCR하지 않는다. POST /events/impression 이 카운터다

AdEventRepository
  userId + campaignId + IMPRESSION + 당일 구간 count
  새 API 경로를 만들지 않는다
```

미래 Phase 코드는 적지 않는다.

## 검증 명령

```text
./gradlew test
린트: ./gradlew compileJava compileTestJava
수동 Demo (선택): /console 에서 cap=2 캠페인 2개 → /player 동일 사용자 3회 재생
  1·2회 동일 광고, 3회 다른 광고. 구현 승인 후 Summary에서 필요 시 README에 한 줄
```

## 하지 않는 것

```text
Redis, Lua, Lock, 원자적 INCR
동시 요청 테스트로 cap 보장을 성공 조건으로 고정
Budget / BUDGET_EXHAUSTED
Kafka, Simulator, SSE, 새 엔드포인트
Player/Dashboard 대규모 UI 개편
```

## HITL

구현 전에 개발자 승인. 승인 전에는 프로덕션 코드를 작성하지 않는다.

승인 문장 예: `승인` / `T2-01 승인`
