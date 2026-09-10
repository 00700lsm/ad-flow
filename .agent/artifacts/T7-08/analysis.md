# Analysis

```text
Task: T7-08
Phase: 7
Date: 2026-09-10
```

## 요청

T7-07 다음. 30대 비율이 샘플 User 1(20대)로 떨어지지 않게, 30대 샘플 User와 start 매핑을 둔다. 시뮬레이터 폼, FR-14 전체는 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-03 User 샘플. FR-14 연령 분포로 요청
              Phase 완료이지 T7-08이 아니다
DESIGN        9.5 T7-04: 20대→User 1, 40대→User 2. 30대 시드 없음
              6.4 화면 30대 막대는 목표. 이 Task는 시드·매핑
              10.3 User id, age
ROADMAP       T7-07 clickRate. 30대 시드 없음. FR-14 미충족
TASKS         T7-07 DONE. 30대 시드가 다음 Task. FR-14 칸 미체크
ADR           013 HTTP Simulator. 001 한 Task ≠ FR-14
측정 T7-07    clickRate100Clicks=2. 30대 시드 없음
```

문서 충돌:

```text
없음. 폼 30대·clickRate 입력을 한 Task로 넣으면 ADR 001과 충돌한다.
6.4 UI 막대는 목표 화면. 시드가 있어야 분포 API가 30대를 고른다.
```

현재 코드:

```text
SampleDataLoader: User 1 age 28, User 2 age 45
userId: 40대가 아니면 User 1
AGE_ORDER에 30대는 있으나 User 3 없음
simulator.html은 20대·40대만
```

## 제약

```text
해도 되는 것
  User 3, age 30~39
  start ageShares 30대 → User 3
  20대→User 1, 40대→User 2 유지
  기존 DB에 User 3이 없으면 시드

하면 안 되는 것
  simulator.html 30대 % / clickRate 필드
  가상 User 대량 INSERT
  Kafka / k6 / SSE / Redis
  Frequency Cap / Budget / UNIQUE 계약 변경
  FR-14 전체를 이 Task에서 충족한다고 쓰기

영향 파일 후보
  SampleDataLoader, SimulationService
  SimulationApiTest
```

## 하지 않는 이유

폼은 T7-05가 20·40만 받았다. 서버 매핑이 없으면 폼만 넣어도 User 1이다.
clickRate 폼은 T7-07이 서버만 했다. 같은 이유로 미룬다.
preferredCategories는 선택 필터가 아니다. age만 30대면 된다.

## Exit

```text
Valid: yes
다음: plan.md
```
