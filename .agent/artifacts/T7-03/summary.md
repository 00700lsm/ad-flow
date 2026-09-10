# Summary

```text
Task: T7-03
Phase: 7
```

## 결과

POST /simulations · start · stop. start는 User 1 · Content 1로 concurrentUsers번 순차 선택. stop 후 start는 requestCount=0.

측정: simulationsEndpoint=1 startRequestCount=2 stopBeforeStartRequestCount=0

화면·분포·Impression 루프·k6 없음.

## 검증

```text
Red: POST /simulations 201 기대 → 실패
Green: ./gradlew test --tests SimulationApiTest --tests SimulatorGapTest 통과
       ./gradlew test 통과
```

## 문서

TASKS T7-03 DONE. DESIGN 4절·9.5·12.7. FR-14 미충족.
