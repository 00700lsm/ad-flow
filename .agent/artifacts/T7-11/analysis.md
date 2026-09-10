# Analysis

```text
Task: T7-11
Phase: 7
Date: 2026-09-10
```

## 요청

T7-10 다음. Phase 7에서 FR-14 중 무엇이 데모이고 무엇이 한계인지 문서로 고정하고 Phase를 닫을지 고른다. k6 / 연속 루프 / Kafka 구현은 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-14 사용자 수·연령·장르로 요청, 분배·예산·캡·대시보드 확인
DESIGN        12.7 노출 분배·Budget·캡·요청량·Kafka Event·Dashboard
              코드: HTTP Simulator. start 한 번 순차. Kafka 없음
ROADMAP       완료 조건 FR-14. RPS는 Phase 이후 Experiment
              T7-10 impressions=2 clicks=2 spentBudget=2. FR-14 미충족
TASKS         T7-10 DONE. FR-14 칸 미체크
ADR           013 C 제품 HTTP. 001 한 Task ≠ FR-14 전문
              012처럼 데모 닫기와 FR 전문 미충족을 같이 쓸 수 있다
측정 T7-10    indexSimulatorThenDashboard=1
              mem 8081 impressions=2 clicks=2 spentBudget=2
```

문서 충돌:

```text
구현 차단 충돌 없음. 이 Task에서 k6·Kafka를 넣으면 ROADMAP Experiment·ADR 013과 충돌한다.
FR-14 전문을 이 Task에서 충족했다고 쓰면 연속 부하·캡 전용 시연·Kafka가 없다.
Phase 8은 ROADMAP에 없다. 닫으면 다음은 Experiment다.
```

현재 코드 / 측정으로 본 FR-14:

```text
있는 것  concurrentUsers · ageShares · categories · clickRate
         start → GET /ads · Impression · Click
         T7-04 연령·장르 분배 spent
         T7-10 Dashboard 노출·클릭·spentBudget
없는 것  start 연속 루프, k6, Kafka Event 증가
         Simulator로 Frequency Cap 전환 시연 (캡은 T2-04 Player)
```

## 제약

```text
해도 되는 것
  충족/한계를 TASKS·DESIGN·ROADMAP·ADR에 고정
  Phase 7를 닫을지 Human Gate에서 고름

하면 안 되는 것
  k6 / 연속 루프 / Kafka 구현
  REQUIREMENTS FR-14 문장 삭제
  Experiment Task를 이 요청에서 시작

영향 파일 후보
  (후보 후 Summary) TASKS, DESIGN, ROADMAP
  ADR: Phase 완료 범위를 바꾸면 015
```

## 하지 않는 이유

RPS는 ROADMAP이 Phase 7 이후로 둔다. Kafka는 브로커가 없다.
캡 화면은 T2-04가 있다. Simulator 캡 시연은 FR-14 문장의 일부일 뿐 새 해법이 아니다.

## Exit

```text
Valid: yes
다음: plan.md
```
