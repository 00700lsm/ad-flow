# ADR 011 - 데모에서는 Dashboard 3초 폴링 누적만 둔다

## 문제

T6-01 측정: pollMs=3000 sse=0 impressionsPerSecField=0.
GET `/dashboard`는 누적 노출·클릭·CTR·예산만 준다. 초당 지표와 SSE가 없다.
Human Gate에서 실시간 해법을 고른다.

## 현재 Context

```text
관련 Phase      6
관련 측정       T6-01 measurement.txt
현재 코드       CampaignStats 누적. dashboard.html setInterval 3000. Kafka 없음
개발자 선택     T6-01 후보 A. 데모이므로 폴링 누적 감수 (2026-09-10)
```

## 검토한 대안

```text
A  감수. 3초 폴링 누적만
B  기존 GET에 최근 1초 건수로 impressionsPerSec / clicksPerSec
C  SSE 또는 WebSocket 푸시
```

## 선택

대안 A.

데모 범위에서는 3초마다 누적을 다시 읽는다. 초당 필드와 SSE는 넣지 않는다. Kafka가 없어 Lag도 계산하지 않는다. FR-13의 Impression/sec · 실시간 스트림은 이 결정으로 아직 충족하지 않는다.

측정 없이 “실시간이 됐다”고 쓰지 않는다. 코드 변경 없음.

## 결과

```text
하는 것      한계를 문서로 고정. 3초 폴링 유지
하지 않는 것 SSE, WebSocket, impressionsPerSec, Kafka, Prometheus
남은 한계    초당 지표 없음. 화면은 최대 3초 지연. Lag / p99 없음
다음에 볼 때 초당·푸시가 제품 문제면 B 또는 C를 다시 Human Gate
```
