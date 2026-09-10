# Analysis

```text
Task: T7-07
Phase: 7
Date: 2026-09-10
```

## 요청

T7-06 다음. start가 Impression 뒤에 Player와 같이 Click을 남긴다. Dashboard 클릭이 오른다. 비율은 결정적으로 재현한다. 30대 시드, FR-14 전체는 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-14 요청을 내고 분배·예산·캡·대시보드를 확인
              Phase 완료이지 T7-07이 아니다
DESIGN        12.7 Impression → 일부 확률로 Click
              4절·9.5: start는 Impression. Click 없음
              6.4 Click은 POST /events/click 과 같은 이벤트
ROADMAP       T7-06 startImpressions=2. Click 없음. FR-14 미충족
TASKS         T7-06 DONE. Click 확률이 다음 Task. FR-14 칸 미체크
ADR           013: 제품 HTTP Simulator. k6 / Kafka 아님
              009: eventId UNIQUE. Impression과 Click은 다른 eventId
              001: 한 Task ≠ FR-14 전체
측정 T7-06    startImpressions=2. Click 없음
```

문서 충돌:

```text
없음. 난수 Click만 넣으면 테스트가 불안정하다. 그래서 clickRate 정수와 슬롯 배정.
30대 시드·실시간 Ad Requests/s를 한 Task로 넣으면 ADR 001과 충돌한다.
12.7 Kafka Event 증가는 목표 문장. 브로커 없음.
```

현재 코드:

```text
start: select → accept(IMPRESSION). requestCount · spentBudget · impressions
CreateRequest에 clickRate 없음
SimulationApiTest는 clicks를 보지 않음
```

## 제약

```text
해도 되는 것
  POST /simulations 에 clickRate (0~100). 없으면 0
  start가 선택마다 Impression 후, 슬롯이 Click이면 accept(CLICK)
  고유 eventId (Impression과 다름)
  Dashboard clicks = Click 슬롯 수 (워커 대기)
  비율 배정은 T7-04 expandShares와 같이 결정적 (n * clickRate / 100)
  stop 전이면 Click 0

하면 안 되는 것
  Math.random / 비시드 난수
  30대 사용자 시드
  Kafka / k6 / SSE / Redis
  Frequency Cap / Budget / UNIQUE 계약 변경
  FR-14 전체를 이 Task에서 충족한다고 쓰기

영향 파일 후보
  Simulation, SimulationService, SimulationController
  SimulationApiTest
```

## 하지 않는 이유

난수 확률은 T7-06 analysis가 불안정하다고 미룬 이유다. clickRate 100이면 2/2, 0이면 0이다.
30대는 User 시드가 20·40만 있다. 이 Task는 이벤트가 Click이다.
simulator.html 필드는 T7-05가 start를 부르므로, 서버가 clickRate를 받으면 curl/테스트로 충분하다. 폼은 다음이 될 수 있다.
예산·캡은 GET /ads가 이미 올린다.

## Exit

```text
Valid: yes
다음: plan.md
```
