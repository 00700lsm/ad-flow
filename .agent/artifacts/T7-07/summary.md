# Summary

```text
Task: T7-07
Phase: 7
```

## 결과

POST clickRate. start가 Impression 뒤 n * clickRate / 100 슬롯에 accept(CLICK). Dashboard 클릭이 그 횟수. 생략이면 0. stop 후 start는 0.

측정: clickRate100Clicks=2 clickRateOmittedClicks=0 stopBeforeStartClicks=0

30대 시드·k6 없음. simulator.html clickRate 없음.

## 검증

```text
Red: clickRate 100이면 clicks=2 기대 → 0
Green: ./gradlew test --tests SimulationApiTest --tests SimulatorPageTest 통과
       ./gradlew test 통과
```

## 문서

TASKS T7-07 DONE. DESIGN 4절·9.5·12.7. README. FR-14 미충족.
