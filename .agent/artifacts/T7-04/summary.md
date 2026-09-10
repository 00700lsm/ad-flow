# Summary

```text
Task: T7-04
Phase: 7
```

## 결과

POST /simulations 가 ageShares·categories를 받는다. start는 20대→User 1, 40대→User 2, 스포츠→Content 1, 드라마→Content 2. 생략 시 User 1 · Content 1.

측정: dramaOnlySpent=2 sportsWhenDramaOnly=0 splitSports=1 splitDrama=1

화면·Impression 루프·30대 시드·k6 없음.

## 검증

```text
Red: 분포 응답·드라마만 spent=2 기대 → 실패
Green: ./gradlew test --tests SimulationApiTest --tests SimulatorGapTest 통과
       ./gradlew test 통과
```

## 문서

TASKS T7-04 DONE. DESIGN 4절·9.5·12.7. FR-14 미충족.
