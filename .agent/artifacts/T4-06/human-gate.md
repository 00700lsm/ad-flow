# Human Gate

```text
Task: T4-06
상태: 후보 A 선택 (2026-09-10). ADR 007
```

## 현재 문제

FR-11 전문은 재처리·대량을 포함한다. 데모 코드는 POST 접수 분리까지다. T4-04 풀 공유 Gate는 미선택이었다.

## 현재 측정

```text
T4-02  POST는 INSERT를 안 기다림
T4-04  workerHoldMs=400 getAdsWaitMs=431 pool=1
T4-05  replayed=0
```

## 원인 가설

해법이 없어서가 아니라, 데모에서 Kafka·풀 분리를 고르지 않아서 Phase가 열린 채로 남았다.

## 후보

```text
A  Phase 4 닫기. T4-04 풀도 감수. 재처리는 ADR 006
B  Phase 4 유지. 다음은 풀 분리
C  Phase 4 유지. 다음은 재처리(Outbox/Kafka)
```

## 선택

후보 A. ADR 007. 코드 변경 없음. T4-04 Human Gate도 A.

Phase 5는 이 Task가 아니다.
