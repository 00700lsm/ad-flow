# Analysis

```text
Task: T5-05
Phase: 5
Date: 2026-09-10
```

## 요청

T5-04 후보 B. 같은 `eventId`를 여러 번내도 Dashboard 노출이 1인 것을 **시연 경로로 읽게** 한다. UNIQUE 로직·SSE는 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-12 집계 한 번. 제품은 화면으로 확인
DESIGN        4절·12.5: UNIQUE. Dashboard COUNT. Kafka 없음
ROADMAP       Phase 5 완료 식은 T5-03 테스트로 이미 3/1/1
TASKS         T5-04 B: Dashboard/Player 시연. 화면은 다음 Task → 이번 요청
ADR           009: UNIQUE. 201은 접수
측정 T5-03    posted=3 aggregated=1 (API 테스트)
```

문서 충돌:

```text
없음. Player는 재생마다 uuid eventId라 반복 재생만으로는 중복 키가 안 나온다.
시연의 입력은 같은 eventId POST 3회다. Player는 “매번 새 키”라는 한계를 적는다.
SSE / 초당 지표는 Phase 6. T5-05가 넣지 않는다.
```

현재 코드:

```text
player.html     eventId: uuid() 매 Impression/Click
dashboard.html  노출 숫자. 중복 키 안내 없음
README          예산·캡 시연은 있음. eventId 3회 경로는 없음
```

## 제약

```text
해도 되는 것
  README에 같은 eventId POST 3회 → Dashboard 노출 1
  dashboard.html / index.html에 한 줄 안내
  브라우저·curl로 확인하고 artifacts/T5-05/observation.md
  UNIQUE·워커는 수정하지 않음

하면 안 되는 것
  SSE / Kafka / 새 HTTP 경로
  Player eventId를 고정값으로 바꿔 일반 재생을 깨기
  UNIQUE 제거, 201 계약 변경
  Playwright

영향 파일 후보
  README.md, index.html, dashboard.html
  DashboardPageTest (안내 문구)
  artifacts/T5-05/observation.md
```

## 하지 않는 이유

DuplicateEventIdAggregationTest를 다시 돌리는 것은 화면 시연이 아니다.
Player uuid를 고정하면 정상 노출마다 키가 겹친다.

## Exit

```text
Valid: yes
다음: plan.md
```
