# Agent Artifacts

대화 이력을 다음 페이즈로 넘기지 않는다.
검증된 산출물만 넘긴다. (`docs/Poc.md` Context Compaction)

```text
analysis.md  → 제약 / 영향 범위 / 하지 않을 것
plan.md      → TDD 케이스와 최소 구현 범위
HITL 승인    → 코드 작성 허가
```

현재 Task 작업 디렉터리:

```text
.agent/artifacts/<task-id>/
  analysis.md
  plan.md
```

`<task-id>`는 `docs/TASKS.md`의 ID다. 예: `T1-05`.

이전 Task 산출물을 현재 프롬프트에 붙이지 않는다.
필요한 결정만 ADR / DESIGN / 테스트로 이미 저장소에 있어야 한다.
