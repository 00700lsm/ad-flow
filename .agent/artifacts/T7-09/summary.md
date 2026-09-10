# Summary

```text
Task: T7-09
Phase: 7
```

## 결과

simulator.html에 30대 % · clickRate. POST body에 ageShares 30대와 clickRate. 20대·40대·장르·start/stop 유지.

측정: thirtiesShare=1 clickRate=1

## 검증

```text
Red: HTML에 30대·clickRate 기대 → 없음
Green: ./gradlew test --tests SimulatorPageTest --tests SimulationApiTest 통과
       ./gradlew test 통과
브라우저: 이 세션에 브라우저 도구 없음. GET /simulator.html 테스트로 확인
```

## 문서

TASKS T7-09 DONE. DESIGN 4절·9.5·12.7. README. FR-14 미충족.
