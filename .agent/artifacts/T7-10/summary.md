# Summary

```text
Task: T7-10
Phase: 7
```

## 결과

README에 simulator.html → start → Dashboard. index에 노출·클릭·예산. mem 8081에서 start requestCount=2, Dashboard impressions=2 clicks=2 spentBudget=2.

측정: indexSimulatorThenDashboard=1

k6·연속 루프·Phase 닫기 없음.

## 검증

```text
Red: index Simulator 뒤에 노출 없음
Green: ./gradlew test --tests SimulatorPageTest 통과
수동: mem 8081 POST start + GET /dashboard/campaigns/1
```

## 문서

TASKS T7-10 DONE. DESIGN 4절·9.5·12.7. README. FR-14 미충족.
