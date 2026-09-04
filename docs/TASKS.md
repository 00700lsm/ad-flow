# TASKS

현재 Phase에서 무엇을 할지 정의한다.

요구사항의 정본은 `docs/REQUIREMENTS.md`,
구조의 정본은 `docs/DESIGN.md`,
문제 확인 순서는 `docs/ROADMAP.md`다.

이 문서는 그 세 문서를 다시 쓰지 않는다.

구현은 `.cursor/skills/ad-flow-vibe-coding/SKILL.md`의
Analysis → Plan(HITL) → Red → Green → Lint → Refactor 순서를 따른다.

Phase 2 Task는 이 문서를 열 때 적는다. FR-09 전체를 한 Task로 구현하지 않는다.

한 요청의 단위는 이 문서의 **Task 하나**다.
Phase를 한 번에 구현하지 않는다. `docs/adr/001-one-task-at-a-time.md`.

에이전트는 아래 포인터를 먼저 본다. 사용자에게 문서 경로를 묻지 않는다.

```text
현재 Task: T2-03
Phase 2: IN_PROGRESS
T2-01: DONE
T2-02: DONE (Race 재현)
Human Gate: A 선택 (PostgreSQL 원자적 INCR). Redis 아님
다음: T2-03 Plan HITL. 승인 전 구현 없음
```

---

# 1. 현재 Phase

```text
Phase 2
동일 사용자 과다 노출
상태: IN_PROGRESS
```

목표:

같은 사용자가 같은 광고를 하루 N번 넘게 보지 않게 한다. T2-03은 선택 시점 원자적 INCR(ADR 003)만 다룬다.

Phase 1은 DONE이다.

---

# 2. 이 Phase에서 하지 않는 것

```text
Redis / Kafka / Lua / Distributed Lock
후보 B / 후보 C
Budget Overspending 해결
Traffic Simulator API
SSE / WebSocket
k6 / Prometheus / Grafana
Mock Ad Exchange
Kubernetes / AWS
운영 AI
```

ROADMAP에 있다는 이유만으로 구현하지 않는다.

---

# 3. Tasks

각 Task는 코드부터 쓰지 않는다.
`.agent/artifacts/<task-id>/analysis.md`와 `plan.md`를 남기고, Plan HITL 승인 후에 Red Test부터 시작한다.

## T2-03. Frequency Cap 선택 시점 원자적 INCR

상태: `TODO`

완료 조건:

```text
동시 GET /ads에서 당일 캡 카운터가 frequencyCap을 넘지 않는다
카운터는 PostgreSQL에서 GET 시 원자적으로 오른다
Impression은 캡 카운터가 아니다
Redis / Lua / 후보 B / C 는 포함하지 않는다
Player 확인은 포함하지 않는다
```

## T2-02. Frequency Cap Race 재현

상태: `DONE`

완료 조건:

```text
동일 userId로 동시 GET /ads + Impression 시
당일 IMPRESSION이 frequencyCap을 넘는 경우가 테스트로 재현된다
초과분을 측정한다
Redis / Lock / Lua 해법은 포함하지 않는다
Human Gate 전에 한도 준수로 고치지 않는다
```

## T2-01. Frequency Cap 순차 GET/INCR

상태: `DONE`

완료 조건:

```text
동일 userId + campaignId의 당일 Impression이 frequencyCap에 도달하면
GET /ads가 그 캠페인을 고르지 않는다
다른 후보가 있으면 그 광고를 고른다
Redis / Lock / 동시성 보장은 포함하지 않는다
```

## T1-01. Campaign Domain

상태: `DONE`

완료 조건:

```text
Campaign 필드와 상태가 테스트로 고정된다
ACTIVE / PAUSED 전이가 가능하다
```

## T1-02. Creative Domain

상태: `DONE`

완료 조건:

```text
캠페인에 Creative를 붙일 수 있다
mediaUrl / clickUrl이 저장된다
```

## T1-03. Campaign CRUD API

상태: `DONE`

완료 조건:

```text
POST/GET/PATCH /campaigns
POST /campaigns/{id}/creatives
가 실패 테스트 → 최소 구현으로 통과한다
```

## T1-04. User / Content 샘플

상태: `DONE`

완료 조건:

```text
로컬에서 광고 선택에 쓸 User / Content가 있다
```

## T1-05. Ad Selection API

상태: `DONE`

완료 조건:

```text
GET /ads?userId=&contentId=
활성 / 기간 / 연령 / 장르 / Priority 규칙이 테스트로 확인된다
```

## T1-06. Impression / Click API

상태: `DONE`

완료 조건:

```text
POST /events/impression
POST /events/click
Dashboard 집계의 입력이 된다
```

## T1-07. 기본 Dashboard API

상태: `DONE`

완료 조건:

```text
GET /dashboard/campaigns/{campaignId}
GET /dashboard/summary
노출 / 클릭 / CTR을 반환한다
```

## T1-08. OTT Player / Campaign Console 화면

상태: `DONE`

완료 조건:

```text
콘솔에서 캠페인을 만들고 Player에서 광고가 보인다
클릭 시 Click이 발생한다
```

## T1-09. Demo 경로 검증

상태: `DONE`

완료 조건:

```text
FR-08 한 흐름을 README에 적힌 방법으로 재현한다
DESIGN.md가 Phase 1 코드와 일치한다
```

---

# 4. Phase 완료 조건

Phase 1:

- [x] FR-01 ~ FR-08을 만족한다
- [x] Phase 2 기술을 미리 넣지 않았다
- [x] README로 Demo 경로를 따라갈 수 있다
- [x] DESIGN이 현재 코드와 맞다

Phase 2:

- [x] T2-01 순차 Frequency Cap
- [ ] FR-09 동시 요청 한도 (T2-03 Plan HITL)
- [ ] Player에서 캡 이후 광고가 바뀌는 것을 확인

다음 Phase 작업은 이 문서에 미리 넣지 않는다.
