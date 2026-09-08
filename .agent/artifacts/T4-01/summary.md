# Summary

```text
Task: T4-01
Phase: 4
```

## 결과

이벤트 INSERT가 DB 커넥션을 붙잡고 있으면 GET `/ads`가 같이 기다린다. 테스트에서만 풀=1, 트랜잭션 안 sleep. 프로덕션 분리·Kafka 없음.

측정: eventHoldMs=400 getAdsWaitMs=409 pool=1

## 검증

```text
Red: ServingEventCouplingTest GET 대기 assert 실패 확인
Green: ./gradlew test 통과
```

## 문서

TASKS T4-01 DONE. Phase 4 IN PROGRESS. DESIGN 4절·12.4에 결합 재현.

해법은 구현하지 않았다. Human Gate: `.agent/artifacts/T4-01/human-gate.md`

다음 Task는 개발자 요청 시에만.
