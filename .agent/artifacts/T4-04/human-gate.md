# Human Gate

```text
Task: T4-04
상태: 후보 A 선택 (2026-09-10). T4-06 / ADR 007
```

## 현재 문제

POST는 INSERT를 안 기다리지만, 워커가 같은 DataSource 커넥션을 붙잡으면 GET `/ads`가 기다린다.

## 현재 측정

```text
workerHoldMs=400
getAdsWaitMs=431
postMs=3
pool=1
H2 풀=1 + 테스트에서 save 트랜잭션 hold. 운영 RPS·디스크 INSERT가 아님
```

## 원인 가설

워커 persist와 광고 선택이 한 풀을 쓴다. T4-02는 요청 스레드만 INSERT와 끊었다.

## 후보

```text
A  감수. 워커와 GET은 같은 DataSource 유지
B  이벤트 저장만 다른 DataSource / 풀
C  Kafka 등 프로세스 밖 적재 (ADR 006에서 데모 미선택. 새 측정 없이 안 고름)
```

## 장점 / 단점

```text
A  추가 인프라 없음. 풀이 차면 선택은 여전히 기다릴 수 있다
B  Serving 풀을 이벤트 INSERT와 나눈다. 앱은 하나. 커넥션 예산이 늘 수 있다
C  재처리에 가깝다. 이 측정만으로 도입하면 ROADMAP·ADR 006과 긴장
```

## 추천안

후보 A. 데모 트래픽에서 워커가 풀을 가득 채운 측정은 없다. B는 풀이 제품 문제가 될 때.

## 예상 Trade-off

B를 고르면 T4-01 결합의 남은 경로만 끊는다. FR-11 재처리는 그대로다.

개발자 선택 A. 같은 DataSource 유지. 풀 분리·Kafka 없음.
