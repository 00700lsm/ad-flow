# AdFlow - Requirements

## 1. 문서 목적

이 문서는 AdFlow가 **무엇을 만족해야 하는지** 정의한다.

구현 구조는 `DESIGN.md`, 문제 확인 순서는 `ROADMAP.md`, 현재 작업은 `TASKS.md`다.

```text
REQUIREMENTS.md  → 무엇을 만족해야 하는가
DESIGN.md        → 목표 / 현재 시스템은 어떻게 구성되는가
ROADMAP.md       → 어떤 문제를 어떤 순서로 확인할 것인가
TASKS.md         → 지금 Phase에서 무엇을 할 것인가
Poc.md           → 바이브 루프(상태 그래프)의 철학
```

이 문서는 특정 기술의 구현 방법을 정의하지 않는다.

```text
Redis
Kafka
Lua Script
Pessimistic / Optimistic Lock
SSE / WebSocket
k6 / Prometheus / Grafana
```

위 기술은 요구사항을 만족하기 위한 **후보**다. 실제 문제 재현과 측정 결과가 필요성을 뒷받침할 때만 도입한다.

---

# 2. 프로젝트 목표

대규모 요청 환경에서 광고를 저지연으로 선택·노출하고, 발생한 광고 이벤트를 유실·중복 없이 처리하여 정산 가능한 데이터로 만드는 미니 OTT 광고 플랫폼을 구현한다.

테스트나 부하 숫자가 결과물이 아니다.
사용자가 직접 조작하고 결과를 확인할 수 있는 서비스 형태를 먼저 만든다.

전체 스토리:

```text
실제로 동작하는 광고 플랫폼을 만들고
사용자가 증가했을 때 발생한 문제를 측정하고 개선했다
```

첫 번째 완료 조건:

Campaign Console에서 광고를 하나 생성하고 OTT Player를 실행했을 때 내가 만든 광고가 실제로 재생된다.

---

# 3. 서비스 범위

AdFlow가 다루는 핵심 흐름은 다음이다.

```text
캠페인 생성
  ↓
콘텐츠 재생
  ↓
광고 선택
  ↓
광고 노출
  ↓
Impression / Click
  ↓
성과 확인
```

프론트엔드는 시연 가능한 수준에 집중한다. 프로젝트의 중심은 Backend다.

포함하지 않는 것:

```text
실제 DSP / SSP / Ad Exchange 연동
실제 결제 / 정산 이체
실제 OTT 영상 CDN / DRM / 인코딩
회원가입 · 로그인 소셜 연동 UI
추천 · 검색 · 댓글
MongoDB
Kubernetes / AWS (명시적 요청 전)
프로젝트 초기의 운영 AI
```

---

# 4. 기능 요구사항

## 4.1 Phase 1 — 시연 가능한 제품 MVP

### FR-01 Campaign 관리

운영자는 캠페인을 생성·조회·수정·중지할 수 있다.

최소 필드:

```text
name, status, budget, startAt, endAt, priority,
targetAgeMin, targetAgeMax, targetCategory, frequencyCap
```

상태 최소값:

```text
ACTIVE
PAUSED
```

Phase 1에서는 `BUDGET_EXHAUSTED`를 자동 전환하지 않아도 된다.

### FR-02 Creative 등록

캠페인에 소재를 등록할 수 있다.

```text
type, mediaUrl, clickUrl
```

### FR-03 User / Content 샘플

광고 선택에 필요한 사용자와 콘텐츠 샘플이 있어야 한다.

```text
User     id, age, preferredCategories
Content  id, title, category
```

### FR-04 광고 선택

`GET /ads?userId={userId}&contentId={contentId}` 요청에 대해 후보를 고른다.

Phase 1에서 반드시 적용할 필터:

```text
활성 상태인가?
시작 / 종료 기간 안인가?
사용자 연령 타겟에 부합하는가?
콘텐츠 장르 조건에 부합하는가?
후보 중 Priority가 가장 높은가?
```

Frequency Cap과 Budget 차감의 **동시성 정확성**은 Phase 1 완료 조건이 아니다.

### FR-05 OTT Player

콘텐츠를 재생하면 광고 요청이 발생하고, 선택된 광고가 화면에 노출된다.

### FR-06 Impression / Click

광고 노출 시 Impression, 클릭 시 Click 이벤트가 기록된다.

Phase 1에서는 API가 동기적으로 저장해도 된다. Kafka는 필수 아니다.

### FR-07 기본 Dashboard

캠페인별 노출 수, 클릭 수, CTR을 확인할 수 있다.

실시간 스트림(SSE/WebSocket)은 Phase 1 범위가 아니다.

### FR-08 Demo 경로

다음이 한 흐름으로 가능해야 한다.

```text
Campaign Console에서 캠페인 생성
  ↓
OTT Player에서 콘텐츠 재생
  ↓
타겟에 맞는 광고 노출
  ↓
Impression / Click 발생
  ↓
Dashboard에서 수치 확인
```

## 4.2 이후 Phase — 문제 확인 후 만족

아래는 ROADMAP 해당 Phase의 완료 조건이다. Phase 1에서 구현하지 않는다.

### FR-09 Frequency Cap

동일한 사용자는 동일한 광고를 하루 최대 N번까지만 본다.
동시 요청에서도 한도를 넘지 않는다.

### FR-10 Budget Control

잔여 예산이 없으면 해당 캠페인은 노출되지 않는다.
동시 요청에서도 Overspending이 없어야 한다.
소진 시 상태는 `BUDGET_EXHAUSTED`다.

### FR-11 Event Pipeline 분리

Impression / Click 처리가 Ad Serving API와 분리되어 대량 이벤트를 받을 수 있다.
Consumer 장애 후에도 재처리 가능해야 한다.

### FR-12 이벤트 멱등

같은 `eventId`는 여러 번 들어와도 정산 집계가 한 번만 증가한다.

### FR-13 실시간 Dashboard

운영자가 Impression/sec, Click/sec, CTR, Budget, (있다면) Lag / p99 / Error Rate를 화면에서 볼 수 있다.

### FR-14 Traffic Simulator

가상 사용자 수·연령·장르 분포를 설정해 광고 요청을 발생시키고, 분배·예산·캡·대시보드 변화를 확인할 수 있다.

---

# 5. 비기능 요구사항

Phase 1의 NFR은 **기능이 반복 실행 가능한가**다.

```text
NFR-01  로컬에서 Docker Compose + 앱으로 Demo 경로를 재현할 수 있다
NFR-02  핵심 도메인/API는 실패하는 테스트 없이 기능을 추가하지 않는다
```

이후 Phase에서 측정으로 확인하는 후보:

```text
NFR-03  광고 서빙 지연 (p95 / p99)
NFR-04  처리량 (RPS)과 Error Rate
NFR-05  예산 Overspending 0
NFR-06  Frequency Cap 초과 0
NFR-07  중복 이벤트 집계 증가량 = 유효 이벤트 수
NFR-08  Consumer Lag 회복
```

수치 목표는 Experiment에서 정한다. REQUIREMENTS에 임의 RPS를 정답으로 적지 않는다.

---

# 6. 성공 기준

Phase 1 성공:

```text
내가 만든 캠페인 광고가 Player에 나온다
Impression / Click이 Dashboard에 남는다
```

프로젝트 전체 성공:

```text
Product → Problem → Experiment → Engineering → Result
가 화면과 측정 기록으로 남아 있다
```

다음만으로는 성공이 아니다.

```text
Redis를 썼다
Kafka를 붙였다
k6로 3,000 RPS를 넣었다
```
