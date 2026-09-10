# Analysis

```text
Task: T7-10
Phase: 7
Date: 2026-09-10
```

## 요청

T7-09 다음. Simulator로 낸 요청이 Dashboard에서 보이는 시연 경로를 적고 관찰한다. Phase 닫기, k6, 연속 루프, FR-14 전체는 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-14 분포를 설정해 요청하고 분배·예산·캡·대시보드를 확인
              Phase 완료이지 T7-10이 아니다
DESIGN        12.7 캠페인별 노출·Budget·Dashboard
              4절: 폼 30대·clickRate 있음. 시연 README 없음
ROADMAP       질문: 제품 화면에서 변화를 볼 수 있는가
              T7-09 thirtiesShare=1. FR-14 미충족
TASKS         T7-09 DONE. FR-14 칸 미체크
ADR           013 제품 HTTP. 001 한 Task ≠ FR-14
              012와 같이 닫기는 시연 다음이 될 수 있다
측정 T7-09    thirtiesShare=1 clickRate=1
```

문서 충돌:

```text
없음. k6·Kafka Event 증가·Phase 닫기를 한 Task로 넣으면 ADR 001과 충돌한다.
12.7 Kafka 증가는 브로커 없음. 대시보드는 기존 누적이다.
```

현재 코드:

```text
POST /simulations · start Impression · clickRate Click
simulator.html 폼
README Current Status만. Demo 절에 Simulator 단계 문장만 있고 조작 경로 없음
index: Simulator에서 요청을 낸다. Dashboard 숫자 안내 없음
```

## 제약

```text
해도 되는 것
  README 시연 단계 (폼 → start → /dashboard.html)
  index 한 줄
  artifacts/T7-10/observation.md (확인한 숫자만)
  기존 API로 curl 또는 화면

하면 안 되는 것
  k6 / 연속 start 루프 / 초당 요청
  Kafka / SSE / Redis
  Frequency Cap / Budget / UNIQUE 계약 변경
  Phase 7 DONE, FR-14 칸 체크

영향 파일 후보
  README.md, index.html
  artifacts/T7-10/observation.md
  (테스트) 시연 안내가 있는지
```

## 하지 않는 이유

API·폼은 T7-03~T7-09가 끝냈다. 없는 것은 사람이 따라갈 경로와 관찰이다.
캡 전용 시연은 T2-04 Player가 있다. 이 Task는 Simulator→Dashboard다.
닫기는 T6-02처럼 한계를 고르는 다음 Task다.

## Exit

```text
Valid: yes
다음: plan.md
```
