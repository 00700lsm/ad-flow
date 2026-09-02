# Experiments

실제 측정 결과를 남긴다. 구현 계획이나 기술 홍보 글이 아니다.

Experiment는 제품 MVP(Phase 1) 이후에 시작한다.

---

## 이름

해결 기술 이름을 넣지 않는다.

피한다:

```text
003-redis.md
004-kafka.md
005-lua.md
```

선호한다:

```text
001-ad-serving-db-lookup.md
002-budget-overspend.md
003-frequency-cap-race.md
004-duplicate-event-settlement.md
```

---

## 기본 구조

```text
Problem / Question
Hypothesis
Conditions
Baseline
Result
Analysis
Candidate
Decision
Remaining Limitation
```

결과가 가설과 달라도 그대로 기록한다.

Scenario를 결과에 맞춰 바꾸지 않는다.
비교 가능성을 깨는 변경은 Human Gate다.

---

## 템플릿

`docs/experiments/_template.md`
