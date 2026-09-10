# Analysis

```text
Task: T7-09
Phase: 7
Date: 2026-09-10
```

## 요청

T7-08 다음. 시뮬레이터 화면에서 30대 비율과 clickRate를 넣고 POST /simulations가 받는다. FR-14 전체, 새 API는 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-14 연령 분포를 설정해 요청. 화면에서 확인
              Phase 완료이지 T7-09가 아니다
DESIGN        6.4 연령대 입력. 4절·9.5: 폼에 30대·clickRate 없음
              API는 T7-04 ageShares, T7-07 clickRate, T7-08 User 3
ROADMAP       T7-08 thirtiesSpent=2. 폼 30대 없음. FR-14 미충족
TASKS         T7-08 DONE. 폼이 다음 Task. FR-14 칸 미체크
ADR           013 제품 HTTP. 001 한 Task ≠ FR-14
측정 T7-08    thirtiesSpent=2. 폼 30대 없음
```

문서 충돌:

```text
없음. k6·Phase 닫기를 한 Task로 넣으면 ADR 001과 충돌한다.
API 필드는 있다. 이 Task는 화면이 그 필드를 보내는가이다.
```

현재 코드:

```text
simulator.html: 20대·40대·스포츠·드라마. clickRate 없음. 30대 없음
POST는 이미 30대·clickRate를 받음
```

## 제약

```text
해도 되는 것
  폼 30대 %, clickRate
  JSON ageShares['30대'], clickRate
  T7-05 필드 유지

하면 안 되는 것
  새 HTTP 경로
  Kafka / k6 / SSE / Redis
  Frequency Cap / Budget / UNIQUE 계약 변경
  FR-14 전체를 이 Task에서 충족한다고 쓰기

영향 파일 후보
  simulator.html
  SimulatorPageTest
```

## 하지 않는 이유

서버 계약은 T7-04·T7-07·T7-08이 끝냈다. 화면만 비었다.
분배 대시보드 시연·Phase 닫기는 다음이 될 수 있다.

## Exit

```text
Valid: yes
다음: plan.md
```
