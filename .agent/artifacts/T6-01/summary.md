# Summary

```text
Task: T6-01
Phase: 6
```

## 결과

GET /dashboard에 Impression/sec · Click/sec · Lag 필드가 없다. dashboard.html은 3초 폴링이고 SSE가 없다. 해법 없음.

측정: pollMs=3000 sse=0 impressionsPerSecField=0

## 검증

```text
Red: impressionsPerSec / EventSource 기대 → 실패
Green: ./gradlew test 통과
```

## 문서

TASKS T6-01 DONE. DESIGN 4절·12.6에 공백 재현.

해법은 구현하지 않았다. Human Gate 후보 A. ADR 011.
