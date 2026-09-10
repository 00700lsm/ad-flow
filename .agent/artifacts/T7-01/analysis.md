# Analysis

```text
Task: T7-01
Phase: 7
Date: 2026-09-10
```

## 요청

Phase 7를 연다. 첫 작업은 가상 사용자 수·연령·장르 분포로 광고 요청을 내는 Traffic Simulator가 **있는지를 재현**하는 것이다. Simulator 구현·k6·RPS 실험은 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-14 가상 사용자 수·연령·장르 분포 설정, 광고 요청 발생,
              분배·예산·캡·대시보드 변화 확인
              Phase 완료 조건이지 T7-01이 아니다
DESIGN        4절: Traffic Simulator는 아직 코드에 없음
              6.4 / 9.5: 목표 UI·POST /simulations · start / stop
              12.7: User → Content → Ad Request → Impression → Click
              Kafka 이벤트 증가는 목표 문장. 현재 Kafka 없음 (ADR 005)
ROADMAP       Phase 7 질문: Simulator로 분배·예산·캡·대시보드를 화면에서 보는가
              완료: FR-14 — Phase 완료
              RPS 단계는 이 Phase 이후 Experiment
TASKS         Phase 6 DONE (ADR 012). 다음: Phase 7는 개발자가 요청할 때 → 이번 요청
              Phase 7 Task는 미리 넣지 않았다. FR-14 전체를 한 Task로 구현하지 않는다
ADR           001: Phase 진행 ≠ FR-14 전체
              011·012: SSE / 초당 지표는 Phase 6 한계. 이 Task에서 번복하지 않음
```

문서 충돌:

```text
구현 차단 충돌 없음.
DESIGN 6.4·9.5·12.7은 목표다. 4절은 시뮬레이터가 없다고 한다.
T7-01은 9.5를 구현하지 않는다. 지금 API·화면에 시뮬레이터가 없는지만 본다.
Kafka가 없어도 FR-14의 가상 사용자 요청 공백은 먼저 본다.
```

현재 코드:

```text
HTTP  Campaign / Creative / GET /ads / events / dashboard 만
POST /simulations · start · stop 없음
simulator.html 없음. index는 Console / Player / Dashboard만
k6 / Prometheus 없음
가상 사용자 수·연령·장르 분포를 넣는 입력 없음
부하 재현은 Player 수동 또는 테스트 동시 GET
```

## 제약

```text
해도 되는 것
  POST /simulations 가 없음을 테스트로 재현
  simulator 화면이 없음을 테스트로 재현
  Human Gate 전에 Simulator API / UI / k6를 넣지 않음

하면 안 되는 것
  POST /simulations 구현, start/stop, 가상 사용자 루프
  k6 / Prometheus / Grafana / Kafka / Redis
  SSE / impressionsPerSec (ADR 011 번복)
  Frequency Cap / Budget / UNIQUE 계약 변경
  FR-14 전체를 이 Task에서 충족한다고 쓰기
  Experiment RPS (100 / 500 / 1,000 / 3,000)

영향 파일 후보
  테스트: SimulatorGapTest (가칭)
  (승인 후 Summary) TASKS T7-01, DESIGN 12.7에 공백 재현만
```

## 하지 않는 이유

지금 Simulator를 넣으면 공백이 사라진다. T6-01이 SSE를 넣지 않은 것과 같다.
k6로 RPS를 넣으면 ROADMAP이 Phase 7 밖으로 보낸 Experiment가 된다.
Kafka 이벤트 증가는 브로커가 있을 때의 12.7 문장이다. T7-01 재현 대상이 아니다.

## Exit

```text
Valid: yes
다음: plan.md
```
