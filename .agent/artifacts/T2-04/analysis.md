# Analysis

```text
Task: T2-04
Phase: 2
Date: 2026-09-07
```

## 요청

T2-03까지 API·테스트로 Frequency Cap 한도를 고정했다. Phase 2 남은 완료 조건은 같은 사용자가 Player에서 캡에 걸린 뒤 다른 광고가 나오는 것을 화면으로 확인하는 것이다.

## 근거 문서

```text
REQUIREMENTS  FR-09: 동일 사용자는 동일 광고를 하루 N번까지만 본다
              제품 목표: 화면으로 확인할 수 있는 서비스
DESIGN        4절: T2-03 원자적 INCR. Player 확인은 남음
              5.2: 1·2번째 같은 광고, 3번째 다른 광고
              6.1: Player에서 Frequency Cap 적용 결과 확인
              12.2: OTT Player에서 동일 광고가 더 이상 안 나오는 형태로 확인. 아직 남음
              18: Demo 시나리오 6번 — 반복 재생 → 광고 변경
ROADMAP       Phase 2 완료: FR-09. 결과는 Player에서 광고가 바뀌는 것으로 확인
TASKS         T2-03 DONE. 다음: Player에서 캡 이후 광고 변경 확인
              Phase 2 체크: Player 확인 미완료
              새 Task ID는 개발자 요청 시 → 이 요청이 그 요청. T2-04
ADR           001: Task 하나. 003: 선택 시점 PostgreSQL INCR. GET만으로 슬롯 소비
```

문서 충돌:

```text
구현 차단 충돌 없음.
README Current Status는 T2-01 시점이다.
  Impression이 캡을 채운다고 적혀 있음. 코드는 ADR 003 (GET이 카운터).
T2-01 TASKS 완료 조건 문구는 당시 계약(Impression COUNT)을 남긴 기록이다.
  T2-04가 그 이력을 고치지 않는다. README 시연 경로는 T2-03 계약에 맞춘다.
```

현재 코드:

```text
GET /ads: count < cap 이면 원자적 +1, 실패 시 다음 Priority
Player: 재생마다 GET /ads, 현재 캠페인명 표시, Impression POST
샘플: User/Content만. 캡 시연용 캠페인 2개는 없음 (Console에서 만든다)
캡 이후 전환을 한 화면에 남는 이력은 없다
```

## 제약

```text
이 Phase에서 해도 되는 것
  Player에서 같은 userId로 반복 재생 시 캠페인이 바뀌는 경로를 README에 적는다
  화면에서 전환을 읽을 수 있게 이번 세션 노출 이력을 최소로 남긴다
  브라우저로 재현하고 artifacts/T2-04에 관찰을 남긴다
  README / index 시연 문구를 T2-03 계약에 맞춘다

하면 안 되는 것
  Redis / Kafka / Lua / Distributed Lock
  새 HTTP API
  캡 카운터 로직 재작성, Impression을 다시 카운터로
  Budget / Simulator / SSE / k6
  Playwright 등 브라우저 자동화 스택 도입
  샘플 캠페인 시드로 Console 시연을 우회

영향 파일 후보
  src/main/resources/static/player.html (세션 노출 이력)
  src/main/resources/static/index.html (시연 단계)
  README.md (Current Status, Player 캡 확인 경로)
  (Summary) TASKS Phase 2 체크, DESIGN 4절·12.2 Player 남음 문구
```

## 하지 않는 이유

API 테스트를 하나 더 넣는 것은 Player 확인이 아니다. T2-01·T2-03이 선택을 이미 고정한다.
이력 UI는 새 서빙 규칙이 아니라, DESIGN 5.2를 화면에서 읽기 위한 최소 표시다.
캠페인 시드는 Console 생성 흐름(FR-08)을 가린다.

## Exit

```text
Valid: yes
다음: plan.md
```
