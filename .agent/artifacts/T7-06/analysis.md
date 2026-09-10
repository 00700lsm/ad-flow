# Analysis

```text
Task: T7-06
Phase: 7
Date: 2026-09-10
```

## 요청

T7-05 다음. start가 광고 선택마다 Player와 같이 Impression을 남긴다. Dashboard 노출이 오른다. Click 확률, 30대 시드, FR-14 전체는 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-14 요청을 내고 분배·예산·캡·대시보드를 확인
              Phase 완료이지 T7-06이 아니다
DESIGN        12.7 Ad Request → 노출 → Impression → (일부) Click
              4절: Impression 루프 없음. 예산은 GET /ads에서 이미 오름
              6.4 Impression / Click 이벤트 생성
ROADMAP       화면에서 대시보드 변화. Kafka Event 증가는 브로커 없음
TASKS         T7-05 DONE. FR-14 칸 미체크. 다음 Task는 이번 요청
ADR           013: 제품 HTTP Simulator. k6 / Kafka 아님
              003: Impression은 캡·예산 카운터가 아님
              001: 한 Task ≠ FR-14 전체
측정 T7-05    simulatorHtml=1. Impression 루프 없음
```

문서 충돌:

```text
없음. Click 확률·30대·실시간 Ad Requests/s를 한 Task로 넣으면 ADR 001과 충돌한다.
12.7 Kafka Event 증가는 목표 문장. 브로커 없음.
```

현재 코드:

```text
start: AdServingService.select 만. requestCount · spentBudget
POST /events/impression 은 Player·테스트만
SimulationApiTest는 spentBudget만 본다
```

## 제약

```text
해도 되는 것
  start가 선택마다 기존 AdEventService.accept(IMPRESSION)
  고유 eventId
  Dashboard impressions = 선택 횟수 (워커 대기)
  stop 전이면 Impression 0

하면 안 되는 것
  Click 확률 루프
  30대 시드
  Kafka / k6 / SSE / Redis
  Frequency Cap / Budget / UNIQUE 계약 변경
  FR-14 전체를 이 Task에서 충족한다고 쓰기

영향 파일 후보
  SimulationService (AdEventService)
  SimulationApiTest
```

## 하지 않는 이유

Click은 12.7의 다음 문장이다. 확률이면 테스트가 불안정하다.
예산·캡은 GET /ads가 이미 올린다. 이 Task는 대시보드 노출이다.
화면 HTML은 T7-05가 start를 호출하므로 서버 Impression이면 폼 변경이 없다.

## Exit

```text
Valid: yes
다음: plan.md
```
