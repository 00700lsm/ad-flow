# Analysis

```text
Task: T7-04
Phase: 7
Date: 2026-09-10
```

## 요청

T7-03 다음. start가 User 1 · Content 1만 쓰지 않고, POST `/simulations`의 연령·장르 분포로 광고 요청을 나눈다. simulator.html, Impression 루프, FR-14 전체는 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-14 사용자 수·연령·장르 분포, 광고 요청, 분배·예산·캡·대시보드
              Phase 완료이지 T7-04가 아니다
DESIGN        9.5 POST /simulations. T7-03: concurrentUsers, User 1 · Content 1
              4절: 연령·장르 분포 없음
              6.4 연령대·장르 분포 (목표 화면). 이 Task는 API만
              12.7 User → Content → Ad Request. Impression은 이후
ROADMAP       Phase 7 IN PROGRESS. 화면에서 분배를 보는 것은 Phase 질문. RPS는 Experiment
TASKS         T7-03 DONE. FR-14 칸 미체크. 다음 Task는 이번 요청
ADR           013: 가상 사용자 수 다음은 연령·장르 분포. k6 / 스크립트 아님
              001: 한 Task ≠ FR-14 전체
측정 T7-03    simulationsEndpoint=1 startRequestCount=2. 화면·분포 없음
```

문서 충돌:

```text
없음. 6.4 UI와 12.7 Impression을 한 Task로 넣으면 ADR 001과 충돌한다.
DESIGN 9.5에 분포 필드 이름은 없다. 새 리소스가 아니라 create body 확장이다.
Kafka 이벤트 증가는 12.7 목표 문장. 브로커 없음.
```

현재 코드:

```text
POST /simulations { concurrentUsers }
start: SAMPLE_USER_ID=1 SAMPLE_CONTENT_ID=1 순차 select
샘플 User 1(28, 스포츠) User 2(45, 드라마)
샘플 Content 1 스포츠 · Content 2 드라마
simulator.html 없음 (SimulatorGapTest)
```

## 제약

```text
해도 되는 것
  create body에 연령 비율·장르 목록
  start가 샘플 User/Content를 그 비율로 순차 선택
  생략 시 T7-03과 같이 User 1 · Content 1
  30대 샘플이 없으면 20대·40대만

하면 안 되는 것
  simulator.html / index Simulator 링크
  가상 User 대량 INSERT
  Impression / Click 자동 생성
  k6 / Redis / Kafka / SSE
  Experiment RPS
  Frequency Cap / Budget / UNIQUE 계약 변경
  FR-14 전체를 이 Task에서 충족한다고 쓰기

영향 파일 후보
  Simulation / SimulationService / SimulationController
  SimulationApiTest
  SimulatorGapTest (화면 공백 유지)
```

## 하지 않는 이유

화면은 ROADMAP 질문이지만 T7-03이 API를 먼저 뒀다. 분포 없이 HTML만 있으면 여전히 User 1이다.
Impression은 Player/Event 경로다. 이 Task는 Ad Request 대상을 나눈다.
30대 사용자를 만들면 샘플 시드 범위를 넘는다.

## Exit

```text
Valid: yes
다음: plan.md
```
