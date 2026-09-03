# ADR 001 - Task 단위로만 진행한다

## 문제

Phase 1에서 "진행해줘"가 Phase 전체 구현 승인으로 해석됐다.
개발자가 Task Plan을 읽고 경계를 정할 틈이 없었다.

## 현재 Context

```text
Phase 1 코드는 있다
하네스에 Plan HITL이 있으나, Phase 요청이 모든 Task를 덮었다
```

## 검토한 대안

```text
1. Phase 단위로 한 번에 구현 (기존)
2. Task 하나 → Plan HITL → 구현 → STOP
```

## 선택

대안 2.

```text
한 대화 / 한 요청에서 다루는 단위는 docs/TASKS.md의 Task 하나다
"Phase N 진행"은 해당 Phase의 다음 TODO Task Analysis까지만 허용한다
다음 Task는 개발자가 다시 요청할 때만 시작한다
```

## 결과

```text
하는 것    Analysis → Plan STOP, 승인 후 그 Task만 Green
하지 않는 것  T1-01부터 T1-09까지 한 번에 구현
남은 한계  Phase 1은 이미 통으로 들어가 있다. 이 ADR은 그 이후부터 적용한다
```
