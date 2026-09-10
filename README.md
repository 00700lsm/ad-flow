# AdFlow

대규모 요청 환경에서 광고를 저지연으로 선택·노출하고, 발생한 광고 이벤트를 유실·중복 없이 처리하여 정산 가능한 데이터로 만드는 미니 OTT 광고 플랫폼이다.

이번 프로젝트는 성능 테스트나 기술 데모가 결과물이 아니다.
사용자가 직접 조작하고 결과를 확인할 수 있는 실제 서비스 형태를 먼저 만든다.

핵심:

```text
광고 캠페인을 직접 생성할 수 있다
OTT 플레이어에서 실제 광고가 노출된다
사용자 조건에 따라 서로 다른 광고가 선택된다
Frequency Cap과 광고 예산 소진을 화면에서 확인할 수 있다
Impression / Click 이벤트가 실시간으로 수집된다
관리자 대시보드에서 광고 성과를 확인할 수 있다
Traffic Simulator로 가상의 대규모 사용자를 발생시킬 수 있다
부하 테스트와 성능 개선은 실제 서비스의 확장 과정으로 진행한다
```

한 줄로는 이렇게 본다.

실제 광고 노출부터 이벤트 수집과 정산 데이터까지 이어지는 광고 플랫폼을 구현하고, 트래픽 증가 과정에서 발생하는 캐시, 동시성, 이벤트 중복, 데이터 정합성 문제를 측정하고 개선한 프로젝트.

