---
name: ad-flow-vibe-coding
description: >-
  AdFlow 바이브 코딩 상태 그래프. Campaign/Creative/Ad Serving/Event/Dashboard
  구현, 테스트 추가, 리팩터링, "만들어줘", "추가해줘", "구현해줘"에 사용한다.
  Analysis·Plan HITL·Red-Green-Refactor를 건너뛰지 않고, Redis/Kafka를
  Phase 1에 선제 도입하지 않는다.
---

# AdFlow 바이브코딩

코드를 쓰기 전에 이 스킬을 따른다. 철학은 `docs/Poc.md`, 제약은 `.cursor/rules/ad-flow.mdc`다.
문서와 이 스킬이 충돌하면 **문서가 우선**이다.

## 목적

완성된 애드테크 스택을 한 번에 만드는 것이 아니다.

- Phase 1: 만든 광고가 Player에 나오고 Dashboard에 이벤트가 남는다
- 그다음: 과다 노출, 예산 초과, 이벤트 결합, 중복 정산을 **재현하고** 고친다

## 하드 제약

요청에 없어도 Phase 1에 넣지 않는다.

```text
Redis, Kafka, Lua, Distributed Lock
Simulator, SSE, k6, Prometheus, Grafana
Mock Ad Exchange, Kubernetes, AWS, MongoDB
운영 AI
Frequency Cap / Budget 동시성 해법
```

"성능상 필요할 것 같아서"는 이유가 되지 않는다.

## 루프

단계를 건너뛰지 않는다. 산출물은 `.agent/artifacts/<task-id>/`에 둔다.
템플릿: `.agent/artifacts/_analysis.template.md`, `_plan.template.md`.

```text
1. Analysis (읽기 전용)
   REQUIREMENTS / DESIGN / ROADMAP / TASKS만 읽고 analysis.md를 쓴다.
   파일 생성·패치·테스트 실행은 하지 않는다.
   Valid가 no면 구현하지 않고 문서 충돌을 보고한다.

2. Plan + HITL
   plan.md에 Red 테스트 목록과 최소 구현 범위를 쓴다.
   마지막에 STOP. 승인 문장 없이 프로덕션 코드를 쓰지 않는다.

3A. Red
   테스트만 추가하고 실행한다. 반드시 실패해야 한다.
   통과하면 가짜 테스트로 보고하고 Red를 다시 한다.

3B. Green
   실패를 없애는 최소 코드만 넣는다.
   없는 파일은 create, 있는 파일은 patch.
   테스트가 통과할 때까지. 재시도는 config.yaml max_retry_limit.
   초과 시 STOP, 변경 롤백 후보를 보고한다.

3C. Lint
   린트/컴파일 에러만 고친다. 동작 변경 금지.

3D. Refactor
   행위 변경 없이. 테스트가 깨지면 되돌린다.

4. Summary
   TASKS 상태, 필요 시 DESIGN/README를 코드에 맞춘다.
   커밋은 사용자가 요청할 때만.
```

대화 전체를 다음 단계에 복사하지 않는다. `analysis.md` / `plan.md` / 테스트 결과만 사용한다.

## 구현할 때

1. 요청이 제외 기능·선제 최적화면 구현하지 않고 이유를 말한다.
2. API·도메인 필드는 DESIGN / REQUIREMENTS에 있는 것만 쓴다.
3. 미래 확장용 인터페이스를 만들지 않는다.
4. 한국어로 짧게 왜 넣었는지 / 왜 빼는지 말한다.

## 성능·동시성

추측으로 Redis, Lock, Kafka를 넣지 않는다.
Human Gate 형식은 프로젝트 룰을 따른다.
Experiment 이름은 기술명이 아니라 문제명이다.
