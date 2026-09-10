# Human Gate

```text
Task: T4-03
상태: A 선택 (데모, ADR 006)
```

## 현재 문제

접수된 이벤트가 INSERT 전까지 Dashboard에 없다. 그 동안 프로세스가 끝나면 집계에 안 남는다.

## 현재 측정

```text
accepted=1
persistedImmediately=0
persistedAfterWait=1
persistDelayMs=400
프로세스 kill은 테스트하지 않음. 같은 창이 유실 후보
```

## 원인 가설

201은 메모리 큐만 보장한다. 디스크는 워커 save 이후다.

## 후보

```text
A  유실 감수. JVM 큐 유지 (ADR 005)
B  같은 PostgreSQL Outbox / 먼저 INSERT 후 201
C  Kafka 등 브로커
```

## 장점 / 단점

```text
A  추가 인프라 없음. FR-11 재처리 미충족
B  기존 DB. POST가 다시 INSERT를 기다릴 수 있음 (T4-01과 긴장)
C  재처리에 가깝다. T4-01과 같이 운영 측정 없이 도입이면 ROADMAP 위반
```

## 추천안

후보 A. 개발자 선택 2026-09-10. 데모이므로 유실 감수. 설명은 ADR 006.

## 예상 Trade-off

B를 고르면 T4-02의 “201이 INSERT를 안 기다림”이 약해질 수 있다.

STOP. 후보 선택 전에 내구성 코드를 넣지 않는다.
