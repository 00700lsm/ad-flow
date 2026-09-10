# Analysis

```text
Task: T7-02
Phase: 7
Date: 2026-09-10
```

## 요청

T7-01 다음. Phase 7에서 FR-14 중 무엇이 재현이고 무엇이 다음 해법인지 **문서로 고정**한다. POST /simulations 구현은 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-14 가상 사용자 수·연령·장르 분포, 광고 요청, 분배·예산·캡·대시보드
              Phase 완료 조건이지 T7-02가 아니다
DESIGN        4절: Simulator 없음
              6.4 / 9.5: POST /simulations · start / stop (목표)
              12.7: T7-01 공백. Kafka 이벤트 증가는 목표 문장. 브로커 없음
ROADMAP       Phase 7 완료: FR-14. RPS는 이후 Experiment
TASKS         T7-01 DONE. FR-14 칸 미체크
              이번 요청: T7-01 Human Gate 후보 C
측정 T7-01    simulationsEndpoint=0 simulatorHtml=0 k6=0
ADR           001: 한 Task ≠ FR-14 전체
              011·012: SSE / 초당 지표 번복 아님
```

문서 충돌:

```text
구현 차단 충돌 없음. T7-02에서 start/stop 루프를 넣으면 T7-01 Plan과 어긋난다.
FR-14 전체를 이 Task에서 충족한다고 쓰면 ADR 001과 충돌한다.
k6(D)는 ROADMAP이 Phase 7 밖으로 보낸다. C와 같이 넣지 않는다.
```

현재 코드 / 측정으로 본 FR-14:

```text
재현     T7-01: API·화면·k6 없음
해법     없음. 개발자 선택 C (DESIGN 9.5)
B        로컬 스크립트 — 제품 API가 아님
D        k6 — Experiment
Kafka    없음. C에도 Lag/이벤트 브로커는 넣지 않음
```

## 제약

```text
해도 되는 것
  충족/미충족과 다음 방향을 TASKS·DESIGN·ROADMAP·ADR에 고정
  Phase 7를 닫지 않음

하면 안 되는 것
  POST /simulations · start/stop · 가상 사용자 루프 구현
  k6 / Redis / Kafka / SSE
  REQUIREMENTS FR-14 문장 삭제
  Frequency Cap / Budget / UNIQUE 계약 변경
  Experiment RPS

영향 파일 후보
  (승인 후 Summary) TASKS 포인터, DESIGN 12.7, ROADMAP
  ADR 013: 제품 Simulator API (C). 구현은 다음 Task
```

## 하지 않는 이유

C는 해법 선택이다. 이 Task는 고른 방향을 문서에 남긴다.
가상 트래픽을 지금 넣으면 T7-01 공백 테스트와 한 커밋에 섞인다.

## Exit

```text
Valid: yes
다음: plan.md
```
