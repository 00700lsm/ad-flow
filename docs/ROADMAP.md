# AdFlow - ROADMAP

## 1. 문서 목적

이 문서는 AdFlow에서 **어떤 문제를 어떤 순서로 확인할 것인지** 정의한다.

ROADMAP은 기술 설치 목록이 아니다.

피한다:

```text
Spring Boot
↓
Redis
↓
Kafka
↓
k6
↓
Prometheus
```

선호한다:

```text
만든 광고가 실제로 재생되는가?
같은 사용자가 광고를 너무 자주 보는가?
동시 요청에서 예산이 초과 소진되는가?
서빙 API가 이벤트 적재에 묶여 있는가?
같은 이벤트가 정산을 두 번 올리는가?
운영자가 이벤트를 화면에서 볼 수 있는가?
가상 사용자로 부하를 재현할 수 있는가?
```

원칙:

```text
Phase에 도달했다
≠
해당 기술을 반드시 도입한다
```

Redis, Kafka, Lock, Lua, SSE는 문제와 측정이 필요성을 뒷받침할 때만 도입한다.

전체 방향은 블로그 Goal, `REQUIREMENTS.md`의 프로젝트 목적과 같아야 한다.

---

# 2. 전체 진행 흐름

각 Phase는 가능한 한 다음을 유지한다.

```text
현재 상태
  ↓
문제 확인 / 기능 완료 조건
  ↓
(필요하면) 재현과 측정
  ↓
원인 분석
  ↓
후보 비교
  ↓
Human Gate
  ↓
ADR
  ↓
구현
  ↓
같은 조건 재확인
  ↓
DESIGN / TASKS 갱신
```

Phase 1은 성능 실험이 아니다. 제품이 먼저다.

---

# 3. Phase

## Phase 1. 시연 가능한 제품 MVP

상태: `DONE`

질문:

만든 광고가 Player에 나오고, Dashboard에 이벤트가 남는가?

포함:

```text
Campaign CRUD
Creative 등록
User / Content 샘플
광고 선택 (활성 / 기간 / 타겟 / 장르 / Priority)
OTT Player
Impression / Click 동기 기록
기본 Dashboard
```

하지 않는 것:

```text
Redis / Kafka
Frequency Cap 동시성 정확성
Budget Overspending 방지
Traffic Simulator
실시간 SSE
k6 부하 테스트
Mock Ad Exchange
```

완료 조건: `REQUIREMENTS.md` FR-01 ~ FR-08.

---

## Phase 2. 동일 사용자 과다 노출

질문:

같은 사용자가 같은 광고를 하루 N번 넘게 보는가?

초기에는 단순 GET/INCR로 재현 가능하게 두고, Race Condition을 확인한 뒤 후보를 비교한다.

완료 조건: FR-09. 결과는 Player에서 광고가 바뀌는 것으로 확인한다.

---

## Phase 3. 예산 초과 소진

질문:

남은 노출이 100인데 동시 요청 1,000이면 100을 넘는가?

PostgreSQL 기반으로 먼저 Overspending을 재현한다.
Lock / Redis 후보는 측정 후 Human Gate에서 고른다.

완료 조건: FR-10. Dashboard에서 예산 감소와 `BUDGET_EXHAUSTED`를 확인한다.

---

## Phase 4. 서빙과 이벤트 처리 결합

질문:

이벤트 적재가 느려지면 광고 선택 API도 같이 느려지는가?
대량 Impression을 Serving 경로에서 감당할 수 있는가?

완료 조건: FR-11. 기술은 결과가 필요성을 말할 때만 고른다.

---

## Phase 5. 중복 이벤트와 정산

질문:

같은 `eventId`가 세 번 들어오면 집계가 세 번 오르는가?

완료 조건: FR-12.

```text
입력 3건 / 유효 1건 / 집계 증가량 1
```

---

## Phase 6. 운영자가 이벤트를 보는가

질문:

시스템 내부 이벤트를 화면에서 실시간으로 볼 수 있는가?

완료 조건: FR-13.

---

## Phase 7. 가상 사용자로 부하를 재현하는가

질문:

Simulator로 분배·예산·캡·대시보드 변화를 제품 화면에서 볼 수 있는가?

완료 조건: FR-14.

성능 숫자(RPS 단계)는 이 Phase 이후 Experiment로 분리한다.

---

# 4. 이후 Experiment (제품 완성 후)

ROADMAP Phase가 기술을 강제하지 않듯, Experiment도 해결 기술 이름으로 짓지 않는다.

```text
Experiment 1  광고 서빙 경로의 DB 조회 병목
Experiment 2  예산 동시성 Overspending
Experiment 3  Frequency Cap Race Condition
Experiment 4  동일 eventId 중복 집계
```

부하 단계 후보는 100 / 500 / 1,000 / 3,000 RPS다.
목표 RPS를 미리 성공 기준으로 고정하지 않는다.

---

# 5. 선택 Phase

아래는 ROADMAP 본선이 아니다. 본선 완료 후 검토한다.

```text
Mock Ad Exchange Adapter (Timeout / Fallback)
Legacy → New 점진 전환과 Shadow Traffic
운영 자동화 AI
```

---

# 6. 현재 위치

```text
Current Phase: Phase 2
상태: IN_PROGRESS
T2-01: DONE (순차 Frequency Cap)
다음 Task: 개발자 요청 시
```
