# Analysis

```text
Task: T2-01
Phase: 2
Date: 2026-09-03
```

## 요청

Phase 2를 연다. 첫 작업은 동일 사용자가 같은 광고를 하루 N번 넘게 보는 문제를, 순차 요청에서 Frequency Cap으로 막는 것이다.

## 근거 문서

```text
REQUIREMENTS  FR-09 (하루 N번). 동시성 정확성은 Phase 완료 조건이지 T2-01 조건이 아니다
DESIGN        4절: Frequency Cap 필터는 아직 코드에 없음
              12.2: 초기 GET frequency / if < cap / INCR. Redis·Lua는 개선 후보
ROADMAP       Phase 2: 단순 GET/INCR로 두고 Race는 확인한 뒤 후보 비교
TASKS         Phase 1 DONE. 다음: Phase 2 열면 T2-01 Analysis만
ADR           001: 한 요청은 Task 하나. Phase 진행 ≠ Phase 전체 구현
```

문서 충돌:

```text
없음. FR-09 전체(동시 요청 한도)는 Phase 2 완료 조건이다.
T2-01은 그중 순차 경로만 다룬다. Redis 도입은 DESIGN 7절 목표이며 현재 구조가 아니다.
```

현재 코드(문서 4절과 일치):

```text
frequencyCap은 Campaign 필드·콘솔 입력으로만 존재한다
GET /ads는 활성/기간/연령/장르/Priority만 본다
Impression은 PostgreSQL AdEvent로 동기 저장한다
```

## 제약

```text
이 Phase에서 해도 되는 것
  동일 userId + campaignId의 당일 IMPRESSION 건수 GET
  cap 이상이면 해당 캠페인을 선택에서 제외
  Impression 저장이 INCR 역할을 한다 (별도 Redis 카운터 없음)
  순차 GET /ads 테스트로 고정
  캡에 걸린 뒤 다른 후보가 있으면 그 광고를 고른다

하면 안 되는 것
  Redis / Kafka / Lua / Distributed Lock
  Frequency Cap Race 해법 (NFR-06 동시성 0)
  Budget 차감 / BUDGET_EXHAUSTED
  Simulator / SSE / k6 / Prometheus / Grafana
  Mock Ad Exchange / Kubernetes / AWS / MongoDB
  운영 AI
  DESIGN에 없는 API

영향 파일 후보
  AdSelector, AdServingService, AdEventRepository
  AdSelectionTest, Ad 서빙/이벤트 API 테스트
  (승인 후) TASKS / DESIGN 4절·선택 규칙 4번
```

## 하지 않는 이유

Race를 코드 단계에서 추측으로 막지 않는다. ROADMAP이 GET/INCR 뒤에 재현하라고 한다.
Redis Atomic / Lua는 Human Gate 대상이다. T2-01에서 고르지 않는다.
Player 화면 문구 추가는 순차 API가 통과한 뒤에 필요하면 다음 Task다. 이 Task의 완료 기준은 테스트로 고정된 선택 규칙이다.

## Exit

```text
Valid: yes
다음: plan.md
```
