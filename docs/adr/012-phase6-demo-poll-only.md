# ADR 012 - Phase 6 데모는 3초 폴링까지다

## 문제

FR-13은 Impression/sec·실시간 스트림을 같이 적는다. T6-01은 초당 필드와 SSE가 없음을 재현했다. 해법은 ADR 011 A다. Phase 6를 닫을지 고른다.

## 현재 Context

```text
관련 Phase      6
관련 측정       T6-01 pollMs=3000 sse=0 impressionsPerSecField=0
현재 코드       누적 CampaignStats. dashboard.html 3초 폴링. Kafka 없음
개발자 선택     T6-02 후보 A (2026-09-10)
                T6-01 Human Gate도 A. 폴링 누적 유지
```

## 검토한 대안

```text
A  Phase 6 닫기. 3초 폴링만 데모. 초당·SSE는 한계 (ADR 011)
B  Phase 6 유지. GET에 impressionsPerSec — ADR 011 번복
C  Phase 6 유지. SSE / WebSocket — ADR 011 번복
```

## 선택

대안 A.

데모 Phase 6 완료는 누적 Dashboard를 3초마다 읽는 것이다. FR-13 전문(초당 지표·SSE·Lag / p99)은 충족이 아니다. T6-01에서 이미 감수를 골랐다. SSE는 새 측정 없이 넣지 않는다.

측정 없이 “실시간이 됐다”고 쓰지 않는다. 코드 변경 없음.

## 결과

```text
하는 것      Phase 6를 데모 범위로 닫음. 한계를 문서에 고정
하지 않는 것 SSE, impressionsPerSec, Kafka, FR-13 문장 삭제
남은 한계    초당 지표 없음. 화면 최대 3초 지연. Lag / p99 없음 (ADR 011)
다음에 볼 때 초당·푸시가 제품 문제면 ADR 011 B 또는 C
             Phase 7(FR-14)는 개발자가 요청할 때
```
