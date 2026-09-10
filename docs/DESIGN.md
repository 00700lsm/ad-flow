# AdFlow - DESIGN

## 1. 문서 목적

이 문서는 AdFlow의 **목표 시스템 구조와 구현 기준**을 정의한다.

원문은 [① AdFlow - Mini OTT Ad Platform 프로젝트 설계](https://cmcm.tistory.com/49)다.

```text
README.md        → 프로젝트가 무엇인지, 사람이 어떻게 진입하는가
REQUIREMENTS.md  → 무엇을 만족해야 하는가
DESIGN.md        → 목표 / 현재 시스템은 어떻게 구성되는가
ROADMAP.md       → 어떤 문제를 어떤 순서로 확인할 것인가
TASKS.md         → 지금 Phase에서 무엇을 할 것인가
Poc.md           → 바이브 루프(상태 그래프)의 철학
adr/             → 왜 그 결정을 했는가
experiments/     → 실제 측정 결과
```

이 문서는 Phase마다 새로 만들지 않는다. 프로젝트 전체에서 `DESIGN.md` 하나를 유지한다.

코드가 없는 기능은 현재 구조로 적지 않는다.
7절의 Redis / Kafka 그림은 **목표**다. 현재 코드는 4절을 따른다.

중요한 설계 변경의 이유는 `docs/adr/`, 실험 결과는 `docs/experiments/`에 기록한다.
구현 루프는 `.cursor/rules/ad-flow.mdc`와 `.cursor/skills/ad-flow-vibe-coding/SKILL.md`다.

---

# 2. 프로젝트 목표

대규모 요청 환경에서 광고를 저지연으로 선택·노출하고, 발생한 광고 이벤트를 유실·중복 없이 처리하여 정산 가능한 데이터로 만드는 미니 OTT 광고 플랫폼을 구현한다.

단순한 성능 테스트나 기술 데모가 아니라, 사용자가 직접 조작하고 결과를 확인할 수 있는 실제 서비스 형태를 우선한다.

핵심:

```text
광고 캠페인을 직접 생성할 수 있다
OTT 플레이어에서 실제 광고가 노출된다
사용자 조건에 따라 서로 다른 광고가 선택된다
Frequency Cap과 광고 예산 소진을 실제 화면에서 확인할 수 있다
Impression / Click 이벤트가 실시간으로 수집된다
관리자 대시보드에서 광고 성과를 확인할 수 있다
Traffic Simulator로 가상의 대규모 사용자를 발생시킬 수 있다
이후 부하 테스트와 성능 개선은 실제 서비스의 확장 과정으로 진행한다
```

한 줄 소개:

대규모 OTT 환경을 가정해 광고 캠페인 관리, 저지연 광고 서빙, Frequency Cap, 예산 제어, Kafka 기반 이벤트 처리와 정산 정합성을 구현한 미니 광고 플랫폼.

---

# 3. 설계 원칙

테스트 자체가 결과물이 되지 않도록 다음 순서로 진행한다.

```text
Product
  실제로 사용할 수 있는 광고 플랫폼 구현
  ↓
Problem
  사용자 증가와 이벤트 증가로 문제 발생
  ↓
Experiment
  문제를 재현하고 측정
  ↓
Engineering
  Redis / Kafka / 동시성 제어 등으로 해결
  ↓
Result
  개선된 시스템이 실제 서비스 화면에서도 동작
```

"k6로 3,000 RPS를 테스트했다"가 아니라,
"실제로 동작하는 광고 플랫폼을 만들고, 사용자가 증가했을 때 발생한 문제를 측정하고 개선했다"를 전체 스토리로 가져간다.

첫 번째 목표는 다음 한 문장이다.

Campaign Console에서 광고를 하나 생성하고 OTT Player를 실행했을 때 내가 만든 광고가 실제로 재생되게 만든다.

여기까지 만든 뒤 성능과 분산 시스템 문제를 하나씩 추가한다.

프론트엔드는 시연 가능한 수준에 집중하고, 프로젝트의 중심은 Backend에 둔다.
MongoDB는 명확한 사용 이유가 생기지 않는 한 넣지 않는다.
AI는 핵심 시스템을 완성한 뒤 운영 자동화 영역에서만 선택적으로 적용한다.

---

# 4. 현재 설계 상태

```text
Current Phase

Phase 7 DONE (ADR 015)
가상 사용자로 부하를 재현하는가
코드: POST /simulations · start 한 번 순차 · simulator.html. README 시연
T7-10 관찰: impressions=2 clicks=2 spentBudget=2
데모 종료: HTTP Simulator. 연속 루프·k6·Kafka Event는 없음
```

현재 구조:

```text
Browser (console / player / dashboard-ui / simulator)
  ↓
Spring Boot
  ├─ Campaign / Creative CRUD
  ├─ Ad Selection (활성 / 기간 / 예산 잔여 / 연령 / 장르 / Priority / 선택 시점 Frequency Cap·Budget)
  ├─ Impression / Click 접수 (JVM 큐) → 워커 INSERT  ← Dashboard 집계. 캡·예산 카운터가 아님
  ├─ Simulations (메모리. start는 ageShares·categories로 순차 GET /ads. 20대→1 30대→3 40대→2. Impression, clickRate Click)
  └─ Dashboard 집계
  ↓
PostgreSQL
  ├─ ad_events.eventId UNIQUE
  └─ frequency_cap_counts (userId, campaignId, UTC day)
```

프론트는 Next.js가 아니라 Spring이 서빙하는 정적 HTML이다.
이벤트는 Kafka 없이 같은 앱의 메모리 큐와 워커가 PostgreSQL에 저장한다 (ADR 005). HTTP 201은 INSERT 완료를 기다리지 않는다.
같은 eventId는 UNIQUE라 집계 행은 한 번이다 (ADR 009). 중복 INSERT는 워커가 건너뛴다.
Frequency Cap 카운터는 `GET /ads`가 후보를 고를 때 `count < cap`인 행만 원자적으로 +1 한다.
실패하면 다음 우선순위 캠페인을 시도한다. `frequencyCap = 0`은 올리지 않는다.
Impression INSERT는 캡을 채우지 않는다. GET만 해도 슬롯은 줄어든다 (ADR 003).
예산은 GET /ads가 spent < budget 인 행만 원자적으로 +1 한다. 도달 시 BUDGET_EXHAUSTED (ADR 004).
Impression INSERT는 예산을 채우지 않는다. GET만 해도 spentBudget은 오른다.
T2-02 측정(해법 전): requests=16 selected=16 impressions=16 cap=1 overflow=15.
T3-02 측정(해법 전): requests=16 selected=16 impressions=16 budget=1 spent=16 overflow=15.
T3-03 테스트(해법 후, 동시 GET): requests=16 selected=1 budget=1 spent=1.
T3-04에서 Dashboard에 spentBudget / remainingBudget / status를 붙였다. budget=1 고우선 다음 저우선이 나온다.
T4-01 측정: eventHoldMs=400 getAdsWaitMs=409 pool=1.
T4-02 측정: persistDelayMs=400 postMs=2. 프로세스 유실·워커와 GET의 풀 공유는 감수 (ADR 006·007).
T4-03 측정: accepted=1 persistedImmediately=0 persistedAfterWait=1. 내구성 해법 없음.
T4-04 측정: workerHoldMs=400 getAdsWaitMs=431 postMs=3 pool=1. 풀 분리 없음 (ADR 007 A).
T4-05 측정: accepted=1 afterCrashPersisted=0 replayed=0 persistDelayMs=400. 재처리 없음.
T5-01 측정: posted=3 uniqueEventIds=1 aggregated=3. 멱등 없음 (해법 전).
T5-03 측정: posted=3 uniqueEventIds=1 aggregated=1 (ADR 009).
T5-05: README curl 3회 201, GET dashboard impressions=1. Player는 새 eventId.
T5-06: Phase 5 데모 완료 = UNIQUE 집계. SSE / Kafka 멱등 Consumer는 없음 (ADR 010).
T6-01 측정: pollMs=3000 sse=0 impressionsPerSecField=0. 실시간 해법 없음 (ADR 011 A).
T6-02: Phase 6 데모 완료 = 3초 폴링 누적. FR-13 초당·SSE는 미충족 (ADR 012).
T7-01 측정: simulationsEndpoint=0 simulatorHtml=0 k6=0. Simulator 해법 없음 (해법 전).
T7-02: 다음은 POST /simulations · start · stop (ADR 013 C). k6 / 스크립트 아님.
T7-03 측정: simulationsEndpoint=1 startRequestCount=2 stopBeforeStartRequestCount=0. 화면·분포 없음.
T7-04 측정: dramaOnlySpent=2 sportsWhenDramaOnly=0 splitSports=1 splitDrama=1. 화면·Impression 없음.
T7-05 측정: simulatorHtml=1 indexLink=1 postsStartStop=1. Impression 루프 없음.
T7-06 측정: startImpressions=2 stopBeforeStartImpressions=0. Click 없음.
T7-07 측정: clickRate100Clicks=2 clickRateOmittedClicks=0 stopBeforeStartClicks=0. 30대 시드 없음.
T7-08 측정: thirtiesSpent=2 twentiesWhenThirtiesOnly=0. 폼 30대 없음.
T7-09 측정: thirtiesShare=1 clickRate=1.
T7-10 관찰: impressions=2 clicks=2 spentBudget=2. Phase 닫기 없음.
T7-11: Phase 7 데모 완료 = HTTP Simulator. FR-14 연속 부하·k6·Kafka는 미충족 (ADR 015).

아직 코드에 없는 것:

```text
Redis
Kafka
SSE / WebSocket
Mock Ad Exchange
```

---

# 5. 주요 사용자 시나리오

## 5.1 광고주 / 운영자

광고 관리 콘솔에서 캠페인을 생성한다.

```text
캠페인명: 아이폰 신제품 광고
예산: 50,000원
타겟 연령: 20 ~ 30대
타겟 장르: 스포츠
Frequency Cap: 사용자당 하루 2회
상태: ACTIVE
```

운영자가 확인할 수 있어야 하는 정보:

```text
노출 수
클릭 수
CTR
사용 예산
잔여 예산
캠페인 상태
Frequency Cap 상태
```

## 5.2 OTT 사용자

사용자가 OTT 콘텐츠를 재생하면 광고 요청이 발생한다.

```text
사용자 A
28세
관심 장르: 스포츠
  ↓
축구 콘텐츠 재생
  ↓
Ad Serving API 호출
  ↓
타겟 조건에 맞는 광고 선택
  ↓
광고 재생
  ↓
Impression 이벤트 발생
```

같은 사용자가 광고를 여러 번 보면 Frequency Cap이 적용된다.

```text
1번째 재생 → 아이폰 광고
2번째 재생 → 아이폰 광고
3번째 재생 → 다른 광고
```

예산이 모두 소진되면 해당 광고는 더 이상 노출되지 않는다.

```text
50,000원
  ↓
39,000원
  ↓
21,000원
  ↓
3,000원
  ↓
0원

ACTIVE → BUDGET_EXHAUSTED
```

---

# 6. 주요 화면

프로젝트 결과물은 아래 4개 화면을 중심으로 구성한다.

## 6.1 OTT Player

광고가 실제로 노출되는 사용자 화면이다.

```text
콘텐츠 재생
광고 요청
광고 영상 또는 광고 카드 노출
Impression 발생
광고 클릭 시 Click 이벤트 발생
Frequency Cap 적용 결과 확인
예산 소진 시 광고 변경 확인
```

```text
┌─────────────────────────────────────┐
│             Mini TVING              │
│                                     │
│            [ 콘텐츠 영상 ]            │
│                                     │
│  ─────────────●────────────         │
│                                     │
│           광고가 시작됩니다           │
│                                     │
└─────────────────────────────────────┘
```

## 6.2 Campaign Console

광고 캠페인을 생성하고 관리하는 관리자 화면이다.

```text
캠페인 생성 / 수정 / 중지
광고 소재 등록
예산 설정
노출 기간 설정
타겟 조건 설정
Priority 설정
Frequency Cap 설정
캠페인 상태 조회
```

```text
캠페인: 아이폰 신제품 광고
예산: 50,000원
타겟: 20 ~ 30대
장르: 스포츠
Frequency Cap: 하루 2회
상태: ACTIVE
```

## 6.3 Ad Dashboard

광고 이벤트와 성과를 확인하는 운영 대시보드다.

```text
Impression
Click
CTR
사용 예산
잔여 예산
Ad Requests / sec
Impression / sec
Kafka Consumer Lag
p95 / p99 latency
Error Rate
```

```text
아이폰 캠페인

노출            1,831
클릭               42
CTR              2.29%
오늘 사용 예산    9,155원
잔여 예산        40,845원

████████░░░░░░░░ 18.3%
```

실시간 이벤트 스트림은 SSE 또는 WebSocket으로 표시한다.

```text
LIVE AD EVENTS

19:21:01  IMPRESSION   Campaign 12
19:21:02  IMPRESSION   Campaign 12
19:21:02  CLICK        Campaign 12
19:21:03  IMPRESSION   Campaign 31
19:21:04  IMPRESSION   Campaign 12
```

## 6.4 Traffic Simulator

실제 사용자 수천 명을 대신하여 가상의 OTT 사용자를 발생시킨다.

```text
동시 사용자 수 설정
연령대 분포 설정
콘텐츠 장르 분포 설정
사용자별 광고 요청 생성
Impression / Click 이벤트 생성
캠페인별 광고 분배 결과 확인
```

```text
User Simulator

동시 사용자
[ 1000 ]

사용자 분포
20대 █████████ 40%
30대 ███████   30%
40대 ████      20%
기타 ██        10%

콘텐츠
☑ 스포츠
☑ 드라마
☑ 예능

[ ▶ Simulation ]
```

시뮬레이션 실행 후 실시간 결과 예:

```text
Active Users     1,000
Ad Requests/s      834
Impressions/s      761

현재 가장 많이 노출되는 광고

1. Nike    31%
2. Apple   24%
3. Netflix 17%
```

---

# 7. 시스템 아키텍처

목표 구조는 다음과 같다.

```text
                       ┌─────────────────┐
                       │ Campaign Admin  │
                       └────────┬────────┘
                                │
                                ▼
                         PostgreSQL
                                │
                         Campaign Sync
                                │
                                ▼
                             Redis
                                │
                                │
Client ───────► Ad Serving API ─┼────► Ad Selection
                                │
                                ├────► Frequency Cap
                                │
                                ├────► Budget / Pacing
                                │
                                └────► External Ad Adapter
                                           │
                                           ▼
                                     Mock Ad Exchange

Client
  │
  ├── impression
  └── click
          │
          ▼
      Event API
          │
          ▼
        Kafka
          │
     ┌────┴─────┐
     ▼          ▼
 Dedup Worker  Aggregator
     │          │
     └────┬─────┘
          ▼
      PostgreSQL
          │
          ▼
     Billing Report
```

역할:

```text
Campaign Admin     캠페인·소재를 PostgreSQL에 저장한다
Campaign Sync      서빙에 필요한 캠페인 스냅샷을 Redis로 동기화한다
Ad Serving API     광고 요청을 받아 후보를 고른다
Ad Selection       활성/기간/예산/캡/타겟/장르/Priority로 필터링한다
Frequency Cap      사용자·캠페인 단위 노출 횟수를 제한한다
Budget / Pacing    잔여 예산을 차감하고 소진 시 노출을 멈춘다
External Adapter   내부 재고가 없을 때 Mock Ad Exchange에 위임한다
Event API          Impression / Click을 받아 Kafka로 넘긴다
Dedup Worker       eventId 기준으로 중복 이벤트를 거른다
Aggregator         실시간 집계를 만든다
Billing Report     Source of Truth 이벤트에서 정산 데이터를 만든다
```

---

# 8. 광고 선택 규칙

광고 요청이 들어오면 다음 순서로 후보를 필터링한다.

```text
1. 활성 상태의 캠페인인가?
2. 캠페인 시작 / 종료 기간 안인가?
3. 예산이 남아 있는가?
4. 사용자가 Frequency Cap을 초과하지 않았는가?
5. 사용자 타겟 조건에 부합하는가?
6. 콘텐츠 장르 조건에 부합하는가?
7. 후보 중 Priority가 가장 높은 광고를 선택한다
```

추후 가중치 기반 선택이나 Pacing 알고리즘을 추가할 수 있다.

---

# 9. MVP API

초기에는 아래 API만 구현한다.

## 9.1 Campaign

```text
POST   /campaigns
GET    /campaigns
GET    /campaigns/{id}
PATCH  /campaigns/{id}
POST   /campaigns/{id}/creatives
```

## 9.2 Ad Serving

```text
GET /ads?userId={userId}&contentId={contentId}
```

## 9.3 Event

```text
POST /events/impression
POST /events/click
```

## 9.4 Dashboard

```text
GET /dashboard/campaigns/{campaignId}
GET /dashboard/summary
```

## 9.5 Simulator

```text
POST /simulations
POST /simulations/{id}/start
POST /simulations/{id}/stop

T7-03: concurrentUsers. start는 샘플 User 1 · Content 1 순차 선택. 메모리. 화면 없음.
T7-04: ageShares · categories. 20대→User 1, 40대→User 2. 스포츠→Content 1, 드라마→Content 2. 생략 시 T7-03.
T7-05: GET /simulator.html. 폼이 create·start·stop 호출. Impression 없음.
T7-06: start가 선택마다 Impression. Click 없음.
T7-07: clickRate. 100이면 clicks=2. 생략이면 0. 30대 시드 없음.
T7-08: 30대→User 3(35). thirtiesSpent=2. 폼 없음.
T7-09: simulator.html 30대 % · clickRate. thirtiesShare=1.
T7-10: README simulator → Dashboard. impressions=2 clicks=2.
T7-11: Phase 7 데모 완료. HTTP Simulator. 루프·k6·Kafka 없음 (ADR 015).
```

---

# 10. 핵심 도메인

## 10.1 Campaign

```text
Campaign
- id
- name
- status
- budget
- spentBudget
- startAt
- endAt
- priority
- targetAgeMin
- targetAgeMax
- targetCategory
- frequencyCap
```

`status`는 최소 `ACTIVE`, `PAUSED`, `BUDGET_EXHAUSTED`를 가진다.

## 10.2 Creative

```text
Creative
- id
- campaignId
- type
- mediaUrl
- clickUrl
```

## 10.3 User

```text
User
- id
- age
- preferredCategories
```

## 10.4 Content

```text
Content
- id
- title
- category
```

## 10.5 AdEvent

```text
AdEvent
- eventId
- campaignId
- creativeId
- userId
- contentId
- type
- occurredAt
```

`type`은 `IMPRESSION`, `CLICK`이다. `eventId`는 멱등 처리의 기준이다.

---

# 11. 기술 스택

## 11.1 Backend

```text
Java 21
Spring Boot
Spring Data JPA
```

## 11.2 Data

```text
PostgreSQL
Redis
Kafka
```

## 11.3 Frontend

```text
Next.js
React
```

## 11.4 Observability / Test

```text
k6
Prometheus
Grafana
```

## 11.5 Infra

```text
Docker Compose
```

추후 필요 시:

```text
gRPC
Kubernetes
AWS
```

Phase 1에서는 PostgreSQL과 Spring Boot만으로 MVP를 만든다.
Redis와 Kafka는 병목이나 이벤트 분리 문제가 확인된 뒤에 도입한다.

---

# 12. 개발 단계

## 12.1 Phase 1. 실제 사용 가능한 제품 MVP

목표는 성능이 아니라 실제 서비스 형태 완성이다.

```text
Campaign CRUD
Creative 등록
User / Content 샘플 데이터
광고 선택 로직
OTT Player
Impression / Click 이벤트
기본 Dashboard
```

완료 조건:

Campaign Console에서 광고를 생성하고, OTT Player에서 해당 광고가 실제로 노출되며, Dashboard에서 Impression과 Click을 확인할 수 있다.

## 12.2 Phase 2. Frequency Cap

동일한 사용자는 동일한 광고를 하루 최대 N번까지만 볼 수 있다.

현재 코드:

```text
GET /ads
  후보를 Priority 순으로
  count < cap 이면 PostgreSQL에서 원자적 +1
  실패하면 다음 후보
```

T2-02에서 동시 GET+Impression Race를 재현했다.
T2-03에서 선택 시점 원자적 INCR로 동시 GET 한도를 테스트로 고정했다 (ADR 003). Redis / Lua는 쓰지 않는다.

결과는 OTT Player에서 동일 광고가 더 이상 노출되지 않는 형태로 확인한다. 세션 노출 이력에 캠페인 이름이 쌓인다. T2-04에서 Player 경로(GET /ads 후 Impression)로 고우선 cap=1 다음 저우선이 나오는 것을 확인했다.

## 12.3 Phase 3. Budget Control

캠페인 예산이 소진되면 광고 노출을 중단한다.

현재 코드 (T3-03·T3-04, ADR 004):

```text
GET /ads
  spent_budget < budget 이면 원자적 +1
  도달하면 BUDGET_EXHAUSTED
  실패하면 다음 후보
POST /events/impression
  이벤트 INSERT만. 예산을 올리지 않는다
CLICK은 예산을 올리지 않는다
```

동시 GET 한도는 테스트로 고정했다. Redis / Lock은 쓰지 않는다.
T3-02 해법 전: selected=16 spent=16 overflow=15.
T3-03 해법 후(테스트): selected=1 spent=1.

초기에는 PostgreSQL 기반으로 구현하고 동시 요청 시 Overspending을 재현한다.

비교 후보:

```text
1. DB Pessimistic Lock
2. DB Optimistic Lock
3. Redis DECR
4. Redis Lua Script
```

비교 지표:

```text
처리량
p95
p99
Overspending 여부
실패율
```

결과는 Dashboard에서 예산이 감소하고 캠페인이 `BUDGET_EXHAUSTED`로 바뀌는 모습으로 보여준다. T3-04에서 summary 표에 사용·잔여·상태를 그렸다. SSE·초당 지표는 없다.

## 12.4 Phase 4. Kafka Event Pipeline

Impression / Click 이벤트를 Kafka 기반으로 처리한다. **현재 코드는 JVM 메모리 큐+워커**다. Kafka 없음.

T4-01: 요청 스레드가 INSERT 커넥션을 붙잡으면 GET `/ads`가 기다렸다.
T4-02 (ADR 005): POST는 큐 적재 후 201. persistDelayMs=400일 때 postMs=2.
T4-03: 워커 delay 중 Dashboard 노출은 0. 살아 있으면 이후 1.
데모에서는 그 창의 유실을 감수한다 (ADR 006). Outbox / Kafka 없음.
T4-04: 워커가 INSERT 커넥션을 붙잡으면 GET `/ads`가 기다렸다. workerHoldMs=400 getAdsWaitMs=431 pool=1.
해법은 A. 같은 DataSource 유지 (ADR 007).
T4-05: take 이후 delay 중 워커 interrupt → 재시작해도 Dashboard 노출 0. accepted=1 replayed=0. 재처리 해법은 A(ADR 006 유지). Kafka / Outbox 없음.
T4-06: Phase 4 데모 완료 = 접수 분리. FR-11 재처리·Kafka는 미충족 (ADR 007).

현재 코드 (T4-02):

```text
POST /events/*  → 메모리 큐 → 201
워커            → PostgreSQL INSERT
GET /ads 와 워커는 같은 DataSource (데모에서 감수, ADR 007)
프로세스 종료 시 큐 유실 (데모에서 감수, ADR 006)
take 이후 워커 장애 시 재처리 없음 (T4-05)
```

```text
OTT Player
  ↓
Event API
  ↓
Kafka
  ↓
Consumer
  ↓
Aggregation
```

목표:

```text
API와 이벤트 처리 분리
대량 이벤트 처리
Consumer 장애 대응
재처리 가능 구조
```

## 12.5 Phase 5. 이벤트 중복과 정합성

Kafka 재처리에서 같은 Impression이 여러 번 처리되면 정산 데이터가 틀어진다. **현재 코드는 eventId UNIQUE**다. Kafka 멱등 Consumer 없음.

T5-01 (해법 전): 같은 eventId 3 POST → 노출 3. posted=3 aggregated=3.
T5-03 (ADR 009): 같은 3 POST → 노출 1. posted=3 uniqueEventIds=1 aggregated=1.
T5-05: README curl 3회, Dashboard 노출 1.
T5-06: Phase 5 데모 완료 = UNIQUE 집계. FR-12는 T5-03 + T5-05 (ADR 010). SSE 없음.

현재 코드:

```text
POST /events/*  같은 eventId도 큐에 넣음. 201은 접수
워커            INSERT. UNIQUE 충돌은 건너뜀
Dashboard       COUNT(행) → 키당 최대 1
```

```text
At-least-once Delivery
+
Idempotent Consumer
+
eventId 기반 Deduplication
```

검증:

```text
중복 이벤트 주입
최종 집계 데이터 확인
정산 데이터와 실시간 데이터 비교
```

Kafka 그림은 목표다. 지금 멱등은 UNIQUE다.

## 12.6 Phase 6. 실시간 Dashboard

Kafka Consumer에서 처리되는 광고 이벤트를 SSE 또는 WebSocket으로 전달한다. **현재 코드는 누적 집계 + 3초 폴링**이다. SSE / 초당 지표 없음.

T6-01 (해법 전): GET CampaignStats에 impressionsPerSec · clicksPerSec · kafkaLag 없음. pollMs=3000 sse=0.
해법은 A. 3초 폴링 누적 유지 (ADR 011). SSE / 초당 필드 없음.
T6-02: Phase 6 데모 완료 = 3초 폴링. FR-13 초당·SSE는 미충족 (ADR 012).

```text
Impression / sec
Click / sec
CTR
Ad Request / sec
Campaign Budget
Kafka Lag
p99
Error Rate
```

목표는 시스템 내부에서 처리되는 이벤트를 사용자가 직접 볼 수 있게 만드는 것이다.

## 12.7 Phase 7. Traffic Simulator

T7-01 (해법 전): POST /simulations 없음. simulator.html 없음. k6 없음.
simulationsEndpoint=0 simulatorHtml=0 k6=0.
T7-02: 해법은 C. DESIGN 9.5 HTTP (ADR 013).
T7-03: POST create/start/stop. startRequestCount=2. 분포 UI·Impression 루프 없음.
T7-04: ageShares·categories. dramaOnlySpent=2 splitSports=1. 화면·Impression 없음.
T7-05: simulatorHtml=1. Impression 루프 없음.
T7-06: startImpressions=2. Click 없음.
T7-07: clickRate100Clicks=2. 30대 시드 없음.
T7-08: thirtiesSpent=2. 폼 30대 없음.
T7-09: thirtiesShare=1 clickRate=1.
T7-10: impressions=2 clicks=2 spentBudget=2.
T7-11: Phase 7 데모 완료 = HTTP Simulator. FR-14 연속 부하·k6·Kafka는 미충족 (ADR 015).

가상의 사용자를 생성하여 광고 요청을 발생시킨다.

```text
User 생성
  ↓
Content 선택
  ↓
Ad Request
  ↓
광고 노출
  ↓
Impression
  ↓
일부 확률로 Click
```

시뮬레이터로 직접 확인할 것:

```text
캠페인별 노출 분배
Budget 감소
Frequency Cap
요청량 증가
Kafka Event 증가
Dashboard 변화
```

---

# 13. 성능 개선 실험

성능 실험은 제품이 완성된 이후 진행한다.

## 13.1 Experiment 1. PostgreSQL 기반 광고 서빙

초기 구조:

```text
Ad Request
  ↓
Campaign SELECT
  ↓
Targeting SELECT
  ↓
Budget SELECT
  ↓
Frequency SELECT
  ↓
Response
```

부하 단계:

```text
100 RPS
500 RPS
1,000 RPS
3,000 RPS
```

측정:

```text
RPS
p50
p95
p99
Error Rate
DB Connection
CPU
```

문제가 확인된 이후 Redis를 도입한다.

핵심은 Redis를 사용했다는 사실이 아니라 다음을 증명하는 것이다.

PostgreSQL 기반 광고 후보 검색 구조에서 병목이 발생했고, Redis 도입 후 동일 조건에서 응답 지연과 DB 부하가 얼마나 개선되었는지 측정한다.

## 13.2 Experiment 2. 광고 예산 동시성

```text
남은 광고 노출 가능 횟수 = 100
동시 요청 = 1,000

정상 기대값: 광고 노출 = 100
```

잘못된 구현에서는 100회를 초과하는 Overspending이 발생할 수 있다.

```text
Pessimistic Lock
Optimistic Lock
Redis Atomic
Lua Script
```

## 13.3 Experiment 3. Frequency Cap Race Condition

```text
한 사용자는 하루 최대 3회 노출
```

T2-02에서 동시 요청이 cap을 넘는 것을 재현했다.
T2-03에서 PostgreSQL 선택 시점 원자적 INCR로 한도를 고정했다. Redis는 쓰지 않았다.

## 13.4 Experiment 4. Kafka Event Duplication

동일한 `eventId`를 가진 이벤트를 여러 번 발행한다.

```text
입력 이벤트 = 3건
실제 유효 이벤트 = 1건
최종 집계 증가량 = 1
```

---

# 14. 정산 데이터 구조

광고 이벤트는 돈과 연결되므로 실시간 통계와 최종 정산 데이터를 구분한다.

```text
Realtime Counter
        +
Source of Truth Event
        +
Reconciliation Batch
```

```text
Realtime            대시보드에서 빠르게 확인하기 위한 값
Source of Truth     실제 발생한 광고 이벤트 기록
Reconciliation      실시간 집계와 원본 이벤트를 비교하여 차이를 보정
```

다루는 문제:

실시간 Dashboard에는 Impression이 10,001건인데 정산 데이터에는 10,000건이라면 어떻게 할 것인가?

---

# 15. 외부 광고 플랫폼 연동

실제 외부 플랫폼 대신 Mock Ad Exchange를 구현한다.

```text
AdFlow
  ↓
External Ad Adapter
  ↓
Mock Ad Exchange
```

추후 VAST 형태의 응답 구조를 단순화하여 구현할 수 있다.

목표:

```text
외부 광고 플랫폼 연동 구조 경험
Adapter 패턴 적용
외부 장애 / Timeout 처리
Fallback 광고 제공
```

이 단계는 MVP 이후 선택이다.

---

# 16. Legacy Migration 실험

신규 광고 플랫폼으로 점진 전환하는 구조를 추가할 수 있다.

```text
Legacy Ad Server
        │
        │ 90%
        ▼
      Client
        ▲
        │ 10%
        │
New Ad Server
```

전환 단계:

```text
Legacy 100%
  ↓
Legacy 90% / New 10%
  ↓
Legacy 50% / New 50%
  ↓
New 100%
```

Shadow Traffic으로 두 시스템의 결과를 비교한다.

```text
Legacy Result
vs
New Result
```

이 단계는 MVP 이후 선택이다.

---

# 17. AI 활용 방향

AI는 프로젝트 초기에 억지로 넣지 않는다.

핵심 시스템을 완성한 뒤 운영 자동화 영역에서 선택적으로 적용한다.

```text
Grafana Alert
  ↓
AI Incident Analyzer
  ↓
관련 메트릭 분석
  ↓
원인 후보 정리
  ↓
Runbook 제안
```

또는:

```text
장애 로그 요약
Kafka Lag 이상 원인 분석
캠페인 이상 트래픽 탐지 결과 설명
성능 테스트 결과 자동 요약
```

AI 자체보다 운영 업무 자동화라는 목적을 우선한다.

---

# 18. Demo Scenario

최종적으로 GitHub README나 포트폴리오에서 아래 흐름을 화면으로 보여줄 수 있어야 한다.

```text
1. Campaign Console에서 광고 캠페인 생성
2. OTT Player에서 콘텐츠 재생
3. 사용자 조건에 맞는 광고 노출
4. Impression / Click 이벤트 발생
5. Dashboard에서 실시간 성과 확인
6. 같은 사용자가 반복 재생
   → Frequency Cap으로 광고 변경
7. Traffic Simulator 실행
   → 요청량 / 이벤트량 증가
8. 캠페인 Budget 감소
   → 예산 소진 후 광고 중단
9. Dashboard에서 시스템 지표 확인
```

이 시나리오가 동작하면 단순 Backend API 모음이 아니라 실제 광고 플랫폼처럼 보인다.

---

# 19. 우선 개발 순서

초반에는 욕심내지 않고 아래 순서로 진행한다.

```text
1.  Campaign Domain
2.  Creative Domain
3.  Campaign CRUD
4.  User / Content Mock Data
5.  Ad Selection API
6.  OTT Player
7.  Impression / Click Event
8.  Campaign Dashboard
9.  Frequency Cap
10. Budget Control
11. Kafka Event Pipeline
12. Traffic Simulator
13. Real-time Dashboard
14. Performance Test
15. Concurrency Experiment
16. Event Deduplication
17. Reconciliation
18. Legacy Migration Experiment
```
