# Plan

```text
Task: T2-03
Phase: 2
HITL: approved
```

## 완료 조건

```text
동일 userId + campaignId의 동시 GET /ads가 남기는 당일 캡 카운터는 frequencyCap을 넘지 않는다
캡에 걸린 캠페인은 다음 후보가 있으면 그 광고를 고른다
카운터는 GET /ads에서 원자적으로 오른다 (PostgreSQL)
Impression INSERT는 캡 카운터가 아니다
frequencyCap = 0 은 카운터를 올리지 않는다
Redis / Lua / 후보 B / 후보 C / 새 HTTP API는 포함하지 않는다
Player 화면 확인은 이 Task가 아니다
```

## Red Tests

실패해야 하는 테스트 목록.

```text
1. FrequencyCapRaceTest.concurrentSelectDoesNotExceedCap
   기존 concurrentSelectThenImpressionExceedsCap 을 대체한다
   cap=1, 캠페인 1개, 같은 userId/contentId, 16 스레드 동시 GET /ads
   200인 선택 횟수 ≤ 1
   당일 캡 카운터 ≤ 1
   Impression을 캡 초과 증거로 쓰지 않는다
   @Transactional 클래스 테스트 금지

2. FrequencyCapApiTest.afterCapReached_selectsOtherCampaign
   cap=2 고우선 + 저우선. GET을 cap번 하면 다음 GET은 저우선
   POST /events/impression 으로 캡을 채우지 않는다

3. FrequencyCapApiTest 또는 AdSelection/API
   frequencyCap=0 이면 연속 GET이 같은 캠페인을 고를 수 있다 (카운터 없음)

4. FrequencyCapApiTest.countsOnlySameUserCampaignAndUtcDay
   다른 userId 카운터 / 다른 날 카운터는 현재 user·당일에 합치지 않는다
   CLICK·IMPRESSION 이벤트는 이 집계에 넣지 않는다
```

가짜 테스트(무조건 pass, 스레드를 직렬화만 하고 한도를 assert하지 않음)는 반려한다.

현재 Race 테스트는 초과를 기대하므로, 기대를 바꾸기 전엔 새 테스트가 실패해야 한다.

## Green 최소 구현

통과에 필요한 파일과 책임만 적는다.

```text
FrequencyCapCount (신규, PostgreSQL)
  PK 또는 UNIQUE (userId, campaignId, utcDay)
  count

리포지토리
  count < cap 일 때만 +1, 성공 여부 반환
  (INSERT 후 조건부 UPDATE. 애플리케이션 synchronized / SELECT FOR UPDATE 전 구간 락은 C이므로 쓰지 않음)
  frequencyCap=0 경로는 호출하지 않음

AdServingService
  @Transactional(readOnly = true) 제거 또는 쓰기 트랜잭션
  활성/기간/연령/장르 통과 후보를 Priority 순으로
  cap>0 이면 tryIncrement, 성공한 캠페인을 반환
  실패면 다음 후보
  AdEvent COUNT로 캡을 읽지 않음

AdSelector
  순수 필터는 유지 가능. 원자적 소비는 서비스/리포지토리 책임
```

미래 Phase 코드는 적지 않는다.

## 검증 명령

```text
./gradlew test --tests FrequencyCapRaceTest --tests FrequencyCapApiTest --tests AdSelectionTest
./gradlew test
린트: ./gradlew compileJava compileTestJava
수동 Demo: 이 Task 범위 아님 (Player는 다음 완료 항목)
```

## 하지 않는 것

```text
Redis, Lua, Distributed Lock, GET+Impression 구간 직렬화
Impression 유니크 한도 (후보 B)
Budget / Kafka / Simulator / SSE / 새 엔드포인트
Player/Dashboard UI
캡 카운터와 Impression 건수를 같게 맞추는 보상 트랜잭션
```

## HITL

구현 전에 개발자 승인. 승인 전에는 프로덕션 코드와 테스트 변경을 작성하지 않는다.

승인 문장 예: `승인` / `T2-03 승인`

Trade-off (구현 전 고지): GET만 하고 Player가 Impression을 안 보내도 슬롯은 줄어든다. ADR 003 남은 한계와 같다.
