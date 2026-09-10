# Summary

```text
Task: T7-01
Phase: 7
```

## 결과

POST /simulations 가 없다. simulator.html이 없다. index에 Simulator 링크가 없다. k6 없음. 해법 없음.

측정: simulationsEndpoint=0 simulatorHtml=0 k6=0

## 검증

```text
Red: POST /simulations 201 · GET /simulator.html 200 기대 → 실패
Green: ./gradlew test --tests SimulatorGapTest 통과
       ./gradlew test 통과
```

## 문서

TASKS T7-01 DONE. DESIGN 4절·12.7에 공백 재현.

해법은 구현하지 않았다. Human Gate A/B/C/D는 다음 요청.
