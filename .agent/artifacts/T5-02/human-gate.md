# Human Gate

```text
Task: T5-02
상태: 후보 C 선택 (2026-09-10)
```

## 현재 문제

T5-01에서 같은 eventId 3회면 집계가 3이다. Phase 5를 닫을지, 멱등으로 갈지 고른다.

## 현재 측정

```text
posted=3 uniqueEventIds=1 aggregated=3
ADR 008: 데모에서 감수 (T5-01 A)
```

## 후보

```text
A  Phase 5 닫기. FR-12는 ADR 008
B  Phase 5 유지. 동시 중복 재현
C  Phase 5 유지. UNIQUE — ADR 008을 다시 연다
```

## 선택

후보 C. Phase 5는 IN PROGRESS. UNIQUE는 다음 Task. 이 Task에서 코드 없음.
ADR 008은 아직 대체하지 않는다. 다음 Task Plan HITL 후에 번복한다.
