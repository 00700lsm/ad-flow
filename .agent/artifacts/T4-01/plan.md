# Plan

```text
Task: T4-01
Phase: 4
HITL: approved
```

## 완료 조건

```text
이벤트 기록이 DB 커넥션을 붙잡고 있으면 GET /ads 가 같이 기다린다
그 대기를 테스트로 재현하고 시간을 남긴다
Kafka / 비동기 분리 / Consumer 재처리는 포함하지 않는다
Human Gate 전에 Serving과 이벤트 저장을 나누지 않는다
```

## Red Tests

실패해야 하는 테스트 목록.

```text
1. ServingEventCouplingTest.getAdsWaitsWhileEventInsertHoldsConnection
   Hikari maximum-pool-size = 1 (테스트 프로퍼티)
   POST /events/impression 경로가 커넥션을 연 채로 지연될 때
   다른 스레드의 GET /ads 가 그 지연에 가까운 시간을 기다린다
   (예: 이벤트 측 sleep ≥ 400ms 이면 GET elapsed ≥ 300ms)
   구현 전: 지연을 넣는 테스트 장치가 없으면 GET은 빠르고 assert 실패
```

가짜 테스트: 같은 클래스에 두 API가 있다고만 assert. 대기가 없으면 결합 재현이 아니다.
k6 / 실서버 Tomcat 기아는 이 Task가 아니다.

## Green 최소 구현

```text
ServingEventCouplingTest 만
  @SpringBootTest + MockMvc + 스레드
  테스트에서 AdEventService를 감싸 트랜잭션 안에 sleep (프로덕션 코드에 delay 설정 없음)
  풀 크기 1
  GET /ads elapsed 를 로그하고
  .agent/artifacts/T4-01/measurement.txt 한 줄
    eventHoldMs= GET waitMs=

프로덕션 AdServingService / AdEventService 동작 변경 없음
```

## 검증 명령

```text
./gradlew test --tests ServingEventCouplingTest
./gradlew test
린트: ./gradlew compileJava compileTestJava
수동 Demo 없음
```

## 하지 않는 것

```text
Kafka, Redis, @Async, 아웃박스, 별도 워커
Frequency Cap / Budget SQL 변경
새 HTTP 경로
k6 / Prometheus / SSE
이벤트 멱등 (FR-12)
```

## HITL

구현 전에 개발자 승인. 승인 전에는 프로덕션 코드와 테스트 변경을 작성하지 않는다.

승인 문장 예: `승인` / `T4-01 승인`

Trade-off: H2 풀=1 + 테스트 sleep 은 “적재가 느리면 선택이 기다린다”는 결합만 보인다. 운영 RPS·Tomcat 기아·진짜 느린 디스크 INSERT는 이 숫자가 아니다. 분리는 재현 다음 Human Gate다.

재현이 되면 응답에 후보만 적고 구현하지 않는다.

```text
후보 A  이벤트 API는 접수만, 저장은 같은 JVM 큐 (Kafka 없음)
후보 B  Kafka + Consumer
후보 C  연결 풀·타임아웃만 늘리고 동기 유지
```
