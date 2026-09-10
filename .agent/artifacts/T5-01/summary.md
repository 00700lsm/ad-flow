# Summary

```text
Task: T5-01
Phase: 5
```

## 결과

같은 eventId Impression 3회면 Dashboard 노출이 3이다. UNIQUE / Kafka 멱등 없음.

측정: posted=3 uniqueEventIds=1 aggregated=3

## 검증

```text
Red: impressions == 1 assert 실패
Green: ./gradlew test 통과
```

## 문서

TASKS T5-01 DONE. DESIGN 4절·12.5에 중복 집계 재현.

해법은 구현하지 않았다. Human Gate 후보 A. ADR 008.
