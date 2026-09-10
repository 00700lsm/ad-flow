# ADR 009 - 같은 eventId는 UNIQUE로 한 번만 집계한다

## 문제

T5-01: posted=3 uniqueEventIds=1 aggregated=3.
T5-02 후보 C. ADR 008의 감수를 뒤집고 집계 증가량 1을 고른다.

## 현재 Context

```text
관련 Phase      5
관련 측정       T5-01 aggregated=3 (해법 전)
                T5-03 aggregated=1 (해법 후)
현재 코드       워커 INSERT. eventId unique 없음 (해법 전)
개발자 선택     T5-01 후보 B / T5-02 C (2026-09-10)
```

## 검토한 대안

```text
A  감수 (ADR 008)
B  PostgreSQL UNIQUE(eventId). 충돌 INSERT는 건너뜀
C  앱 exists 후 스킵
```

## 선택

대안 B.

집계 정본이 PostgreSQL이다. UNIQUE가 순차·동시 중복 INSERT를 막는다. exists만 하면 큐에 같은 키가 겹칠 때 레이스가 남는다. Kafka 멱등 Consumer는 브로커가 없다.

T5-03 측정 없이 “멱등이 됐다”고 쓰지 않는다. HTTP 201은 접수로 둔다 (ADR 006).

ADR 008은 이 결정으로 대체한다.

## 결과

```text
하는 것      ad_events.eventId UNIQUE. 워커는 충돌만 건너뜀
하지 않는 것 Kafka, Redis, exists-only, 201=INSERT
남은 한계    세 번 201이어도 집계는 1
             Impression과 Click이 같은 eventId면 한 행
```
