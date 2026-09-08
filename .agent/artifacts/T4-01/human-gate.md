# Human Gate

```text
Task: T4-01
상태: pending
```

## 현재 문제

이벤트 적재가 커넥션을 붙잡으면 광고 선택 API가 같은 풀에서 기다린다.

## 현재 측정

```text
테스트 H2, hikari maximum-pool-size=1
eventHoldMs=400
getAdsWaitMs=409
Tomcat 스레드 기아·운영 RPS는 측정하지 않음
```

## 원인 가설

GET `/ads`와 POST `/events/*`가 같은 DataSource를 쓴다. 느린 INSERT가 커넥션을 반환하지 않으면 선택이 대기한다.

## 후보

```text
A  같은 JVM에서 이벤트 API는 접수만, 저장은 메모리 큐+워커 (Kafka 없음)
B  Kafka + Consumer
C  동기 유지. 풀·타임아웃만 키움
```

## 장점 / 단점

```text
A  의존 적음. 프로세스 죽으면 큐 유실. FR-11 재처리 약함
B  FR-11 재처리에 가깝다. 인프라·운영 비용. 측정 없이 도입이면 ROADMAP 위반
C  결합은 남음. 느린 INSERT가 풀을 채우면 같은 증상
```

## 추천안

지금은 고르지 않는다. T4-01은 재현까지다. 운영 부하 숫자가 없다.

## 예상 Trade-off

A를 골라도 멱등(FR-12)은 다음 Phase다. B는 이 숫자만으로 필요성이 증명되지 않는다.

STOP. 후보 선택 전에 분리 코드를 넣지 않는다.
