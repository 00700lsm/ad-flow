# ADR 004 - Budget은 선택 시점 원자적 차감으로 막는다

## 문제

동시 GET /ads 후 Impression이 `budget`을 넘는다.
T3-02 측정: requests=16 selected=16 impressions=16 budget=1 spent=16 overflow=15.
Human Gate에서 차감 시점과 저장소를 고른다.

## 현재 Context

```text
관련 Phase      3
관련 Experiment T3-02 measurement.txt
현재 코드       GET은 spent 읽기
                차감은 POST /events/impression chargeImpression
측정된 사실     Overspend는 있다. 해법 코드는 없다
개발자 선택     GET 시점 PostgreSQL 원자적 +1 (T3-03 승인)
```

## 검토한 대안

```text
1. GET에서 spent < budget 일 때만 spentBudget +1 (PostgreSQL)
2. Impression 조건부 UPDATE만 (T3-02 후보 A)
3. GET+Impression 구간 DB Lock (후보 B)
4. Redis DECR / Lua (후보 C, DESIGN 12.3)
```

## 선택

대안 1.

`GET /ads`가 후보를 고를 때 `spent_budget < budget`인 행만 원자적으로 +1 한다.
도달하면 `BUDGET_EXHAUSTED`. 실패하면 다음 후보다.
Impression INSERT는 예산을 건드리지 않는다.

PostgreSQL을 고른 이유: Serving이 이미 쓰는 저장소다. Frequency Cap(ADR 003)과 같다.
Impression만 막으면 노출(GET 200)은 그대로 초과한다. Redis / 구간 Lock은 고르지 않았다.

이 ADR만으로 처리량·p95가 개선됐다고 쓰지 않는다.

## 결과

```text
하는 것      T3-03에서 선택 시점 원자적 차감 (PostgreSQL)
하지 않는 것 Redis / Lua / 구간 Lock / Impression 예산 카운터
남은 한계    GET만 하고 Impression이 없어도 예산을 쓴다
             Player·Dashboard 확인은 Phase 3 완료 항목, T3-03이 아님
```
