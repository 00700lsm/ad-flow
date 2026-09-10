# ADR 014 - Task Summary가 끝나면 그 Task를 커밋한다

## 문제

한 Task가 끝날 때마다 개발자가 `커밋`을 다시 쳐야 했다.
Plan HITL은 코드를 막는다. Summary 뒤 커밋은 이미 끝난 작업의 기록이다.
짧은 입력(`다음` / `승인`)을 쓰면서 Git만 한 턴을 더 쓰는 불일치가 생겼다.

## 현재 Context

```text
관련 Phase      7 (T7-06 ~ T7-09에서 체감)
이전 규칙       사용자가 커밋을 요청하기 전에는 커밋하지 않는다
이미 있는 게이트 Plan HITL, Human Gate, Summary 후 STOP
관찰            Summary 직후 `커밋`이 반복됐다. T4 measurement 시간 변동은 Task가 아닌데 워킹트리에 남았다
```

필요를 느낀 이유:

```text
Task가 끝나도 저장소 HEAD는 이전이었다
다음 Task Plan이 같은 워킹트리에 쌓이면 커밋 범위가 섞였다
푸시·Force·amend는 여전히 위험하다. 로컬 커밋만 빠졌다
```

## 검토한 대안

```text
A  유지. 커밋은 요청 문장만
B  Summary가 끝나면 그 Task 산출물만 커밋. 푸시는 요청 시
C  Plan HITL 산출물과 Summary를 각각 자동 커밋
```

## 선택

대안 B.

Plan은 Task가 끝난 것이 아니다. 승인 전에 커밋하면 HITL이 약해진다.
끝난 Task를 기록하지 않으면 `다음`이 미커밋 구현과 새 Plan을 한 더미로 만든다.
재현 Task와 해법 Task는 여전히 커밋을 나눈다. 한 커밋에 두 문제를 넣지 않는다.

## 결과

```text
하는 것      Summary 후 그 Task 파일만 커밋하고 STOP. 메시지에 Task ID
하지 않는 것 Plan 중 커밋, 푸시, force, 다른 Task·측정 노이즈 포함
남은 한계    개발자가 `커밋하지 마`면 건너뛴다. 푸시는 요청할 때만
```
