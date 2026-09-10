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
현재 Task: T7-11 DONE
Phase 7: DONE (ADR 015)
T7-01 ~ T7-11: DONE
다음: Experiment는 개발자가 요청할 때
```

---

# 1. 현재 Phase

```text
Phase 7
가상 사용자로 부하를 재현하는가
상태: DONE (ADR 015)
```

목표:

Phase 7 데모는 HTTP Simulator와 Dashboard 시연까지다. 연속 부하·k6·Kafka는 한계다.

Phase 6는 DONE이다 (ADR 012).

---

# 2. 이 Phase에서 하지 않는 것

```text
Kafka / Redis / Consumer 재처리
k6 / Prometheus / Grafana
Human Gate 전 SSE / WebSocket / 초당 지표
Mock Ad Exchange
Kubernetes / AWS
운영 AI
Frequency Cap / Budget / UNIQUE 계약 변경
Experiment RPS
FR-14 전체 (분배 화면까지) 한 Task
```

ROADMAP에 있다는 이유만으로 구현하지 않는다.

---

# 3. Tasks

각 Task는 코드부터 쓰지 않는다.
`.agent/artifacts/<task-id>/analysis.md`와 `plan.md`를 남기고, Plan HITL 승인 후에 Red Test부터 시작한다.

## T7-11. Phase 7 데모 범위 고정

상태: `DONE`

완료 조건:

```text
FR-14 중 충족(HTTP Simulator·시연)과 한계(연속 부하·k6·Kafka)를 문서로 고정한다
Phase 7를 데모 범위로 닫는다 (ADR 015)
k6 / Kafka / 연속 루프 구현은 포함하지 않는다
```

## T7-10. Simulator로 Dashboard 변화 시연

상태: `DONE`

완료 조건:

```text
README에 simulator.html → start → Dashboard 경로가 있다
시연 후 노출·클릭 또는 spentBudget이 오른 관찰을 artifacts/T7-10에 남긴다
k6 / Kafka / 연속 루프 / Phase 닫기는 포함하지 않는다
```

## T7-09. Simulator 폼 30대·clickRate

상태: `DONE`

완료 조건:

```text
simulator.html에 30대 %와 clickRate가 있다
POST /simulations body에 ageShares 30대와 clickRate가 들어간다
기존 20대·40대·장르·start/stop은 유지한다
새 API / k6 / Kafka는 포함하지 않는다
```

## T7-08. 30대 샘플 User

상태: `DONE`

완료 조건:

```text
샘플 User 3이 30대다
start가 ageShares 30대를 그 User로 GET /ads 한다
20대·40대 매핑은 T7-04와 같다
simulator.html 30대 입력 / k6 / Kafka는 포함하지 않는다
```

## T7-07. start 후 Click

상태: `DONE`

완료 조건:

```text
POST /simulations 가 clickRate를 받는다
start가 Impression 뒤에 그 비율만큼 Click을 남긴다
Dashboard 클릭이 그 횟수만큼 오른다
clickRate를 안 내면 Click은 0이다
stop 후 start면 Click을 안 낸다
30대 시드 / k6 / Kafka는 포함하지 않는다
```

## T7-06. start 후 Impression

상태: `DONE`

완료 조건:

```text
POST /simulations/{id}/start 가 선택마다 Impression을 남긴다
Dashboard 노출이 그 횟수만큼 오른다
stop 후 start면 Impression을 안 낸다
Click 확률 / 30대 시드 / k6 / Kafka는 포함하지 않는다
```

## T7-05. Simulator 화면

상태: `DONE`

완료 조건:

```text
GET /simulator.html 이 200이다
index에 Simulator 링크가 있다
화면에서 concurrentUsers·20대/40대·스포츠/드라마를 넣고
POST /simulations 와 start / stop 을 호출한다
Impression 루프 / 30대 시드 / k6 / Kafka는 포함하지 않는다
```

## T7-04. 연령·장르 분포로 광고 요청

상태: `DONE`

완료 조건:

```text
POST /simulations 가 연령 비율·장르를 받는다
start가 그 분포로 샘플 User/Content를 골라 GET /ads와 같은 선택을 한다
비율·장르를 안 내면 T7-03과 같이 User 1 · Content 1
simulator.html / Impression 루프 / k6 / Kafka는 포함하지 않는다
```

## T7-03. POST /simulations 로 광고 요청

상태: `DONE`

완료 조건:

```text
POST /simulations 로 concurrentUsers를 받는다
POST /simulations/{id}/start 가 그 횟수만큼 GET /ads 와 같은 선택을 한다
POST /simulations/{id}/stop 이 start 전이면 요청을 안 낸다
k6 / 분포 UI / Kafka는 포함하지 않는다
```

## T7-02. Phase 7 다음 방향 고정

상태: `DONE`

완료 조건:

```text
FR-14 중 재현(T7-01 Simulator 없음)과 다음 방향(후보 C)을 문서로 고정한다
Phase 7를 닫지 않는다
POST /simulations 구현 / k6 / Kafka는 포함하지 않는다
```

## T7-01. Traffic Simulator 공백 재현

상태: `DONE`

완료 조건:

```text
POST /simulations 가 없다
시뮬레이터 화면이 없다
공백을 테스트로 재현하고 남긴다
Simulator 구현 / k6 / Kafka는 포함하지 않는다
Human Gate 전에 가상 트래픽을 넣지 않는다
```

## T6-02. Phase 6 데모 범위 고정

상태: `DONE`

완료 조건:

```text
FR-13 중 재현(T6-01 폴링 공백)과 한계(ADR 011)를 문서로 고정한다
Phase 6를 데모 범위로 닫는다 (ADR 012)
SSE / 초당 지표 / Kafka는 포함하지 않는다
```

## T6-01. Dashboard 초당 지표·실시간 푸시 공백 재현

상태: `DONE`

완료 조건:

```text
GET /dashboard 응답에 Impression/sec · Click/sec · Lag가 없다
dashboard.html은 3초 폴링이고 SSE가 없다
공백을 테스트로 재현하고 남긴다
SSE / 초당 지표 구현 / Kafka는 포함하지 않는다
Human Gate A. 데모에서 3초 폴링 감수 (ADR 011)
```

## T5-06. Phase 5 데모 범위 고정

상태: `DONE`

완료 조건:

```text
FR-12가 T5-03 테스트와 T5-05 시연으로 충족임을 문서로 고정한다
Phase 5를 데모 범위로 닫는다 (ADR 010)
SSE / Kafka / UNIQUE 재구현은 포함하지 않는다
```

## T5-05. Dashboard에서 중복 eventId 집계 1 시연

상태: `DONE`

완료 조건:

```text
같은 eventId Impression을 세 번내면 Dashboard 노출이 1이다
시연 경로는 README에 같은 eventId POST 3회로 적힌다
관찰을 artifacts/T5-05에 남긴다
UNIQUE / 새 HTTP / SSE는 포함하지 않는다
```

## T5-04. Phase 5 다음 방향 고정

상태: `DONE`

완료 조건:

```text
FR-12가 T5-03 테스트로 충족임을 문서로 고정한다
Phase 5를 닫지 않고 화면 시연으로 연다 (후보 B)
SSE / Kafka / UNIQUE 재구현은 포함하지 않는다
```

## T5-03. eventId UNIQUE 집계 한 번

상태: `DONE`

완료 조건:

```text
같은 eventId로 POST /events/impression 3회면 Dashboard 노출은 1이다
입력 3 / 유효 1 / 집계 증가량 1
UNIQUE(eventId). 충돌 INSERT는 집계에 안 넣는다
Kafka / Redis / exists-only 스킵은 포함하지 않는다
```

## T5-02. Phase 5 다음 방향 고정

상태: `DONE`

완료 조건:

```text
FR-12 재현(T5-01)과 한계(ADR 008)를 문서로 고정한다
Phase 5를 닫지 않고 UNIQUE 쪽으로 연다 (후보 C)
UNIQUE / upsert 구현은 포함하지 않는다
```

## T5-01. 동일 eventId 중복 집계 재현

상태: `DONE`

완료 조건:

```text
같은 eventId로 POST /events/impression 이 세 번 오면 Dashboard 노출이 3일 수 있다
입력 3 / 유효 키 1 / 집계 +3 을 테스트로 재현하고 남긴다
unique / upsert / Kafka 멱등은 포함하지 않는다
Human Gate 전에 한 번만 집계되도록 고치지 않는다
```

## T4-06. Phase 4 데모 범위 고정

상태: `DONE`

완료 조건:

```text
FR-11 중 충족(T4-02 접수 분리)과 한계(재처리 ADR 006, 풀 공유 T4-04)를 문서로 고정한다
Phase 4를 데모 범위로 닫는다 (ADR 007)
Kafka / Outbox / 별도 DataSource 구현은 포함하지 않는다
```

## T4-05. 워커 장애 후 재처리 불가 재현

상태: `DONE`

완료 조건:

```text
워커가 take 이후 INSERT 전에 죽으면, 재시작해도 그 Impression은 집계에 안 남는다
재처리 불가를 테스트로 재현하고 남긴다
Kafka / Outbox / 별도 Consumer는 포함하지 않는다
Human Gate 전에 재처리를 넣지 않는다
```

## T4-04. 워커 INSERT와 GET /ads 풀 공유 재현

상태: `DONE`

완료 조건:

```text
워커가 ad_events INSERT 커넥션을 붙잡고 있으면 GET /ads 가 같이 기다린다
대기를 테스트로 재현하고 시간을 남긴다
Kafka / Outbox / 별도 DataSource / Consumer 재처리는 포함하지 않는다
Human Gate 전에 풀 결합을 고치지 않는다
```

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
- [x] T4-04 워커·GET 풀 공유 재현 (해법 A, ADR 007)
- [x] T4-05 워커 장애 후 재처리 불가 재현
- [x] T4-06 Phase 4 데모 범위 고정 (ADR 007)
- [x] FR-11 접수 분리 (데모, T4-02)
- [ ] FR-11 Consumer 재처리 / Kafka (ADR 006, Phase 4에서 미충족)

Phase 5:

- [x] T5-01 동일 eventId 중복 집계 재현
- [x] T5-02 다음 방향 UNIQUE (ADR 008 재검토)
- [x] T5-03 eventId UNIQUE 집계 한 번 (ADR 009)
- [x] T5-04 다음 방향 화면 시연
- [x] T5-05 Dashboard에서 중복 eventId 집계 1 시연
- [x] T5-06 Phase 5 데모 범위 고정 (ADR 010)
- [x] FR-12 멱등 (입력 3 / 유효 1 / 집계 +1, T5-03 테스트 + T5-05 시연)

Phase 6:

- [x] T6-01 Dashboard 초당 지표·실시간 푸시 공백 재현
- [x] T6-02 Phase 6 데모 범위 고정 (ADR 012)
- [ ] FR-13 실시간 Dashboard (ADR 011·012, Phase 6에서 미충족)

Phase 7:

- [x] T7-01 Traffic Simulator 공백 재현
- [x] T7-02 다음 방향 POST /simulations (ADR 013)
- [x] T7-03 POST /simulations 로 광고 요청
- [x] T7-04 연령·장르 분포로 광고 요청
- [x] T7-05 Simulator 화면
- [x] T7-06 start 후 Impression
- [x] T7-07 start 후 Click
- [x] T7-08 30대 샘플 User
- [x] T7-09 Simulator 폼 30대·clickRate
- [x] T7-10 Simulator로 Dashboard 변화 시연
- [x] T7-11 Phase 7 데모 범위 고정 (ADR 015)
- [x] FR-14 HTTP Simulator (데모, T7-03~T7-10)
- [ ] FR-14 연속 부하 / k6 / Kafka Event (ADR 015, Phase 7에서 미충족)

다음 Phase 작업은 이 문서에 미리 넣지 않는다.
