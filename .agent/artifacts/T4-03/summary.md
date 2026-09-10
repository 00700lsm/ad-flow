# Summary

```text
Task: T4-03
Phase: 4
```

## 결과

POST 201 직후 Dashboard 노출은 0이다. 워커가 살아 있으면 이후 1. 프로덕션 저장소·Kafka 변경 없음.

측정: accepted=1 persistedImmediately=0 persistedAfterWait=1 persistDelayMs=400

## 검증

```text
Red: 즉시 impressions=1 assert 실패
Green: ./gradlew test 통과
```

## 문서

TASKS T4-03 DONE. DESIGN 12.4에 집계 공백.

해법은 구현하지 않았다. Human Gate: `.agent/artifacts/T4-03/human-gate.md`
