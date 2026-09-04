# Human Gate

```text
Task: T2-02
상태: A 선택 (구현은 T2-03 Plan HITL)
Date: 2026-09-04
```

재현은 끝났다. 개발자가 후보 A를 골랐다.

```text
측정  requests=16 selected=16 impressions=16 cap=1 overflow=15
후보 A  선택 시점 원자적 INCR — 선택됨. 저장소 PostgreSQL (ADR 003)
후보 B  Impression 조건부 기록 — 탈락
후보 C  DB lock 직렬화 — 탈락
결정  A. Redis/Lua는 고르지 않음
```

T2-03 Plan 승인 전에 Redis / Lock / Lua / 선택 시점 INCR을 구현하지 않는다.
