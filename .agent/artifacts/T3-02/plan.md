# Plan

```text
Task: T3-02
Phase: 3
HITL: approved
```

## 완료 조건

```text
동일 캠페인에 대해 동시 GET /ads 후 Impression을 남기면
spentBudget이 budget을 넘는 경우가 테스트로 재현된다
초과분(spentBudget − budget 또는 선택/Impression − budget)을 남긴다
Redis / Lock / Lua 등 해법은 구현하지 않는다
Human Gate에서 후보를 고르기 전에는 한도 준수로 고치지 않는다
```

이 Task는 「Overspending 0으로 고친다」가 아니다.
테스트 기대는 **한도 초과가 일어난다**이다. 통과 = Race 재현.

## Red Tests

실패해야 하는 테스트 목록.

```text
1. BudgetRaceTest.concurrentSelectThenImpressionExceedsBudget
   budget=1, frequencyCap=0, 캠페인 1개
   여러 스레드가 동시에 GET /ads → 200이면 POST /events/impression
   GET /campaigns/{id} 의 spentBudget > 1 이어야 한다
   현재 코드에서 초과가 안 나오면 재현 실패(테스트 실패). 해법을 넣지 않고
   스레드 수·동기화만 조정해 재현을 시도한다 (max_retry_limit)
```

가짜 테스트: 초과를 assert하지 않음, Thread.sleep만, 한도 준수를 성공으로 바꿈.
`@Transactional` 클래스 테스트는 동시 요청과 충돌하므로 쓰지 않는다.
T2-03 `FrequencyCapRaceTest`를 예산 한도 준수로 바꾸지 않는다.

## Green 최소 구현

```text
src/test/.../BudgetRaceTest.java (신규)
  CountDownLatch로 GET을 맞춘 뒤 Impression
  최종 spentBudget > budget 을 assert
  측정값(요청 수, 성공 선택, Impression, spent, overflow)을
  로그 또는 artifacts/T3-02/measurement.txt 한 줄

프로덕션 코드
  변경 없음. Campaign.chargeImpression / AdSelector / AdEventService를 고치지 않는다
```

재현이 되면 응답에 Human Gate를 적고 STOP한다.
미래 Phase 코드는 적지 않는다.

## 검증 명령

```text
./gradlew test --tests BudgetRaceTest
린트: ./gradlew compileTestJava
수동 Demo: 이 Task 범위 아님
```

## 하지 않는 것

```text
Redis, Lua, Lock, synchronized, 원자적 DECR, version 컬럼
한도 준수를 성공 조건으로 바꾸는 테스트 (그건 해법 Task)
Frequency Cap 로직 변경
Kafka / Simulator / SSE / 새 엔드포인트
Dashboard 사용·잔여 UI / Player
```

## Human Gate (구현 승인 후 재현이 되면 보고)

```text
현재 문제
  GET은 spent를 읽기만 하고, Impression이 +1 한다. 동시 GET이 같은 잔여를 본다

현재 측정 / 화면 상태
  (재현 테스트 숫자로 채움)

원인 가설
  read-then-write. 서빙은 spent 스냅샷, 차감은 Impression의 비원자 +1

후보 A  Impression 차감을 조건부 UPDATE (spent < budget 일 때만 +1)
후보 B  GET+차감 구간 DB Lock
후보 C  Redis DECR / Lua

장점 / 단점 / 추천안 / Trade-off
  재현 숫자를 본 뒤에 적는다. 이 Plan 승인만으로 Redis를 고르지 않는다
```

## HITL

구현 전에 개발자 승인. 승인 전에는 프로덕션 코드와 테스트 파일을 작성하지 않는다.

승인 문장 예: `승인` / `T3-02 승인`
