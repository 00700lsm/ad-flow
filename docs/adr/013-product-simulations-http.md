# ADR 013 - Traffic Simulator는 제품 HTTP로 둔다

## 문제

T7-01 측정: simulationsEndpoint=0 simulatorHtml=0 k6=0.
가상 사용자로 광고 요청을 내는 제품 경로가 없다.
Human Gate에서 Simulator 해법을 고른다.

## 현재 Context

```text
관련 Phase      7
관련 측정       T7-01 measurement.txt
현재 코드       GET /ads · events · dashboard. POST /simulations 없음
개발자 선택     T7-01 후보 C / T7-02 C (2026-09-10)
```

## 검토한 대안

```text
A  감수. Player 수동만
B  GET /ads를 N번 치는 로컬 스크립트
C  DESIGN 9.5 POST /simulations + start/stop
D  k6 시나리오
```

## 선택

대안 C.

제품에서 가상 사용자 수·연령·장르 분포를 넣고 요청을 내려면 HTTP가 있다. 스크립트(B)는 화면 경로가 아니다. k6(D)는 ROADMAP이 Phase 7 이후 Experiment로 둔다. Kafka 이벤트 증가·SSE·RPS 단계는 이 결정에 넣지 않는다.

T7-02에서 API를 구현하지 않는다. 측정 없이 “시뮬레이터가 됐다”고 쓰지 않는다.

## 결과

```text
하는 것      다음 구현은 POST /simulations · start · stop
하지 않는 것 A 감수, B 스크립트를 제품으로 쓰기, D k6, Kafka, SSE, FR-14 문장 삭제
남은 한계    API·화면·트래픽 루프 없음 (T7-01)
다음에 볼 때 DESIGN 9.5 구현 Task는 개발자가 요청할 때
```
