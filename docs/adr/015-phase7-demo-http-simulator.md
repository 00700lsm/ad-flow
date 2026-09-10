# ADR 015 - Phase 7 데모는 HTTP Simulator까지다

## 문제

FR-14는 분포 설정·요청·분배·예산·캡·대시보드를 같이 적는다. DESIGN 12.7은 요청량·Kafka Event 증가도 적는다. T7-10까지 제품 HTTP Simulator와 Dashboard 시연이 있다. 연속 부하·k6·Kafka는 없다. Phase 7를 닫을지 고른다.

## 현재 Context

```text
관련 Phase      7
관련 측정       T7-10 impressions=2 clicks=2 spentBudget=2
현재 코드       POST /simulations · start 한 번 순차 · simulator.html. Kafka 없음
개발자 선택     T7-11 후보 A (2026-09-10)
                ADR 013 C. RPS는 ROADMAP Experiment
```

## 검토한 대안

```text
A  Phase 7 닫기. HTTP Simulator + Dashboard 시연만 데모. 루프·k6·Kafka는 한계
B  Phase 7 유지. start 연속 루프
C  Phase 7 유지. k6 — Experiment를 Phase 7로 당김
```

## 선택

대안 A.

데모 Phase 7 완료는 제품 HTTP로 가상 사용자 수·연령·장르·clickRate를 넣고 start가 Impression/Click을 남기며 Dashboard에서 숫자가 오르는 것이다. FR-14 전문(연속 부하·캡 전용 시뮬 시연·k6·Kafka Event)은 충족이 아니다. RPS는 ROADMAP이 Phase 이후 Experiment로 둔다. k6는 새 측정 없이 넣지 않는다.

측정 없이 “대규모 부하가 됐다”고 쓰지 않는다. 코드 변경 없음.

## 결과

```text
하는 것      Phase 7를 데모 범위로 닫음. 한계를 문서에 고정
하지 않는 것 k6, 연속 루프, Kafka, FR-14 문장 삭제
남은 한계    start는 한 번 순차. k6 없음. Kafka Event 증가 없음
             Simulator로 Frequency Cap 전환 시연 없음 (캡은 T2-04 Player)
다음에 볼 때 RPS가 제품 문제면 Experiment
             연속 루프가 제품 문제면 T7-11 후보 B
```
