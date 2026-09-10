# Summary

```text
Task: T4-05
Phase: 4
```

## 결과

워커가 take 이후 INSERT 전에 죽으면 재시작해도 Impression은 집계에 안 남는다. Kafka / Outbox 없음.

측정: accepted=1 afterCrashPersisted=0 replayed=0 persistDelayMs=400

## 검증

```text
Red: replay 후 impressions == 1 assert 실패
Green: ./gradlew test 통과
```

## 문서

TASKS T4-05 DONE. DESIGN 4절·12.4에 재처리 불가 재현.

해법은 구현하지 않았다. Human Gate 후보 A. ADR 006 유지.
