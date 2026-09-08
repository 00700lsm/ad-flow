# Analysis

```text
Task: T4-02
Phase: 4
Date: 2026-09-08
```

## 요청

T4-01 다음. Serving과 이벤트 저장을 나누는 해법 Task로 보이지만, T4-01 Human Gate 후보가 아직 없다.

## 근거 문서

```text
REQUIREMENTS  FR-11 분리·대량·재처리. Phase 완료이지 한 Task가 아님
DESIGN        4절: 동기 INSERT. 12.4: Kafka는 목표. T4-01: 풀 결합만 재현
ROADMAP       기술은 결과가 필요성을 말할 때만
TASKS         T4-01 DONE. Kafka/Consumer는 Human Gate 전 금지
              다음: 개발자 요청 → 이번 요청
ADR           001: Task 하나. 해법 선택 없이 구현하지 않음
T4-01         human-gate.md 상태 pending
측정          eventHoldMs=400 getAdsWaitMs=409 pool=1
              Tomcat 기아·운영 RPS 없음
```

문서 충돌:

```text
해법 구현을 막는다.
T2-03은 Human Gate에서 A를 고른 뒤에만 Analysis가 Valid였다.
여기서 A/B/C 없이 Plan을 쓰면 에이전트가 기술을 고른다.
```

현재 코드:

```text
GET /ads 와 POST /events/* 같은 DataSource
분리 없음
```

## 제약

```text
이 요청에서 해도 되는 것
  Human Gate를 다시 보고 후보만 고르게 한다

하면 안 되는 것
  Kafka / JVM 큐 / 풀 튜닝 구현
  후보를 임의로 A로 정한 Plan
```

## 하지 않는 이유

측정은 “풀=1에서 결합이 있다”뿐이다. B는 이 숫자로 필요성이 안 나온다.
A도 유실·재처리 한계가 있어 사람이 골라야 한다.

## Exit

```text
Valid: no
다음: Human Gate 후보 선택. plan.md 없음
```
