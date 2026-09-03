# ADR

Architecture Decision Record.

의미가 바뀌는 결정만 남긴다. 일상적인 구현 선택은 남기지 않는다.

---

## 작성 시점

```text
문제 재현 / 측정
  ↓
분석
  ↓
Human Gate
  ↓
Decision
  ↓
ADR
  ↓
구현
```

결정 코드보다 먼저, 또는 같은 작업 단위에서 작성한다.
프로젝트 후반에 몰아서 복원하지 않는다.

---

## 대상 예

```text
Redis 도입 여부
Kafka 도입 여부
Budget / Frequency Cap 동시성 제어 방식
이벤트 저장을 Serving API와 분리할지
Source of Truth와 Realtime Counter를 나눌지
프로젝트 Scope 변경
Phase 완료 조건을 바꿀지
한 요청에서 다룰 Task 범위
```

---

## 템플릿

`docs/adr/_template.md`
