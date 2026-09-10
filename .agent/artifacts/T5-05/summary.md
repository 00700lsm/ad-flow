# Summary

```text
Task: T5-05
Phase: 5
```

## 결과

같은 eventId Impression 3회 → Dashboard 노출 1. 시연은 README curl. Player는 매번 새 키.

관찰: post 3×201, impressions=1. UNIQUE 로직은 T5-03 그대로.

## 검증

```text
Red: dashboard.html eventId 안내 없음
Green: ./gradlew test 통과
수동: mem 8081 curl + GET /dashboard/campaigns/1
```

## 문서

TASKS T5-05 DONE. README Current Status Phase 5. dashboard.html / index.html 안내.
