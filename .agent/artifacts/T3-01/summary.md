# Summary

```text
Task: T3-01
Phase: 3
```

## 결과

순차 Impression에서 예산을 채운다. `spentBudget >= budget`이면 GET /ads가 그 캠페인을 고르지 않고, 도달 시 `BUDGET_EXHAUSTED`다. Click은 예산을 올리지 않는다.

동시 요청 Overspending 0은 고정하지 않았다. Redis / Lock 없음.

## 검증

```text
Red: AdSelectionTest 2건 + BudgetApiTest 2건 실패 확인
Green: ./gradlew test 통과
Lint: ./gradlew compileJava compileTestJava 통과
```

## 문서

TASKS T3-01 DONE. DESIGN 4절·12.3을 순차 차감과 맞춤.

다음 Task는 개발자 요청 시에만.
