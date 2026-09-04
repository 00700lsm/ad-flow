# Analysis

```text
Task: T2-02
Phase: 2
Date: 2026-09-03
```

## 요청

T2-01 다음. 동일 사용자의 동시 광고 요청에서 Frequency Cap이 한도를 넘는지 재현하고, 해법은 Human Gate 전에 고르지 않는다.

## 근거 문서

```text
REQUIREMENTS  FR-09: 하루 N번. 동시 요청에서도 한도를 넘지 않는다 (Phase 2 완료 조건)
              NFR-06: Frequency Cap 초과 0 (이후 측정 후보)
DESIGN        4절: 순차 GET. 서빙 시 INCR 없음. Impression이 카운터
              12.2: GET/INCR Race 가능. Redis Atomic / Lua는 개선 후보
              13.3: 동시 요청으로 한도 초과를 재현한 뒤 원자적 연산을 검토
ROADMAP       Phase 2: 단순 GET/INCR로 두고 Race를 확인한 뒤 후보 비교
TASKS         T2-01 DONE. 다음 체크: FR-09 동시 요청 한도 (Race 재현 후 Human Gate)
              Player 캡 이후 광고 변경 확인은 Phase 완료 항목이지 이 Task가 아님
ADR           001: 한 요청은 Task 하나. FR-09 전체를 한 Task로 구현하지 않음
```

문서 충돌:

```text
없음. FR-09 동시성 정확성은 Phase 완료 조건이다.
T2-02는 재현·측정·후보 보고까지다. Redis/Lock 도입은 Human Gate + ADR 이후 Task다.
```

현재 코드(문서 4절과 일치):

```text
GET /ads 는 당일 IMPRESSION COUNT를 읽고 cap 이상이면 캠페인을 뺀다 (readOnly)
POST /events/impression 이 COUNT를 올린다
두 요청 사이에 다른 스레드가 같은 COUNT를 보면 같은 캠페인을 고른다
Lock / 원자적 INCR / Redis 없음
```

Race 가설:

```text
cap=1, 동시에 GET /ads 여러 건
모두 count=0 < 1 → 같은 캠페인 반환
각자 Impression 기록
최종 당일 IMPRESSION > 1
```

## 제약

```text
이 Phase에서 해도 되는 것
  동시 GET /ads + Impression으로 cap 초과를 테스트로 재현
  초과 횟수를 측정해 artifacts에 남김
  Human Gate 형식으로 후보만 보고 (구현 없음)

하면 안 되는 것
  Redis / Kafka / Lua / Distributed Lock / 원자적 INCR
  Frequency Cap Race 해법 (NFR-06을 이 Task에서 달성)
  Budget / Simulator / SSE / k6 / Prometheus / Grafana
  DESIGN에 없는 API
  Player UI 개편 (Phase 완료 항목, 별 Task)

영향 파일 후보
  재현 테스트 (신규). 프로덕션 코드는 이 Task에서 바꾸지 않음
  (승인 후) TASKS 포인터, artifacts/T2-02 측정 메모
```

## 하지 않는 이유

초과가 측정되기 전에 Redis/Lock을 넣으면 ROADMAP의 「재현 → 측정 → 후보 → Human Gate」를 건너뛴다.
Player 확인은 순차 캡이 이미 API 테스트로 고정된 뒤의 화면 검증이다. Race 재현과 섞지 않는다.

## Exit

```text
Valid: yes
다음: plan.md
```
