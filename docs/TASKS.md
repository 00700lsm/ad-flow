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
현재 Task: T4-03 DONE
Phase 4: IN PROGRESS
T4-01 ~ T4-03: DONE
다음: Phase 4 다음 Task는 개발자가 요청할 때
```

---

# 1. 현재 Phase

```text
Phase 4
서빙과 이벤트 처리 결합
상태: IN PROGRESS
```

목표:

이벤트 적재가 느려도 광고 선택 API가 같이 멈추지 않는지 먼저 재현한다. 분리 기술은 측정 후 Human Gate에서 고른다.

Phase 3는 DONE이다. FR-11 전체를 한 Task로 구현하지 않는다.

---

# 2. 이 Phase에서 하지 않는 것

```text
Kafka / Redis / Consumer 재처리
k6 / Prometheus / Grafana
Traffic Simulator API
SSE / WebSocket
Mock Ad Exchange
Kubernetes / AWS
운영 AI
Frequency Cap / Budget 계약 변경
FR-12 멱등
```

ROADMAP에 있다는 이유만으로 구현하지 않는다.

---

# 3. Tasks

각 Task는 코드부터 쓰지 않는다.
`.agent/artifacts/<task-id>/analysis.md`와 `plan.md`를 남기고, Plan HITL 승인 후에 Red Test부터 시작한다.

## T4-03. 접수 직후 집계 공백 재현

상태: `DONE`

완료 조건:

```text
POST /events/impression 201 직후 Dashboard 노출이 아직 0일 수 있다
accepted=1 persisted=0 창을 테스트로 재현한다
Kafka / Outbox / 재처리 Consumer는 포함하지 않는다
Human Gate 전에 유실을 없애지 않는다
```

## T4-02. 이벤트 접수를 JVM 큐로 분리

상태: `DONE`

완료 조건:

```text
POST /events/* 는 INSERT가 끝나기 전에 201을 줄 수 있다
저장은 같은 JVM 워커가 한다
Kafka 없음
```

## T4-01. Serving·Event 커넥션 결합 재현

상태: `DONE`

완료 조건:

```text
이벤트 기록이 DB 커넥션을 붙잡고 있으면 GET /ads 가 같이 기다린다
대기를 테스트로 재현하고 시간을 남긴다
Kafka / 비동기 분리 / Consumer 재처리는 포함하지 않는다
Human Gate 전에 Serving과 이벤트 저장을 나누지 않는다
```

## T3-04. Dashboard에서 예산 소진 확인

상태: `DONE`

완료 조건:

```text
Player에서 예산을 소진하면 그 캠페인은 안 나오고, 다른 후보가 있으면 그 광고가 나온다
Dashboard에서 spentBudget이 늘고 잔여가 줄며, 소진 캠페인 상태가 BUDGET_EXHAUSTED다
전환은 Dashboard(상태·예산)와 Player 세션 이력(캠페인 이름)에서 읽는다
시연 경로는 README에 T3-03 계약(GET /ads가 예산을 소비)으로 적힌다
Redis / 새 HTTP 경로 / 예산 로직 변경은 포함하지 않는다
```

## T3-03. Budget 선택 시점 원자적 차감

상태: `DONE`

완료 조건:

```text
동시 GET /ads에서 spentBudget이 budget을 넘지 않는다
차감은 PostgreSQL에서 GET 시 원자적으로 오른다
Impression은 예산 카운터가 아니다
Redis / Lua / 후보 B / C 는 포함하지 않는다
Player 확인은 포함하지 않는다
```

## T3-02. Budget Overspend 재현

상태: `DONE`

완료 조건:

```text
동일 캠페인에 동시 GET /ads + Impression 시
spentBudget이 budget을 넘는 경우가 테스트로 재현된다
초과분을 측정한다
Redis / Lock / Lua 해법은 포함하지 않는다
Human Gate 전에 한도 준수로 고치지 않는다
```

## T3-01. Budget 순차 차감

상태: `DONE`

완료 조건:

```text
spentBudget이 budget에 도달하면 GET /ads가 그 캠페인을 고르지 않는다
Impression 1건당 spentBudget +1
도달 시 상태는 BUDGET_EXHAUSTED
다른 후보가 있으면 그 광고를 고른다
Redis / Lock / 동시성 보장은 포함하지 않는다
```

## T2-04. Player에서 Frequency Cap 이후 광고 변경 확인

상태: `DONE`

완료 조건:

```text
같은 사용자가 Player에서 반복 재생하면 캡에 걸린 캠페인은 안 나오고
다른 후보가 있으면 그 광고가 나온다
전환은 세션 노출 이력에서 캠페인 이름으로 읽는다
시연 경로는 README에 GET /ads 슬롯 소비로 적힌다
Redis / 새 HTTP API / 캡 로직 변경은 포함하지 않는다
```

## T2-03. Frequency Cap 선택 시점 원자적 INCR

상태: `DONE`

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
- [x] FR-09 동시 요청 한도 (T2-03 테스트)
- [x] Player에서 캡 이후 광고가 바뀌는 것을 확인

Phase 3:

- [x] T3-01 순차 Budget
- [x] FR-10 동시 요청 한도 (T3-03 테스트)
- [x] Dashboard에서 예산 감소와 BUDGET_EXHAUSTED 확인

Phase 4:

- [x] T4-01 Serving·Event 결합 재현
- [x] T4-02 JVM 큐 접수 (ADR 005)
- [x] T4-03 접수 직후 집계 공백 재현
- [ ] FR-11 Consumer 재처리 / Kafka (데모에서 A, ADR 006)

다음 Phase 작업은 이 문서에 미리 넣지 않는다.
