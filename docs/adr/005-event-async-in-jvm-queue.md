# ADR 005 - 이벤트는 같은 JVM 큐로 Serving 요청과 저장을 나눈다

## 문제

이벤트 INSERT가 DB 커넥션을 붙잡으면 GET `/ads`가 같은 풀에서 기다린다.
T4-01 측정: eventHoldMs=400 getAdsWaitMs=409 pool=1.
Human Gate에서 분리 방식을 고른다.

## 현재 Context

```text
관련 Phase      4
관련 Experiment T4-01 measurement.txt
현재 코드       POST /events/* 가 같은 트랜잭션에서 INSERT 후 반환
측정된 사실     요청 스레드가 INSERT 커넥션을 붙잡으면 선택이 대기한다
개발자 선택     후보 A (T4-02)
```

## 검토한 대안

```text
A  같은 JVM. API는 접수만, 저장은 메모리 큐+워커
B  Kafka + Consumer
C  동기 유지. 풀·타임아웃만 키움
```

## 선택

대안 A.

`POST /events/impression` · `POST /events/click`은 큐에 넣고 바로 201을 준다. INSERT는 단일 워커가 한다. Kafka 없음.

고른 이유: T4-01이 증명한 것은 요청 경로가 저장을 기다린다는 결합이다. 이미 쓰는 JVM으로 그 결합만 끊는다. B는 운영 RPS·재처리 측정이 없다. C는 결합을 남긴다.

이 ADR만으로 처리량·p95가 개선됐다고 쓰지 않는다.

## 결과

```text
하는 것      접수와 INSERT를 같은 프로세스에서 다른 스레드로 나눈다
하지 않는 것 Kafka / Redis / 재처리 / 멱등
남은 한계    프로세스 종료 시 큐 유실
             워커 INSERT는 GET /ads 와 같은 DataSource라 풀이 차면 선택은 여전히 기다릴 수 있다
```
