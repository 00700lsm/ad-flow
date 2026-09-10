# ADR 008 - 데모에서는 같은 eventId 중복 집계를 감수한다

## 문제

T5-01 측정: posted=3 uniqueEventIds=1 aggregated=3.
같은 Impression `eventId`를 세 번내면 Dashboard 노출이 3이다.
Human Gate에서 멱등을 고른다.

## 현재 Context

```text
관련 Phase      5
관련 측정       T5-01 measurement.txt
현재 코드       워커가 eventId 검사 없이 INSERT. Dashboard는 COUNT
개발자 선택     후보 A. 데모이므로 중복 집계 감수 (2026-09-10)
```

## 검토한 대안

```text
A  감수. 같은 eventId도 행을 늘림
B  PostgreSQL UNIQUE(eventId) / 충돌 시 INSERT 무시
C  앱에서 조회 후 스킵
```

## 선택

대안 A.

데모 범위에서는 접수한 만큼 행이 는다. UNIQUE·앱 단 스킵은 넣지 않는다. FR-12의 “집계 증가량 1”은 이 결정으로 아직 충족하지 않는다.

측정 없이 “멱등이 됐다”고 쓰지 않는다. 코드 변경 없음.

## 결과

```text
하는 것      한계를 문서로 고정. 중복 INSERT 유지
하지 않는 것 UNIQUE, upsert, Kafka 멱등 Consumer
남은 한계    같은 eventId → 집계가 횟수만큼 증가
다음에 볼 때 중복이 제품 문제면 B 또는 C를 다시 Human Gate
```
