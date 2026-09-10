# Plan

```text
Task: T4-04
Phase: 4
HITL: approved
```

## 완료 조건

```text
워커가 ad_events INSERT 커넥션을 붙잡고 있으면 GET /ads 가 같이 기다린다
대기를 테스트로 재현하고 시간을 남긴다
Kafka / Outbox / 별도 DataSource / Consumer 재처리는 포함하지 않는다
Human Gate 전에 풀 결합을 고치지 않는다
```

## Red Tests

실패해야 하는 테스트 목록.

```text
1. ServingEventWorkerPoolCouplingTest.getAdsWaitsWhileWorkerInsertHoldsConnection
   Hikari maximum-pool-size = 1
   POST /events/impression 은 201을 빨리 준다
   워커 events.save 가 커넥션을 연 채로 ≥ 400ms 지연
   그 동안 다른 스레드 GET /ads 의 elapsed ≥ 300ms
   구현 전: 워커 save가 커넥션을 안 붙잡으면 GET은 빠르고 assert 실패
```

가짜 테스트:
- persistDelayMs(save 전 sleep)만 주고 GET이 빠르다고 결합이 없다고 쓰기
- POST가 빠르다고만 assert (그건 T4-02)
- 같은 DataSource라는 코드 존재만 assert

## Green 최소 구현

```text
ServingEventWorkerPoolCouplingTest 만
  @SpringBootTest + MockMvc + 풀=1
  워커 save 경로에서만 트랜잭션/커넥션을 붙잡은 채 sleep
  (프로덕션 drain()의 persistDelayMs 의미는 바꾸지 않음)
  GET /ads elapsed 를
  .agent/artifacts/T4-04/measurement.txt 한 줄
    workerHoldMs= getAdsWaitMs= postMs= pool=1

프로덕션 AdServingService / 큐 계약 변경 없음
```

## 검증 명령

```text
./gradlew test --tests ServingEventWorkerPoolCouplingTest
./gradlew test
린트: ./gradlew compileJava compileTestJava
수동 Demo 없음
```

## 하지 않는 것

```text
Kafka, Redis, Outbox, 재처리 Consumer
이벤트용 DataSource 분리
풀 크기 변경을 해법으로 적용
Frequency Cap / Budget SQL
새 HTTP 경로
201 = INSERT 완료로 되돌리기
FR-12 멱등
```

## HITL

구현 전에 개발자 승인. 승인 전에는 프로덕션 코드와 테스트 변경을 작성하지 않는다.

승인 문장 예: `승인` / `T4-04 승인`

Trade-off: H2 풀=1 + 워커 save hold는 “적재가 커넥션을 쓰면 선택이 기다린다”만 보인다. 운영 RPS·디스크 INSERT·Tomcat 기아는 이 숫자가 아니다.

재현이 되면 응답에 후보만 적고 구현하지 않는다.

```text
후보 A  감수. 워커와 GET은 같은 DataSource 유지
후보 B  이벤트 저장만 다른 DataSource / 풀
후보 C  Kafka 등 프로세스 밖 적재 (ADR 006에서 데모 미선택. 새 측정 없이 안 고름)
```
