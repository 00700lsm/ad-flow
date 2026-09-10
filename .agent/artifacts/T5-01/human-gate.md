# Human Gate

```text
Task: T5-01
상태: 후보 A 선택 (2026-09-10). ADR 008
```

## 현재 문제

같은 eventId Impression을 세 번 접수하면 Dashboard 노출이 3이 된다. 정산 집계가 키 하나당 한 번이 아니다.

## 현재 측정

```text
posted=3
uniqueEventIds=1
aggregated=3
워커 INSERT 이후 COUNT. 운영 RPS·동시 중복이 아님
```

## 원인 가설

ad_events.eventId에 unique가 없다. 워커는 매번 save한다. Dashboard는 행 수다.

## 후보

```text
A  감수. 같은 eventId도 행을 늘림
B  PostgreSQL UNIQUE(eventId) / 충돌 시 INSERT 무시
C  앱에서 조회 후 스킵 (동시 POST면 레이스)
```

## 장점 / 단점

```text
A  코드 없음. FR-12 미충족
B  이미 쓰는 DB. 동시 중복도 DB가 막음. 타입(Impression/Click) 같은 키인지는 스키마에 묶임
C  인프라 없음. T2-02와 같은 레이스가 남음
```

## 추천안

후보 B. 집계 정본이 PostgreSQL이고, 측정은 중복 INSERT다. Kafka 멱등은 브로커가 없다.

## 예상 Trade-off

B를 고르면 201 이후 워커가 두 번째 save에서 실패할 수 있다. 접수 201과 저장 성공이 더 어긋난다 (ADR 006).

개발자 선택 A. UNIQUE·멱등 없음. FR-12는 미충족.
