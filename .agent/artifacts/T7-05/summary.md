# Summary

```text
Task: T7-05
Phase: 7
```

## 결과

GET /simulator.html. index·nav 링크. 폼이 concurrentUsers·20대/40대·스포츠/드라마로 POST /simulations · start · stop. requestCount 표시.

측정: simulatorHtml=1 indexLink=1 postsStartStop=1

Impression 루프·30대 시드·k6 없음.

## 검증

```text
Red: GET /simulator.html 200 기대 → 404
Green: ./gradlew test --tests SimulatorPageTest --tests SimulationApiTest 통과
       ./gradlew test 통과
브라우저: 이 세션에 브라우저 도구 없음. 페이지 존재는 테스트로 확인
```

## 문서

TASKS T7-05 DONE. DESIGN 4절·9.5·12.7. FR-14 미충족.
