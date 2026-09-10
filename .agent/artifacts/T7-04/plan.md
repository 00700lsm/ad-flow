# Plan

```text
Task: T7-04
Phase: 7
HITL: pending
```

## 완료 조건

```text
POST /simulations 가 연령 비율·장르를 받는다
start가 그 분포로 샘플 User/Content를 골라 GET /ads와 같은 선택을 한다
비율·장르를 안 내면 T7-03과 같이 User 1 · Content 1
simulator.html / Impression 루프 / k6 / Kafka는 포함하지 않는다
```

## Red Tests

실패해야 하는 테스트 목록.

```text
1. SimulationApiTest.createStoresDistribution
   POST { concurrentUsers: 2, ageShares: { "20대": 50, "40대": 50 },
          categories: ["스포츠", "드라마"] }
   → 201, 같은 필드가 응답에 있다
   지금 필드를 무시하거나 매핑 없음

2. SimulationApiTest.startUsesDramaUserWhenOnlyFortiesAndDrama
   스포츠 캠페인 + 드라마 캠페인
   create: concurrentUsers 2, ageShares { "40대": 100 }, categories ["드라마"]
   start → requestCount=2
   드라마 spentBudget=2, 스포츠 spentBudget=0
   지금 User 1 · Content 1 이라 스포츠만 오른다

3. SimulationApiTest.startSplitsAcrossAgeAndCategory
   스포츠·드라마 캠페인 각 1
   create: concurrentUsers 2, ageShares { "20대": 50, "40대": 50 },
           categories ["스포츠", "드라마"]
   start → 스포츠 spent=1, 드라마 spent=1 (앞 1회 User1/Content1, 뒤 1회 User2/Content2)
   지금 둘 다 스포츠
```

기존 `startSelectsAdsThatManyTimes`는 body에 분포가 없으면 그대로 통과해야 한다.

가짜 테스트: create 201만 주고 start가 여전히 User 1만 고름.
가짜 테스트: 테스트가 MockMvc로 GET /ads를 직접 2회 호출.

SimulatorGapTest: simulator.html · index 공백은 유지.

## Green 최소 구현

통과에 필요한 파일과 책임만 적는다.

```text
CreateRequest: ageShares(Map), categories(List). 없으면 20대 100% · 스포츠
start: concurrentUsers를 비율로 나눠 순차
  20대 → User 1, 40대 → User 2
  스포츠 → Content 1, 드라마 → Content 2
  장르가 둘이면 같은 회차의 연령 슬롯과 짝 (20대-스포츠, 40대-드라마)
PostgreSQL Simulation 테이블 없음
샘플 User/Content INSERT 없음
simulator.html 없음
```

## 검증 명령

```text
./gradlew test --tests SimulationApiTest --tests SimulatorGapTest
./gradlew test
```

## 하지 않는 것

```text
simulator.html, index Simulator 링크
30대 사용자 시드, 가상 User 대량 INSERT
Impression / Click 루프
비동기 워커 / Redis / Kafka / k6 / SSE
FR-14 전체, Experiment RPS
Frequency Cap / Budget / UNIQUE 변경
```

## HITL

구현 전에 개발자 승인. 승인 문장 예: `승인` / `T7-04 승인`

```text
하는 것  create 분포 → start가 User 1/2 · Content 1/2 순차
하지 않는 것 화면, Impression, 30대 시드, k6
```

STOP. 승인 전에 분포 선택 코드를 쓰지 않는다.
