# Plan

```text
Task: T6-01
Phase: 6
HITL: approved
```

## 완료 조건

```text
GET /dashboard 응답에 Impression/sec · Click/sec · Lag가 없다
dashboard.html은 3초 폴링이고 SSE가 없다
공백을 테스트로 재현하고 남긴다
SSE / 초당 지표 구현 / Kafka는 포함하지 않는다
Human Gate 전에 실시간으로 고치지 않는다
```

## Red Tests

```text
1. DashboardRealtimeGapTest.campaignJsonHasNoPerSecondOrLagFields
   GET /dashboard/campaigns/{id}
   impressions / clicks / ctr 는 있다
   impressionsPerSec · clicksPerSec · kafkaLag 를 기대하면 지금 실패 (필드 없음)

2. DashboardPageTest 또는 같은 클래스
   dashboard.html 에 EventSource / impressionsPerSec 를 기대하면 지금 실패
   실제 고정은 "3초마다" + EventSource 없음
```

가짜 테스트: 누적 impressions가 있다고 실시간이다. FR-07이다.
가짜 테스트: 3초 후 누적이 오른다고 초당 지표다.

## Green 최소 구현

```text
DashboardRealtimeGapTest 만
  JSON에 초당·Lag 필드 없음
  HTML에 setInterval 3000, EventSource 없음
  measurement.txt
    pollMs=3000 sse=0 impressionsPerSecField=0

프로덕션 Dashboard / HTML 변경 없음
SSE 엔드포인트 없음
```

## 검증 명령

```text
./gradlew test --tests DashboardRealtimeGapTest
./gradlew test
```

## 하지 않는 것

```text
SSE, WebSocket, Kafka, Redis, Prometheus, Simulator
impressionsPerSec 구현, 새 API
Frequency Cap / Budget / UNIQUE 변경
FR-13 전체
```

## HITL

구현 전에 개발자 승인. 승인 문장 예: `승인` / `T6-01 승인`

재현 후 후보만 보고 이 Task에서 해법을 넣지 않는다.

```text
A  감수. 3초 폴링 누적만 (FR-13 미충족은 다음 Task에서 고정)
B  기존 GET에 최근 1초 건수로 impressionsPerSec / clicksPerSec (SSE 없음)
C  SSE 또는 WebSocket 푸시
```

추천: 승인 후 테스트로 공백만 남긴다. B/C는 측정·재현 다음 Human Gate다. Kafka Lag는 브로커가 없어 B에도 넣지 않는다.

STOP. 승인 전에 프로덕션 코드·SSE·초당 필드를 쓰지 않는다.
