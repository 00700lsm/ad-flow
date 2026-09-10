# Summary

```text
Task: T7-06
Phase: 7
```

## 결과

start가 선택마다 기존 AdEventService.accept(IMPRESSION). Dashboard 노출이 requestCount와 같다. stop 후 start는 0.

측정: startImpressions=2 stopBeforeStartImpressions=0

Click 확률·30대 시드·k6 없음.

## 검증

```text
Red: start 후 impressions=2 기대 → 0
Green: ./gradlew test --tests SimulationApiTest --tests SimulatorPageTest 통과
       ./gradlew test 통과
```

## 문서

TASKS T7-06 DONE. DESIGN 4절·9.5·12.7. FR-14 미충족.
