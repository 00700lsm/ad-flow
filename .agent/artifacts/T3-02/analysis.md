# Analysis

```text
Task: T3-02
Phase: 3
Date: 2026-09-07
```

## 요청

T3-01 다음. 동시 광고 요청에서 예산이 한도를 넘는지 재현한다. 해법은 Human Gate 전에 고르지 않는다.

## 근거 문서

```text
REQUIREMENTS  FR-10 잔여 없으면 미노출. 동시 Overspending 없어야 함 (Phase 완료)
DESIGN        4절: 순차 Impression 차감. 동시 Overspending 한도 없음
              12.3: PostgreSQL로 Overspending 재현. Lock/Redis는 측정 후 후보
ROADMAP       Phase 3: 남은 100 vs 동시 1,000. 재현 후 Human Gate
TASKS         T3-01 DONE. FR-10 전체를 한 Task로 구현하지 않음
              다음: 개발자가 요청할 때 → 이번 요청
ADR           001: Task 하나. FR-10 한 번에 구현 금지
              003: Frequency Cap GET 원자적 INCR. Budget 해법이 아님
```

문서 충돌:

```text
없음. FR-10 동시성 0은 Phase 완료 조건이다.
T3-02는 재현·측정·후보 보고까지다. Redis/Lock은 Human Gate + ADR 이후 Task다.
TASKS에 T3-02 행이 없었던 것은 Phase 3를 T3-01만 열고 다음을 미리 안 적어서다.
```

현재 코드(문서 4절과 일치):

```text
GET /ads 는 spentBudget >= budget 이면 제외한다 (읽기)
POST /events/impression 이 spentBudget +1, 도달 시 BUDGET_EXHAUSTED
두 GET이 모두 spent=0을 보면 같은 캠페인을 고른다
chargeImpression 에 잔여 비교 원자 연산 / Lock 없음
Frequency Cap은 GET 시점 원자적 INCR (ADR 003). 예산과 시점·저장소가 다르다
```

Race 가설:

```text
budget=1, frequencyCap=0, 동시에 GET /ads 여러 건
모두 spent=0 < 1 → 같은 캠페인 반환
각자 Impression → chargeImpression +1
최종 spentBudget > budget
```

## 제약

```text
이 Phase에서 해도 되는 것
  동시 GET /ads + Impression으로 Overspending을 테스트로 재현
  초과분을 artifacts에 남김
  Human Gate로 후보만 보고 (구현 없음)

하면 안 되는 것
  Redis / Kafka / Lua / Distributed Lock / Optimistic Lock / 원자적 DECR
  Overspending 0을 이 Task 완료 조건으로 고정
  Frequency Cap 계약 변경
  Simulator / SSE / k6 / Prometheus / Grafana
  DESIGN에 없는 API
  Dashboard 사용·잔여 UI / Player 개편 (Phase 완료 항목)

영향 파일 후보
  재현 테스트 (신규). 프로덕션 코드는 이 Task에서 바꾸지 않음
  (승인 후) artifacts/T3-02 측정. TASKS 포인터
```

## 하지 않는 이유

초과가 측정되기 전에 Lock/Redis를 넣으면 ROADMAP의 재현 → 측정 → 후보 → Human Gate를 건너뛴다.
GET 시점에 예산을 깎으면 Frequency Cap과 같은 해법을 선점하고, 현재 Impression 차감과 어긋난다.
Dashboard/Player 확인은 순차 소진이 고정된 뒤의 화면 검증이다. Race와 섞지 않는다.

## Exit

```text
Valid: yes
다음: plan.md
```
