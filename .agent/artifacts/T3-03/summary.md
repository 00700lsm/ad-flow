# Summary

```text
Task: T3-03
Phase: 3
```

## 결과

동시 GET `/ads`에서 `spentBudget`이 `budget`을 넘지 않는다. 차감은 PostgreSQL 조건부 UPDATE다. Impression은 예산을 올리지 않는다. Redis / Lock 없음.

해법 후 테스트: requests=16 selected=1 budget=1 spent=1.

## 검증

```text
Red: BudgetRaceTest·BudgetApiTest 3건 실패 확인
Green: ./gradlew test 통과
```

ADR 004. 측정: `.agent/artifacts/T3-03/measurement.txt`

## 문서

TASKS T3-03 DONE. DESIGN 4절·12.3을 GET 시점 차감과 맞춤.

GET만 해도 예산이 줄어든다. Player·Dashboard 확인은 하지 않았다.
다음 Task는 개발자 요청 시에만.
