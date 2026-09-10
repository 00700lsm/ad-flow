# Summary

```text
Task: T7-08
Phase: 7
```

## 결과

User 3 age 35. start 30대 슬롯은 User 3. 20대·40대는 1·2. 폼 변경 없음.

측정: thirtiesSpent=2 twentiesWhenThirtiesOnly=0

## 검증

```text
Red: 30대 100% 스포츠면 30~39 캠페인 spent=2 기대 → User 1이라 20~29가 오름
Green: ./gradlew test --tests SimulationApiTest --tests SimulatorPageTest 통과
       ./gradlew test 통과
```

## 문서

TASKS T7-08 DONE. DESIGN 4절·9.5·12.7. README. FR-14 미충족.
