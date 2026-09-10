# Observation

```text
Task: T7-10
Date: 2026-09-10
```

브라우저 클릭 자동화는 이 세션에 없어 하지 못했다.
Dashboard가 쓰는 것과 같은 경로를 mem 프로필 port 8081에서 실행했다.

```text
POST /campaigns  sim-demo  budget=10 cap=0 스포츠 20-39 → id=1
POST /campaigns/1/creatives CARD → id=1
POST /simulations  concurrentUsers=2 clickRate=100 20대 100% 스포츠
POST /simulations/1/start  requestCount=2
```

결과:

```text
GET /dashboard/campaigns/1
  spentBudget=2 remainingBudget=8 impressions=2 clicks=2 ctr=1.0
GET /simulator.html  200
GET /index.html  Simulator 뒤에 노출
```

확인하지 않은 것: 실제 브라우저에서 Simulator 버튼을 누른 화면.
k6 / 연속 start 루프 없음.
