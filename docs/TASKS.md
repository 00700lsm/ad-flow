# TASKS

현재 Phase에서 무엇을 할지 정의한다.

요구사항의 정본은 `docs/REQUIREMENTS.md`,
구조의 정본은 `docs/DESIGN.md`,
문제 확인 순서는 `docs/ROADMAP.md`다.

이 문서는 그 세 문서를 다시 쓰지 않는다.

구현은 `.cursor/skills/ad-flow-vibe-coding/SKILL.md`의
Analysis → Plan(HITL) → Red → Green → Lint → Refactor 순서를 따른다.

---

# 1. 현재 Phase

```text
Phase 1
시연 가능한 제품 MVP
상태: DONE
```

목표:

Campaign Console에서 광고를 생성하고, OTT Player에서 해당 광고가 노출되며, Dashboard에서 Impression과 Click을 확인한다.

---

# 2. 이 Phase에서 하지 않는 것

```text
Redis / Kafka
Frequency Cap 동시성 해결
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

# 4. Phase 1 완료 조건

- [x] FR-01 ~ FR-08을 만족한다
- [x] Phase 2 기술을 미리 넣지 않았다
- [x] README로 Demo 경로를 따라갈 수 있다
- [x] DESIGN이 현재 코드와 맞다

다음 Phase 작업은 이 문서에 미리 넣지 않는다.
