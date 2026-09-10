# Analysis

```text
Task: T7-03
Phase: 7
Date: 2026-09-10
```

## 요청

T7-02 C / ADR 013. POST `/simulations` · start · stop 으로 가상 사용자 수만큼 광고 요청을 낸다. 연령·장르 분포 UI, Impression 생성, Dashboard 시연, FR-14 전체는 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-14 사용자 수·분포·요청·분배·예산·캡·대시보드
              Phase 완료이지 T7-03이 아니다
DESIGN        9.5 POST /simulations, /start, /stop
              6.4 동시 사용자 수. 분포 UI는 목표 화면
              12.7 User → Content → Ad Request → Impression
              4절: 아직 Simulator 코드 없음
ROADMAP       Phase 7 IN PROGRESS. RPS는 Experiment
TASKS         T7-02 DONE. 다음은 DESIGN 9.5 구현
ADR           013 C. k6 / 스크립트 아님. Kafka·SSE 아님
측정 T7-01    simulationsEndpoint=0
```

문서 충돌:

```text
없음. 9.5와 6.4 분포 UI를 한 Task로 넣으면 ADR 001과 충돌한다.
Kafka 이벤트 증가는 12.7 목표 문장. 브로커 없음.
```

현재 코드:

```text
GET /ads?userId=&contentId= 만
샘플 User 1(28, 스포츠) Content 1(스포츠)
POST /simulations 없음
SimulatorGapTest: java에 /simulations 없음, simulator.html 없음
```

## 제약

```text
해도 되는 것
  POST /simulations (concurrentUsers)
  POST start: 기존 User/Content로 GET /ads를 concurrentUsers번 (순차)
  POST stop: 아직 start 전이면 요청을 안 냄
  메모리 보관. PostgreSQL Simulation 테이블 없음
  SimulatorGapTest의 API 없음 기대를 맞춤

하면 안 되는 것
  simulator.html / 연령·장르 분포 UI
  가상 User 대량 INSERT
  Impression / Click 자동 생성
  k6 / Redis / Kafka / SSE
  Experiment RPS
  Frequency Cap / Budget / UNIQUE 계약 변경
  FR-14 전체를 이 Task에서 충족한다고 쓰기

영향 파일 후보
  SimulationController / SimulationService (가칭)
  SimulationApiTest
  SimulatorGapTest (화면 공백만 남김)
```

## 하지 않는 이유

분포 UI·대시보드 시연은 요청이 난 다음이다. T2-01이 순차 캡만 한 것과 같다.
start를 k6로 바꾸면 ADR 013을 건너뛴다.
Impression은 Player/Event 경로다. 이 Task는 Ad Request만 낸다.

## Exit

```text
Valid: yes
다음: plan.md
```
