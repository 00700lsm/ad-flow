# Plan

```text
Task: T2-04
Phase: 2
HITL: approved
```

## 완료 조건

```text
같은 사용자가 OTT Player에서 같은 콘텐츠를 반복 재생하면
frequencyCap에 걸린 캠페인은 더 이상 안 나오고, 다른 후보가 있으면 그 광고가 나온다
전환은 Player 화면에서 캠페인 이름으로 읽을 수 있다
시연 경로는 README에 T2-03 계약(GET /ads가 당일 슬롯을 소비)으로 적힌다
관찰을 artifacts/T2-04에 남긴다
Redis / 새 HTTP API / 캡 로직 변경은 포함하지 않는다
```

## Red Tests

실패해야 하는 테스트 목록.

```text
1. PlayerPageTest.playerHtmlHasSessionAdHistory
   GET /player.html (또는 classpath static)
   세션 노출 이력을 보여줄 영역이 있다 (예: id="adHistory")
   구현 전 실패. 문자열만 있고 이력이 안 쌓이면 Summary에서 반려
```

가짜 테스트: `/ads`를 다시 호출하고 캠페인 ID만 assert하는 API 테스트. T2-03 중복이며 Player가 아니다.

브라우저 E2E 프레임워크는 이 Task에서 도입하지 않는다. 화면 확인은 아래 수동 Demo다.

## Green 최소 구현

```text
player.html
  이번 세션에서 재생할 때마다 캠페인명을 이력에 쌓는다
  현재 광고 + 이력을 같이 보여 캡 이후 변경을 한 화면에서 읽는다
  GET /ads 계약은 바꾸지 않는다

index.html
  Phase 2 시연 한 줄: 같은 사용자 반복 재생 → 광고 변경

README.md
  Current Status를 T2-03 + T2-04 확인으로
  Impression이 캡을 채운다는 문구를 GET 슬롯 소비로
  Console: 같은 타겟·같은 날, cap=1 고우선 + 저우선 두 캠페인
  Player: 사용자 1 + 콘텐츠 1을 두 번 재생 → 이력이 다른 캠페인

artifacts/T2-04/observation.md
  승인 후 브라우저(또는 동일 경로 curl + Player 조작)로 확인한 결과만
  확인하지 않고 Phase 2 DONE이라고 쓰지 않는다
```

백엔드 FrequencyCapCounter / AdServingService는 이 Task에서 수정하지 않는다.

## 검증 명령

```text
./gradlew test --tests PlayerPageTest
./gradlew test
린트: ./gradlew compileJava compileTestJava
수동 Demo:
  docker compose + bootRun
  /console.html  스포츠·연령 맞는 캠페인 2개 (priority 높음 cap=1, 낮음 cap=2)
  /player.html   사용자 A · 축구 하이라이트 → 재생 → 다시 재생
  이력 1번째 ≠ 2번째 캠페인명
```

## 하지 않는 것

```text
Redis, Kafka, Lua, Lock, 새 엔드포인트
캡 카운터를 Impression으로 되돌리기
Budget / Simulator / SSE / k6 / Playwright
샘플 Campaign 자동 삽입
```

## HITL

구현 전에 개발자 승인. 승인 전에는 프로덕션 코드와 테스트 변경을 작성하지 않는다.

승인 문장 예: `승인` / `T2-04 승인`

Trade-off: GET만 하고 재생을 눌러도 슬롯은 줄어든다 (ADR 003). Player는 성공한 GET마다 Impression을 보내므로 시연 경로에서는 둘 다 남는다.
