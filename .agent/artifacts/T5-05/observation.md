# Observation

```text
Task: T5-05
Date: 2026-09-10
```

브라우저 클릭 자동화는 이 세션에 없어 하지 못했다.
Dashboard가 쓰는 것과 같은 경로를 mem 프로필 port 8081에서 실행했다.

```text
POST /campaigns  중복시연  budget=50000 cap=0 스포츠 20-39 → id=1
POST /campaigns/1/creatives CARD → id=1
POST /events/impression  eventId=dup-demo-1  3회
```

결과:

```text
post 3회  HTTP 201
GET /dashboard/campaigns/1  impressions=1 clicks=0
GET /dashboard.html  같은 eventId는 노출·클릭 집계가 한 번이다
GET /  같은 eventId Impression을 세 번내면 Dashboard 노출은 1
```

확인하지 않은 것: 실제 브라우저에서 Dashboard 표를 눈으로 본 화면.
