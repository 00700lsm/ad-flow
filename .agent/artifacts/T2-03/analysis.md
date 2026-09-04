# Analysis

```text
Task: T2-03
Phase: 2
Date: 2026-09-04
```

## 요청

Human Gate에서 후보 A를 골랐다. 동일 사용자의 동시 GET /ads가 frequencyCap을 넘지 않게, 선택 시점에 카운터를 원자적으로 올린다.

## 근거 문서

```text
REQUIREMENTS  FR-09: 하루 N번. 동시 요청에서도 한도를 넘지 않는다
              NFR-06: Frequency Cap 초과 0 (이 Task 테스트로 고정할 동시 한도)
DESIGN        4절: 서빙 시 INCR 없음. Impression이 카운터. Race 가능
              12.2: GET/INCR Race. Redis Atomic / Lua는 개선 후보(이번 선택 아님)
ROADMAP       Phase 2: 재현 뒤 후보 비교. 완료는 FR-09 + Player 확인
TASKS         T2-02 DONE. Human Gate 보류 → 개발자가 A 선택
ADR           001: Task 하나
              002: 보류. 003: A + PostgreSQL, Redis 아님
```

문서 충돌:

```text
없음. DESIGN 12.2의 Redis/Lua는 후보이지 현재 구조가 아니다.
ADR 003이 저장소를 PostgreSQL로 못 박았으므로 Redis를 이 Task에 넣지 않는다.
Player 캡 이후 광고 변경은 Phase 완료 항목이다. T2-03 완료 조건이 아니다.
```

현재 코드(문서 4절과 일치):

```text
GET /ads readOnly COUNT(AdEvent IMPRESSION)
POST /events/impression 이 COUNT를 올린다
FrequencyCapRaceTest: 동시 GET+Impression → overflow=15
```

A 적용 시 바뀌는 계약:

```text
캡 카운터 = GET /ads의 원자적 +1
AdEvent IMPRESSION = Dashboard 집계. 캡이 아님
T2-01 API 테스트(Impression N건 후 제외)는 GET N회로 바꿔야 한다
```

## 제약

```text
이 Phase에서 해도 되는 것
  PostgreSQL 당일 카운터 행 + count < cap 일 때만 +1
  실패 시 다음 우선순위 캠페인
  동시 GET에서 선택 횟수 ≤ cap 을 테스트로 고정
  T2-01 순차 테스트를 새 카운터 계약에 맞게 수정

하면 안 되는 것
  Redis / Kafka / Lua / Distributed Lock (C가 아님)
  후보 B (Impression 조건부 INSERT)
  Budget / Simulator / SSE / k6 / 새 HTTP API
  Player UI 개편

영향 파일 후보
  AdServingService (readOnly 해제, 소비 후 선택)
  카운터 엔티티/리포지토리 (신규)
  AdSelector 또는 선택 루프
  FrequencyCapRaceTest 기대를 한도 준수로 변경
  FrequencyCapApiTest / AdSelectionTest 순차 계약
```

## 하지 않는 이유

Redis는 A의 저장소로 고르지 않았다. 성능 추측으로 넣지 않는다.
Impression에 한도를 걸면 B다. GET+이벤트를 한 락으로 묶으면 C다.
Player 확인은 동시 한도가 테스트로 고정된 다음 Task다.

## Exit

```text
Valid: yes
다음: plan.md
```
