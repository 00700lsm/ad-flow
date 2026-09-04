# Human Gate

```text
Task: T2-02
상태: 보류
Date: 2026-09-04
```

재현은 끝났다. 해법(A/B/C, 저장소)은 고르지 않는다.

```text
측정  requests=16 selected=16 impressions=16 cap=1 overflow=15
후보 A  선택 시점 원자적 INCR (저장소 미정)
후보 B  Impression 조건부 기록
후보 C  DB lock 직렬화
결정  없음. 보류
```

보류인 동안 Redis / Lock / Lua / 선택 시점 INCR을 구현하지 않는다.
다음 Task는 개발자가 Human Gate를 재개할 때만 연다.
