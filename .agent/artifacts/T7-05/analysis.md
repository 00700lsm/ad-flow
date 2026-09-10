# Analysis

```text
Task: T7-05
Phase: 7
Date: 2026-09-10
```

## 요청

T7-04 다음. 제품 화면 `simulator.html`에서 사용자 수·연령·장르를 넣고 start/stop 한다. Impression 루프, 30대 시드, FR-14 전체는 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-14 사용자 수·연령·장르 분포, 광고 요청, 분배·예산·캡·대시보드
              Phase 완료이지 T7-05가 아니다
DESIGN        6.4 User Simulator 화면 (동시 사용자, 연령 분포, 장르, Simulation)
              4절: simulator.html 없음. API는 T7-04까지 있음
              12.7 Impression은 이후. Kafka 이벤트 증가는 브로커 없음
ROADMAP       질문: 제품 화면에서 분배·예산·캡·대시보드를 볼 수 있는가
              RPS는 Experiment
TASKS         T7-04 DONE. FR-14 칸 미체크. 다음 Task는 이번 요청
ADR           013 C 제품 HTTP. 화면은 그 API를 쓰는 정적 HTML
              001: 한 Task ≠ FR-14 전체
측정 T7-04    dramaOnlySpent=2. 화면·Impression 없음
측정 T7-01    simulatorHtml=0
```

문서 충돌:

```text
없음. 6.4의 30대·예능·실시간 Ad Requests/s와 Impression을 한 Task로 넣으면 ADR 001과 충돌한다.
6.4 스케치의 30대는 샘플 User가 없다. T7-04는 20대·40대만 쓴다.
```

현재 코드:

```text
POST /simulations · start · stop. ageShares · categories
정적 HTML: console / player / dashboard. simulator.html 없음
SimulatorGapTest: 파일 없음, index에 링크 없음
```

## 제약

```text
해도 되는 것
  src/main/resources/static/simulator.html
  index·nav에 링크
  폼: concurrentUsers, 20대·40대 비율, 스포츠·드라마
  fetch POST /simulations 후 /start · /stop
  requestCount를 화면에 표시
  SimulatorGapTest를 페이지 있음으로 맞춤

하면 안 되는 것
  Impression / Click 자동 POST
  30대 시드 / 예능 콘텐츠
  실시간 Ad Requests/s · SSE
  k6 / Redis / Kafka
  Experiment RPS
  Frequency Cap / Budget / UNIQUE 계약 변경
  FR-14 전체를 이 Task에서 충족한다고 쓰기

영향 파일 후보
  simulator.html, index.html, 다른 페이지 nav
  SimulatorGapTest → SimulatorPageTest (가칭)
```

## 하지 않는 이유

Impression은 Player/Event 경로다. 화면이 없어도 예산은 GET /ads로 이미 오른다.
30대·예능은 샘플이 없다. 입력만 있으면 T7-04가 User 1로 접는다.
Dashboard 시연·Phase 닫기는 화면이 있는 다음이다.

## Exit

```text
Valid: yes
다음: plan.md
```
