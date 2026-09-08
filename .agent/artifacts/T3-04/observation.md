# Observation

```text
Task: T3-04
Date: 2026-09-08
```

브라우저 클릭은 이 세션에 자동화 도구가 없어 하지 못했다.
Player·Dashboard가 쓰는 것과 같은 경로를 현재 코드(mem, port 8081)에서 실행했다.

```text
Console와 동일 POST
  아이폰 예산시연  budget=1 priority=20 cap=0  스포츠 20-39
  나이키 예산시연  budget=50000 priority=10 cap=0  스포츠 20-39

Player와 동일
  GET /ads?userId=1&contentId=1  후 Impression
  두 번 반복
```

결과:

```text
1번째  아이폰 예산시연
2번째  나이키 예산시연
dashboard.html  사용 예산 / 잔여 예산 / 상태 / c.spentBudget
GET /dashboard/campaigns/1
  spentBudget=1 remainingBudget=0 status=BUDGET_EXHAUSTED impressions=1
GET /dashboard/campaigns/2
  spentBudget=1 remainingBudget=49999 status=ACTIVE
```

확인하지 않은 것: 실제 브라우저에서 Dashboard 표를 눈으로 본 화면.
