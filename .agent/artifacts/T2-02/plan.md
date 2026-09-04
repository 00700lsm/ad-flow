# Plan

```text
Task: T2-02
Phase: 2
HITL: pending
```

## 완료 조건

```text
동일 userId + campaignId에 대해 동시 GET /ads 후 Impression을 남기면
당일 IMPRESSION 건수가 frequencyCap을 넘는 경우가 테스트로 재현된다
초과분(선택 횟수 또는 저장 건수 − cap)을 남긴다
Redis / Lock / Lua 등 해법은 구현하지 않는다
Human Gate에서 후보를 고르기 전에는 다음 Task로 가지 않는다
```

이 Task는 「한도를 지키게 고친다」가 아니다.
테스트 기대는 **한도 초과가 일어난다**이다. 통과 = Race 재현.

## Red Tests

실패해야 하는 테스트 목록.

```text
1. FrequencyCapRaceTest.concurrentSelectThenImpressionExceedsCap
   cap=1 캠페인 1개, 같은 userId/contentId
   여러 스레드가 동시에 GET /ads → 200이면 POST /events/impression
   당일 IMPRESSION 건수 > 1 이어야 한다
   현재 코드에서 초과가 안 나오면 재현 실패(테스트 실패). 해법을 넣지 않고
   스레드 수·동기화만 조정해 재현을 시도한다 (max_retry_limit)
```

가짜 테스트(무조건 pass, Thread.sleep만, 초과를 assert하지 않음)는 반려한다.
`@Transactional` 클래스 테스트는 동시 요청과 충돌하므로 이 테스트에는 쓰지 않는다.

## Green 최소 구현

통과에 필요한 파일과 책임만 적는다.

```text
src/test/.../FrequencyCapRaceTest.java (신규)
  CountDownLatch 등으로 GET을 맞춘 뒤 Impression
  최종 count > cap 을 assert
  측정값(요청 수, 성공 선택, Impression, 초과분)을 로그 또는 artifacts/T2-02에 한 줄

프로덕션 코드
  변경 없음. AdSelector / AdServingService / Event 경로를 고치지 않는다
```

재현이 안정적으로 되면 Plan HITL에서 예고한 Human Gate를 응답에 적고 STOP한다.
미래 Phase 코드는 적지 않는다.

## 검증 명령

```text
./gradlew test --tests FrequencyCapRaceTest
린트: ./gradlew compileTestJava
수동 Demo: 이 Task 범위 아님 (Player는 Phase 완료 항목)
```

## 하지 않는 것

```text
Redis, Lua, Lock, synchronized, DB 락, 원자적 INCR
한도 준수를 성공 조건으로 바꾸는 테스트 (그건 해법 Task)
Budget / Kafka / Simulator / SSE / 새 엔드포인트
Player/Dashboard UI
```

## Human Gate (구현 승인 후 재현이 되면 보고)

```text
현재 문제
  GET과 Impression이 분리되어 동시 요청이 같은 COUNT를 본다

현재 측정 / 화면 상태
  (재현 테스트 숫자로 채움)

원인 가설
  read-then-write. 서빙은 readOnly COUNT, INCR는 Impression

후보 A  선택 시점에 카운터를 원자적으로 올린다 (아직 저장소 미정)
후보 B  Impression 기록에 조건부 INSERT / 유니크 한도
후보 C  DB lock으로 GET+기록 구간을 직렬화

장점 / 단점 / 추천안 / Trade-off
  재현 숫자를 본 뒤에 적는다. 이 Plan 승인만으로 Redis를 고르지 않는다
```

## HITL

구현 전에 개발자 승인. 승인 전에는 테스트 파일도 작성하지 않는다.

승인 문장 예: `승인` / `T2-02 승인`
