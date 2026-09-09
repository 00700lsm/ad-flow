# Summary

```text
Task: T4-02
Phase: 4
```

## 결과

POST `/events/*`는 메모리 큐에 넣고 201을 준다. INSERT는 단일 JVM 워커. Kafka 없음. persistDelayMs=400일 때 postMs=2.

워커와 GET `/ads`는 같은 DataSource. 프로세스 종료 시 큐 유실 (ADR 005).

## 검증

```text
Red: ServingEventCouplingTest POST < 150ms 실패 확인
Green: ./gradlew test 통과
```

## 문서

ADR 005. TASKS T4-02 DONE. DESIGN 4절·12.4 JVM 큐.

다음 Task는 개발자 요청 시에만. FR-11 재처리는 하지 않았다.
