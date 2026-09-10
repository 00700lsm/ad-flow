# Analysis

```text
Task: T5-02
Phase: 5
Date: 2026-09-10
```

## 요청

T5-01 다음. Phase 5에서 FR-12 중 무엇이 재현이고 무엇이 한계인지 **문서로 고정**한다. UNIQUE / 멱등 해법은 이 Task에서 구현하지 않는다.

## 근거 문서

```text
REQUIREMENTS  FR-12 같은 eventId는 집계가 한 번만 증가
DESIGN        4절·12.5: unique 없는 INSERT. Dashboard COUNT
ROADMAP       Phase 5 완료: 입력 3 / 유효 1 / 집계 +1
              기술은 결과가 필요성을 말할 때만
TASKS         T5-01 DONE. FR-12 칸 미체크 (데모 A, ADR 008)
              Human Gate 전 UNIQUE / 멱등 금지
ADR           008: 데모에서 중복 집계 감수. FR-12 미충족
측정 T5-01    posted=3 uniqueEventIds=1 aggregated=3
```

문서 충돌:

```text
구현 차단 충돌 없음. T5-02에서 UNIQUE를 넣으면 ADR 008을 건너뛴다.
FR-12 전체를 이 Task에서 충족한다고 쓰면 ADR 008과 충돌한다.
동시 중복 레이스는 T5-01이 아니다. Phase를 닫으면 그 재현도 안 한다.
Phase 6(FR-13)은 Phase 5를 닫은 뒤에만 연다.
```

현재 코드 / 측정으로 본 FR-12:

```text
재현     T5-01: 순차 동일 eventId 3 → 집계 3
해법     없음 (ADR 008 A)
동시     미측정
Click    같은 키 미측정. Impression과 동일 경로(accept+save)
```

## 제약

```text
해도 되는 것
  충족/미충족을 TASKS·DESIGN·ROADMAP·ADR에 고정
  Phase 5를 닫을지 Human Gate에서 고름

하면 안 되는 것
  UNIQUE / upsert / Kafka 멱등 Consumer 구현
  ADR 008 번복을 승인 없이 코드로 넣기
  REQUIREMENTS FR-12 문장 삭제
  Phase 6 Task를 이 요청에서 시작

영향 파일 후보
  (승인·후보 후 Summary) TASKS 포인터·Phase 5 칸, DESIGN, ROADMAP
  ADR: Phase 완료 범위를 바꾸면 009
```

## 하지 않는 이유

T5-01에서 이미 A를 골랐다. UNIQUE는 해법이지 다음 재현이 아니다.
동시 레이스는 순차 3배가 이미 있다. 데모에서 멱등을 안 고르면 같은 한계다.

## Exit

```text
Valid: yes
다음: plan.md
```
