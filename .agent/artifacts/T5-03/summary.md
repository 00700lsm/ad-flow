# Summary

```text
Task: T5-03
Phase: 5
```

## 결과

같은 eventId Impression 3회면 Dashboard 노출은 1이다. UNIQUE(eventId). 워커는 충돌 INSERT를 건너뛴다. Kafka 없음.

측정: posted=3 uniqueEventIds=1 aggregated=1

## 검증

```text
Red: impressions == 1, 당시 aggregated=3 으로 실패
Green: ./gradlew test 통과
```

## 문서

TASKS T5-03 DONE. FR-12 테스트 칸. DESIGN 4절·12.5. ADR 009가 008을 대체.
