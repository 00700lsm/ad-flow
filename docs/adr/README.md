# ADR

Architecture Decision Record.

의미가 바뀌는 결정만 남긴다. 일상적인 구현 선택은 남기지 않는다.

---

## 작성 시점

```text
문제 재현 / 측정
  ↓
분석
  ↓
Human Gate
  ↓
Decision
  ↓
ADR
  ↓
구현
```

결정 코드보다 먼저, 또는 같은 작업 단위에서 작성한다.
프로젝트 후반에 몰아서 복원하지 않는다.

---

## 대상 예

```text
Redis 도입 여부
Kafka 도입 여부
Budget / Frequency Cap 동시성 제어 방식
이벤트 저장을 Serving API와 분리할지 → `005-event-async-in-jvm-queue.md`
데모에서 큐 유실을 감수할지 → `006-accept-in-memory-event-queue-loss.md`
Phase 4 데모 완료 범위 → `007-phase4-demo-accept-split-only.md`
데모에서 eventId 중복 집계를 감수할지 → `008-accept-duplicate-event-id-aggregation.md` (ADR 009로 대체)
같은 eventId 집계 UNIQUE → `009-event-id-unique-aggregation.md`
Phase 5 데모 완료 범위 → `010-phase5-demo-event-id-unique.md`
데모에서 Dashboard 폴링만 둘지 → `011-accept-dashboard-poll-only.md`
Phase 6 데모 완료 범위 → `012-phase6-demo-poll-only.md`
Traffic Simulator를 제품 HTTP로 둘지 → `013-product-simulations-http.md`
Phase 7 데모 완료 범위 → `015-phase7-demo-http-simulator.md`
Task Summary 후 커밋할지 → `014-commit-after-task-summary.md`
Source of Truth와 Realtime Counter를 나눌지
프로젝트 Scope 변경
Phase 완료 조건을 바꿀지
한 요청에서 다룰 Task 범위
```

---

## 템플릿

`docs/adr/_template.md`
