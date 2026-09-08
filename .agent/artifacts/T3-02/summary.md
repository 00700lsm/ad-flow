# Summary

```text
Task: T3-02
Phase: 3
```

## 결과

동시 GET `/ads` 16건이 budget=1 캠페인을 모두 고르고, Impression 뒤 spentBudget=16이다. overflow=15. 해법은 넣지 않았다.

## 검증

```text
./gradlew test --tests BudgetRaceTest 통과 (초과 재현)
./gradlew test 통과
```

측정: `.agent/artifacts/T3-02/measurement.txt`

## 문서

TASKS T3-02 DONE. DESIGN 4절·12.3에 해법 전 숫자를 적음.

다음 Task는 개발자 요청 시에만. Human Gate 승인 없이 Lock/Redis를 넣지 않는다.