설계 원문: [① AdFlow - Mini OTT Ad Platform 프로젝트 설계](https://cmcm.tistory.com/49)

---

## Current Status

```text
Phase 7
가상 사용자로 부하를 재현하는가
T7-01 공백 재현. T7-02 제품 HTTP (ADR 013)
T7-03 POST /simulations start/stop. T7-04 연령·장르 분포. T7-05 simulator.html. T7-06 Impression. Click 없음
Redis / Kafka 없음
```

Campaign Console에서 광고를 만들고, OTT Player에서 노출되며, Dashboard에서 Impression / Click / 사용 예산 / 상태를 확인할 수 있다.

`GET /ads`가 후보를 고를 때 당일 캡 카운터와 예산을 원자적으로 올린다. 한도·예산에 걸린 캠페인은 다음 후보가 있으면 그 광고를 고른다. Impression은 Dashboard 집계용이며 캡·예산 카운터가 아니다.
동시 GET 한도는 테스트로 고정했다. Redis는 쓰지 않는다. Kafka는 없다. Simulator는 POST /simulations와 simulator.html이 있고 start가 Impression을 남긴다. Click 확률은 없다.

---

## 실행 방법

Java 21과 Docker가 필요하다.

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
docker compose -f docker/docker-compose.yml up -d
./gradlew test
./gradlew bootRun
```

Postgres를 쓰지 않고 메모리 DB로 보려면:

```bash
./gradlew bootRun --args='--spring.profiles.active=mem'
```

브라우저:

```text
http://localhost:8080/              시작
http://localhost:8080/console.html  캠페인 생성
http://localhost:8080/player.html   광고 재생
http://localhost:8080/dashboard.html 성과
```

광고 영상은 업로드 첨부파일이 아니다. 저장소 `src/main/resources/static/ads/videos/`에 파일을 두고, 캠페인 소재 URL에 그 경로를 적으면 Spring이 정적 리소스로 내려준다.

```text
파일     src/main/resources/static/ads/videos/example.mp4
URL      /ads/videos/example.mp4
콘솔     소재 URL에 위 경로를 넣는다 (기본값)
Player   mediaUrl이 .mp4이면 <video>로 재생한다
```

S3 / 파일 업로드 API / CDN은 없다. 영상을 바꾸려면 리소스 폴더에 파일을 넣고 소재 URL만 맞추면 된다.

샘플: 사용자 1(28세, 스포츠) + 콘텐츠 1(축구 하이라이트)에 스포츠 캠페인이 붙는다.

Frequency Cap을 Player에서 보려면 Console에서 같은 타겟(20–39세, 스포츠) 캠페인 두 개를 만든다.

```text
고우선  이름 예: 아이폰  cap=1  priority=20
저우선  이름 예: 나이키  cap=2  priority=10
```

`/player.html`에서 사용자 A · 축구 하이라이트를 두 번 재생한다. 이번 세션 노출 이력이 다른 캠페인 이름이어야 한다. 재생할 때마다 `GET /ads`가 슬롯을 소비한다.

예산을 Dashboard에서 보려면 Console에서 같은 타겟(20–39세, 스포츠) 캠페인 두 개를 만든다. Frequency Cap은 0으로 둔다.

```text
고우선  이름 예: 아이폰 예산시연  budget=1  priority=20  cap=0
저우선  이름 예: 나이키 예산시연  budget=50000  priority=10  cap=0
```

같은 Player에서 두 번 재생한다. 이력 1번째는 고우선, 2번째는 저우선이어야 한다. `/dashboard.html`에서 고우선은 사용 1 / 잔여 0 / `BUDGET_EXHAUSTED`다. 재생할 때마다 `GET /ads`가 예산을 소비한다.

같은 `eventId` 중복 집계를 Dashboard에서 보려면 Console에서 캠페인·크리에이티브 하나를 만든 뒤, 그 id로 Impression을 **같은 eventId로 세 번** 보낸다. Player 반복 재생은 매번 새 `eventId`라서 이 시연이 아니다.

```bash
# campaignId / creativeId 는 Console에서 만든 값
curl -s -o /dev/null -w "%{http_code}\n" -X POST http://localhost:8080/events/impression \
  -H 'Content-Type: application/json' \
  -d '{"eventId":"dup-demo-1","campaignId":1,"creativeId":1,"userId":1,"contentId":1}'
# 위 요청을 두 번 더 반복한다. 세 번 모두 201이어야 한다.
```

`/dashboard.html`에서 해당 캠페인 노출은 1이다. HTTP 201은 큐 접수이며 INSERT 완료가 아니다.

---

## 핵심 방향

테스트 자체가 결과물이 되지 않도록 아래 순서로 진행한다.

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

---

## 화면

결과물은 아래 4개 화면을 중심으로 구성한다.

### OTT Player

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

### Campaign Console

광고 캠페인을 생성하고 관리하는 관리자 화면이다.

```text
캠페인 생성 / 수정 / 중지
광고 소재 등록
예산 · 노출 기간 · 타겟 · Priority · Frequency Cap 설정
캠페인 상태 조회
```

예시:

```text
캠페인: 아이폰 신제품 광고
예산: 50,000원
타겟: 20 ~ 30대
장르: 스포츠
Frequency Cap: 하루 2회
상태: ACTIVE
```

### Ad Dashboard

광고 이벤트와 성과를 확인하는 운영 대시보드다.

```text
Impression / Click / CTR
사용 예산 / 잔여 예산
Ad Requests/sec / Impression/sec
Kafka Consumer Lag
p95 / p99 latency
Error Rate
SSE 또는 WebSocket 실시간 이벤트 스트림
```

### Traffic Simulator

실제 사용자 수천 명을 대신해 가상의 OTT 사용자를 발생시킨다.

```text
동시 사용자 수 설정
연령대 · 콘텐츠 장르 분포 설정
사용자별 광고 요청 · Impression / Click 생성
캠페인별 광고 분배 결과 확인
```

---

## Current Architecture

목표 구조다. 구현은 Phase 1 MVP부터 단계적으로 맞춘다.

```text
Campaign Admin
      ↓
 PostgreSQL
      ↓ Campaign Sync
    Redis
      ↑
Client ──► Ad Serving API ─┬─► Ad Selection
                           ├─► Frequency Cap
                           ├─► Budget / Pacing
                           └─► External Ad Adapter
                                      ↓
                               Mock Ad Exchange

Client (impression / click)
      ↓
  Event API
      ↓
    Kafka
      ↓
 Dedup Worker / Aggregator
      ↓
 PostgreSQL
      ↓
 Billing Report
```

프론트엔드는 시연 가능한 수준에 집중하고, 프로젝트의 중심은 Backend에 둔다.
MongoDB는 명확한 사용 이유가 생기지 않는 한 넣지 않는다.

---

## 광고 선택 규칙

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

사용자 시나리오 예:

```text
사용자 A / 28세 / 관심 장르: 스포츠
  ↓
축구 콘텐츠 재생
  ↓
Ad Serving API
  ↓
타겟 조건에 맞는 광고 선택
  ↓
광고 재생 → Impression
```

같은 사용자가 반복 시청하면 Frequency Cap이 적용된다.

```text
1번째 재생 → 아이폰 광고
2번째 재생 → 아이폰 광고
3번째 재생 → 다른 광고
```

예산이 모두 소진되면 해당 광고는 더 이상 노출되지 않는다.

```text
50,000원 → 0원
ACTIVE → BUDGET_EXHAUSTED
```

---

## MVP API

초기에는 아래 API만 구현한다.

```text
Campaign
  POST   /campaigns
  GET    /campaigns
  GET    /campaigns/{id}
  PATCH  /campaigns/{id}
  POST   /campaigns/{id}/creatives

Ad Serving
  GET    /ads?userId={userId}&contentId={contentId}

Event
  POST   /events/impression
  POST   /events/click

Dashboard
  GET    /dashboard/campaigns/{campaignId}
  GET    /dashboard/summary

Simulator
  POST   /simulations
  POST   /simulations/{id}/start
  POST   /simulations/{id}/stop
```

---

## 핵심 도메인

```text
Campaign   id, name, status, budget, spentBudget, startAt, endAt,
           priority, targetAgeMin, targetAgeMax, targetCategory, frequencyCap

Creative   id, campaignId, type, mediaUrl, clickUrl

User       id, age, preferredCategories

Content    id, title, category

AdEvent    eventId, campaignId, creativeId, userId, contentId, type, occurredAt
```

---

## 기술 스택

```text
Backend          Java 21 / Spring Boot / Spring Data JPA
Data             PostgreSQL / Redis / Kafka
Frontend         Next.js / React
Observability    k6 / Prometheus / Grafana
Infra            Docker Compose
```

추후 필요 시 gRPC, Kubernetes, AWS를 검토한다.

---

## 개발 단계

성능 실험은 제품이 동작한 뒤에 한다.

### Phase 1. 실제 사용 가능한 제품 MVP

성능이 아니라 서비스 형태 완성이다.

```text
Campaign CRUD
Creative 등록
User / Content 샘플 데이터
광고 선택 로직
OTT Player
Impression / Click 이벤트
기본 Dashboard
```

완료 조건: Campaign Console에서 광고를 생성하고, OTT Player에서 해당 광고가 노출되며, Dashboard에서 Impression과 Click을 확인할 수 있다.

### Phase 2. Frequency Cap

동일한 사용자는 동일한 광고를 하루 최대 N번까지만 볼 수 있다.

초기에는 `GET frequency` 후 `INCR`로 구현하고, 동시 요청 Race Condition을 재현한다.
개선 후보는 Redis Atomic Operation, Lua Script다.
결과는 OTT Player에서 동일 광고가 더 이상 노출되지 않는 형태로 확인한다.

### Phase 3. Budget Control

캠페인 예산이 소진되면 광고 노출을 중단한다.

초기에는 PostgreSQL 기반으로 구현하고 동시 요청 Overspending을 재현한다.

```text
비교 후보: DB Pessimistic Lock / Optimistic Lock / Redis DECR / Redis Lua Script
비교 지표: 처리량, p95, p99, Overspending 여부, 실패율
```

결과는 Dashboard에서 예산이 감소하고 캠페인이 `BUDGET_EXHAUSTED`로 바뀌는 모습으로 보여준다.

### Phase 4. Kafka Event Pipeline

목표 그림은 Kafka다. **현재 코드는 JVM 메모리 큐+워커**다 (ADR 005). 데모 Phase 4는 접수 분리까지 닫았다 (ADR 007). 재처리·Kafka는 없다.

```text
OTT Player → Event API → 메모리 큐 → 워커 INSERT → Aggregation
```

### Phase 5. 이벤트 중복과 정합성

Kafka 재처리에서 같은 Impression이 여러 번 처리되면 정산이 틀어진다.

```text
At-least-once Delivery
+ Idempotent Consumer
+ eventId 기반 Deduplication
```

### Phase 6. 실시간 Dashboard

Kafka Consumer 결과를 SSE 또는 WebSocket으로 보여준다.

### Phase 7. Traffic Simulator

가상 사용자를 만들어 광고 요청을 발생시킨다.

```text
User 생성 → Content 선택 → Ad Request → 광고 노출 → Impression → 일부 확률로 Click
```

시뮬레이터로 캠페인별 노출 분배, Budget 감소, Frequency Cap, 요청량·Kafka 이벤트 증가, Dashboard 변화를 직접 확인한다.

---

## 성능 개선 실험

제품이 완성된 이후 진행한다.

```text
Experiment 1  PostgreSQL 기반 광고 서빙 병목 → Redis 도입 전후 비교
              100 / 500 / 1,000 / 3,000 RPS
              RPS, p50, p95, p99, Error Rate, DB Connection, CPU

Experiment 2  광고 예산 동시성
              남은 노출 100, 동시 요청 1,000 → 노출이 100을 넘지 않는지

Experiment 3  Frequency Cap Race Condition
              하루 최대 3회 노출이 동시 요청에서 깨지는지

Experiment 4  Kafka Event Duplication
              입력 3건 / 유효 이벤트 1건 / 최종 집계 증가량 1
```

핵심은 Redis를 썼다는 사실이 아니다.
PostgreSQL 기반 광고 후보 검색에서 병목이 났고, Redis 도입 후 같은 조건에서 지연과 DB 부하가 얼마나 줄었는지를 측정한다.

---

## 정산 데이터

광고 이벤트는 돈과 연결되므로 실시간 통계와 최종 정산을 구분한다.

```text
Realtime Counter
+ Source of Truth Event
+ Reconciliation Batch
```

Realtime은 대시보드용 빠른 값이고, Source of Truth는 실제 발생한 이벤트 기록이다.
둘의 차이가 나면 Reconciliation으로 보정한다.

---

## Demo Scenario

최종적으로 아래 흐름을 화면에서 직접 보여줄 수 있어야 한다.

```text
1. Campaign Console에서 광고 캠페인 생성
2. OTT Player에서 콘텐츠 재생
3. 사용자 조건에 맞는 광고 노출
4. Impression / Click 이벤트 발생
5. Dashboard에서 실시간 성과 확인
6. 같은 사용자가 반복 재생 → Frequency Cap으로 광고 변경
7. Traffic Simulator 실행 → 요청량 / 이벤트량 증가
8. 캠페인 Budget 감소 → 예산 소진 후 광고 중단
9. Dashboard에서 시스템 지표 확인
```

이 시나리오가 동작하면 단순 Backend API 모음이 아니라 실제 광고 플랫폼처럼 보인다.

---

## 우선 개발 순서

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

이후 선택적으로 다룰 수 있는 것:

```text
Mock Ad Exchange Adapter (Timeout / Fallback)
Legacy → New 점진 전환과 Shadow Traffic
운영 자동화 영역의 AI (장애 로그 요약, Lag 원인 분석)
```

AI는 핵심 시스템을 완성한 뒤에만 검토한다.

---

## 바이브 루프

구현은 대화로 바로 코드를 쓰지 않는다. 사람용 설명은 저장소 루트 `HARNESS.md`다.

```text
Analysis → Plan(HITL) → Red → Green → Lint → Refactor → Summary
```

```text
HARNESS.md                                   하네스·HITL·Artifact 설명 (사람용)
AGENTS.md                                    에이전트 진입점
.cursor/rules/ad-flow.mdc                    항상 적용 규칙
.cursor/skills/ad-flow-vibe-coding/SKILL.md  Task 루프
.agent/config.yaml                           런타임 / 거버넌스
.agent/artifacts/<task-id>/                  현재 Task 산출물
docs/Poc.md                                  상태 그래프 철학
```

새 대화에서 개발자가 말할 것:

```text
다음
```

에이전트가 TASKS 포인터와 문서를 연다. 경로를 나열하지 않아도 된다.

---

## 문서

```text
HARNESS.md            에이전트 하네스·HITL·Artifact (사람용)
docs/REQUIREMENTS.md  무엇을 만족해야 하는가
docs/DESIGN.md        목표 / 현재 시스템 구조
docs/ROADMAP.md       문제 확인 순서
docs/TASKS.md         현재 Phase 작업
docs/Poc.md           바이브 하네스 철학
docs/adr/             설계 결정
docs/experiments/     측정 결과
```

설계 원문: [① AdFlow - Mini OTT Ad Platform 프로젝트 설계](https://cmcm.tistory.com/49)
