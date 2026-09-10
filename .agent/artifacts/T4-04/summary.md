# Summary

```text
Task: T4-04
Phase: 4
```

## 결과

워커가 INSERT 커넥션을 붙잡으면 GET `/ads`가 같이 기다린다. POST는 빠르다. 풀 분리·Kafka 없음.

측정: workerHoldMs=400 getAdsWaitMs=431 postMs=3 pool=1

## 검증

```text
Red: GET wait >= 300 assert 실패
Green: ./gradlew test 통과
```

## 문서

TASKS T4-04 DONE. DESIGN 4절·12.4에 풀 공유 재현.

해법은 구현하지 않았다. Human Gate: `.agent/artifacts/T4-04/human-gate.md`
