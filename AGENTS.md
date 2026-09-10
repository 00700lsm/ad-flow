# AdFlow

에이전트는 이 저장소의 문서를 대화보다 우선한다.
사용자는 파일 경로를 적지 않는다. `다음` / `승인` / Task ID면 충분하다.

```text
HARNESS.md 사람용 하네스 설명
규칙     .cursor/rules/ad-flow.mdc
루프     .cursor/skills/ad-flow-vibe-coding/SKILL.md
철학     docs/Poc.md
요구     docs/REQUIREMENTS.md
구조     docs/DESIGN.md
순서     docs/ROADMAP.md
현재작업 docs/TASKS.md
거버넌스 .agent/config.yaml
산출물   .agent/artifacts/
```

Plan HITL 없이 프로덕션 코드를 쓰지 않는다.
한 요청에서 Task 하나만 진행한다. Phase 전체를 구현하지 않는다.
Task Summary가 끝나면 그 Task만 커밋한다. 푸시는 요청할 때만.
