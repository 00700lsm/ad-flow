# ADR 007 - Phase 4 데모는 접수 분리까지다

## 문제

FR-11은 분리·대량·재처리를 같이 적는다. T4-02는 POST와 INSERT를 같은 JVM에서 나눴다. 재처리와 풀 분리는 재현만 있고 해법이 없다. Phase 4를 닫을지 고른다.

## 현재 Context

```text
관련 Phase      4
관련 측정       T4-02 postMs (POST는 INSERT를 안 기다림)
                T4-04 workerHoldMs=400 getAdsWaitMs=431 pool=1
                T4-05 replayed=0
현재 코드       메모리 큐 + 워커 INSERT. 같은 DataSource. Kafka 없음
개발자 선택     T4-06 후보 A (2026-09-10)
                T4-04 Human Gate도 A. 같은 DataSource 유지
```

## 검토한 대안

```text
A  Phase 4 닫기. 접수 분리만 데모 충족. 재처리·풀 분리는 한계
B  Phase 4 유지. 워커와 GET DataSource를 나눔
C  Phase 4 유지. Outbox 또는 Kafka로 재처리 — ADR 006 번복
```

## 선택

대안 A.

데모 Phase 4 완료는 ADR 005의 접수 분리다. FR-11 전문(대량·재처리·Kafka)은 충족이 아니다. T4-04 풀=1 대기만으로 풀을 나누지 않는다. Kafka는 새 측정 없이 넣지 않는다.

측정 없이 “파이프라인이 개선됐다”고 쓰지 않는다. 코드 변경 없음.

## 결과

```text
하는 것      Phase 4를 데모 범위로 닫음. 한계를 문서에 고정
하지 않는 것 Kafka, Outbox, 별도 DataSource, FR-11 문장 삭제
남은 한계    워커 INSERT와 GET /ads 같은 DataSource
             큐·in-flight 유실, 재처리 없음 (ADR 006)
             대량 RPS 미측정
다음에 볼 때 풀이 제품 문제면 T4-04 후보 B
             유실이 제품 문제면 ADR 006 B 또는 C
             Phase 5(FR-12)는 개발자가 요청할 때
```
