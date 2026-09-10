# Plan

```text
Task: T7-08
Phase: 7
HITL: pending
```

## 완료 조건

```text
샘플 User 3이 30대다
start가 ageShares 30대를 그 User로 GET /ads 한다
20대·40대 매핑은 T7-04와 같다
simulator.html 30대 입력 / k6 / Kafka는 포함하지 않는다
```

## Red Tests

실패해야 하는 테스트 목록.

```text
1. SimulationApiTest.startUsesThirtiesUserWhenOnlyThirtiesAndSports
   캠페인 A: 스포츠, targetAge 20~29
   캠페인 B: 스포츠, targetAge 30~39
   POST { concurrentUsers: 2, ageShares: { "30대": 100 }, categories: ["스포츠"] }
   start → requestCount=2
   B spentBudget=2, A spentBudget=0
   지금 30대→User 1(28)이라 A가 2이거나 User 3 없어 실패
```

기존 startSplitsAcrossAgeAndCategory · startUsesDramaUserWhenOnlyFortiesAndDrama는 유지.

가짜 테스트: 테스트가 GET /ads?userId=3 을 직접 2회 호출.
가짜 테스트: 30대 캠페인 연령을 20~39로 넓혀 User 1도 맞게 함.

## Green 최소 구현

```text
SampleDataLoader: User(3L, 35, "스포츠"). 없을 때만
SimulationService.userId: 30대 → 3, 40대 → 2, 그 외 → 1
simulator.html 변경 없음
새 Content / 대량 User 없음
```

## 검증 명령

```text
./gradlew test --tests SimulationApiTest --tests SimulatorPageTest
./gradlew test
```

## 하지 않는 것

```text
simulator.html 30대·clickRate
Kafka / k6 / SSE / Redis
FR-14 전체, Experiment RPS
Frequency Cap / Budget / UNIQUE 변경
```

## HITL

구현 전에 개발자 승인. 승인 문장 예: `승인` / `T7-08 승인`

```text
하는 것  User 3(35세) · 30대 슬롯 → User 3
하지 않는 것 폼, k6, 대량 User
```

STOP. 승인 전에 User 3 시드 코드를 쓰지 않는다.
