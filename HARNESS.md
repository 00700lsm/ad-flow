# AdFlow 에이전트 하네스

이 저장소가 에이전트와 사람을 묶는 방식이다. Scrum·Kanban 같은 **제품 개발 방법론**이 아니다. 제품 순서는 `docs/ROADMAP.md`가 정한다. 이쪽은 **에이전트가 코드를 쓸 때 어느 상태에서 멈추고, 무엇을 증거로 남기는지**를 고정한 **엔지니어링 하네스(harness)**다.

한 줄로 부르면 이렇게 보면 된다.

```text
상태 그래프 TDD 하네스
State-graph TDD harness, HITL gated, artifact-compacted
```

업계에서 흔히 붙는 이름과 이 프로젝트에서의 대응:

```text
방법론 (methodology)     제품이 무엇을 어떤 순서로 푸는가 → ROADMAP / REQUIREMENTS
프로세스 (process)        한 일의 단계 → 바이브 루프
하네스 (harness)          에이전트를 상태 머신에 묶어 탈선하지 않게 함 → 이 문서
HITL                     사람이 게이트에서 허가·거절·후보 선택
Human Gate               HITL 중에서도 기술·계약이 갈라지는 비싼 선택
Artifact                 대화 대신 Git에 남는 단계 산출물
바이브 코딩               대화로 바로 짜지 않고, 위 루프를 IDE에서 돌리는 운용 모드
```

철학의 원문은 `docs/Poc.md`다. 에이전트가 매번 따르는 짧은 규칙은 `.cursor/rules/ad-flow.mdc`, Task 루프는 `.cursor/skills/ad-flow-vibe-coding/SKILL.md`, 진입점은 `AGENTS.md`다. 이 파일은 **사람이 읽고 개입 지점을 찾기 위한** 설명이다.

---

## 1. 왜 이렇게 묶는가

LLM은 같은 요청을 다음 턴에 다르게 해석한다. 대화가 길어지면 이전 제약을 잊고 Redis를 넣거나 Phase를 한 번에 구현한다.

그래서 세 가지를 기계적으로 나눈다.

```text
무엇을 만들 것인가     저장소 문서 (대화보다 우선)
지금 어느 Task인가     docs/TASKS.md 포인터
이 Task에서 무엇이 합의됐는가   .agent/artifacts/<TaskID>/
```

핵심 문장:

```text
AI는 구현 속도를 높이는 도구다.
요구사항, 성공 기준, 기술 선택, Scope의 최종 판단은 개발자가 한다.
```

측정하거나 화면으로 확인하지 않은 개선을 개선이라고 쓰지 않는다. 테스트는 제품을 고정하는 수단이고, 제품이 먼저다.

```text
Product → Problem → Experiment → Engineering → Result
```

---

## 2. 두 개의 시계

헷갈리면 여기가 원인이다. **제품 Phase**와 **하네스 페이즈**는 다른 시계다.

제품 Phase (`docs/ROADMAP.md`):

```text
Phase 1  시연 가능한 제품
Phase 2  동일 사용자 과다 노출
Phase 3  예산 초과 소진
Phase 4  서빙과 이벤트 처리 결합
…
```

하네스 단계 (한 Task를 돌릴 때):

```text
0  Governance     .agent/config.yaml
1  Analysis       analysis.md
2  Plan + HITL    plan.md → 사람 승인
3A Red            실패하는 테스트
3B Green          최소 구현
3C Lint           컴파일/린트만
3D Refactor       행위 변경 없이
4  Summary        TASKS / DESIGN 동기화 → STOP
```

`docs/Poc.md`의 Phase 1·2·3은 하네스 단계다. `docs/TASKS.md`의 Phase 3은 예산 문제다. 에이전트에게 “Phase 3 진행”이라고 하면 **제품 Phase 3의 다음 Task**이지, 하네스 Green을 건너뛰라는 뜻이 아니다.

한 요청의 단위는 제품 Phase가 아니라 **TASKS의 Task 하나**다. `docs/adr/001-one-task-at-a-time.md`.

```text
"Phase 3 진행해줘"
  → 그 Phase의 다음 TODO Task Analysis / Plan 까지
"승인"
  → 그 Task의 Red부터 Summary 까지. 다음 Task 허가가 아님
```

---

## 3. 바이브 루프 (한 Task)

```text
개발자: 다음  /  T3-04  /  진행
        ↓
에이전트: TASKS 포인터, REQUIREMENTS / DESIGN / ROADMAP, config.yaml
        ↓
Analysis (읽기 전용)     artifacts/<id>/analysis.md
        ↓
Plan                     artifacts/<id>/plan.md
        ↓
        STOP  ← Plan HITL. 코드 없음
        ↓
개발자: 승인  /  거절 + 수정 지시
        ↓
Red → Green → Lint → Refactor → Summary
        ↓
        STOP  ← 다음 Task로 넘어가지 않음
        ↓
개발자: 다음  /  커밋 푸시  /  화면 피드백
```

건너뛰면 안 되는 것:

```text
Analysis 없이 코드
Plan 승인 없이 프로덕션 코드
실패하지 않은 테스트를 Green의 근거로 사용 (가짜 테스트)
Green 재시도가 max_retry_limit를 넘는데 계속 패치
Summary를 DONE으로 착각하고 다음 Task 구현
커밋 요청 없이 커밋
```

대화 전체를 다음 단계에 복사하지 않는다. 다음 노드가 읽는 것은 `analysis.md`, `plan.md`, 테스트 결과, 필요하면 `measurement.txt`다.

---

## 4. 사람이 개입하는 게이트

게이트는 세 층이다. 일상은 1·2, 비싸면 3이다.

### 4.1 루프를 켤지 (작업 시작)

에이전트는 `docs/TASKS.md` 맨 위 포인터를 본다. 사람이 `다음`을 말하기 전에는 다음 Task를 시작하지 않는다.

사람이 적어도 되는 말:

```text
다음
진행
T3-04
승인
거절. 새 HTTP 경로 빼
커밋 푸시
```

에이전트는 문서 경로를 다시 묻지 않는다. 사람이 이전 채팅을 붙여 넣을 필요도 없다.

### 4.2 Plan HITL (매 Task, 구현 직전)

`plan.md`가 승인 본문이다. 볼 항목:

```text
완료 조건     TASKS / REQUIREMENTS와 같은가
Red Tests     실패해야 하는가. 가짜 테스트인가
Green 범위    최소인가. 미래 Phase가 섞였는가
하지 않는 것  Redis / 새 API / 계약 변경이 몰래 들어왔는가
Trade-off     GET만으로 예산이 줄어드는 것 같은 부작용을 알고 승인하는가
HITL          pending 인가
```

`승인` / `T3-04 승인`이면 그 Task의 Red부터 Summary만 허가한다.  
거절이면 Plan을 고친다. 코드는 여전히 없다.

### 4.3 Human Gate (기술·계약이 갈라질 때)

Plan HITL과 이름이 다르다. **이미 동작하는 단순 구현을 재현·측정한 뒤**, 해법 기술을 고를 때 연다.

바로 구현하지 않고 STOP하는 예:

```text
Redis / Kafka / 관측 스택 도입
Lock / Lua 등 동시성 해법
Frequency Cap / Budget 완료 조건 변경
Event 저장을 Serving과 분리
Experiment 비교 가능성 변경
프로젝트 Scope / Phase 완료 조건 변경
DESIGN에 없는 API 추가
Phase 또는 여러 Task를 한 번에 구현
```

보고 형식:

```text
현재 문제
현재 측정 / 화면 상태
원인 가설
후보 A / B / C
장점 / 단점
추천안
예상 Trade-off
STOP
```

승인 전에 그 변경을 구현하지 않는다. 승인 직후 `docs/adr/`에 이유를 남긴다.

이 저장소에서 실제로 돈 예:

```text
T2-02  Frequency Cap 동시 초과 재현 → Human Gate → 후보 A → T2-03 + ADR 003
T3-02  Budget Overspend 재현      → Human Gate → GET 원자적 차감 → T3-03 + ADR 004
```

추측으로 Race를 예방하지 않는다. 재현 Task에서 한도 준수로 고치지 않는다.

### 4.4 그 외에 항상 사람 몫

```text
REQUIREMENTS / ROADMAP / TASKS의 최종 문장
화면으로 제품이 보이는지 (테스트 통과 ≠ 시연)
커밋과 푸시
Green이 재시도 한도를 넘겼을 때 계속 / 롤백 / 범위 축소
```

거버넌스 숫자(`.agent/config.yaml`):

```text
max_retry_limit        Green 실패 재시도. 넘으면 STOP + 롤백 후보 보고
max_tokens_per_task    Task당 토큰 상한
max_cost_usd           Task당 비용 상한
model_policy           단계별 모델 티어
runtime.mode           지금 저장소는 ide_assisted
```

---

## 5. Artifact — 왜 이 이름이고 어디에 있는가

### 5.1 이름

ML·데이터 파이프라인에서 artifact는 **한 단계가 다음 단계에 넘기는 검증된 파일**이다. 학습 로그 전체가 아니라 체크포인트다.

이 하네스도 같다. 채팅은 비결정적이고 길다. 다음 상태 노드에는 정형 파일만 넣는다. `docs/Poc.md`는 이를 Context Compaction이라고 부른다.

그래서 관리 경로 이름이 `.agent/artifacts/`다. 산출물, 작업 로그, 메모보다 **단계 증거**에 가깝다.

에이전트 설정(`.agent/config.yaml`)은 artifact가 아니다. 파이프라인을 켜는 거버넌스다.

### 5.2 디렉터리 규칙

```text
.agent/artifacts/_analysis.template.md
.agent/artifacts/_plan.template.md
.agent/artifacts/T3-04/analysis.md
.agent/artifacts/T3-04/plan.md
.agent/artifacts/T3-04/observation.md     (화면 확인 Task)
.agent/artifacts/T3-02/measurement.txt    (재현 Task)
.agent/artifacts/T2-02/human-gate.md      (후보 보고)
.agent/artifacts/T3-04/summary.md
```

폴더 이름은 TASKS의 Task ID와 같다. 새 대화가 열려도 `T3-04`만 있으면 그 Task의 합의를 복원할 수 있다.

### 5.3 파일별 역할

| 파일 | 누가 쓰나 | 다음 단계가 읽는가 | 사람이 할 일 |
|------|-----------|-------------------|--------------|
| `analysis.md` | Analysis | Plan | Valid인가. 문서 충돌인가. 범위가 한 Task인가 |
| `plan.md` | Plan | Red 이후 전부 | **승인 / 거절** |
| `measurement.txt` | 재현 Green | Human Gate | 숫자로 후보를 고를 근거인가 |
| `human-gate.md` | 재현 후 보고 | 다음 해법 Task | A / B / C 선택 |
| `observation.md` | 화면 Task | Summary | 브라우저에서 빠진 것을 보완할지 |
| `summary.md` | Summary | 다음 요청의 시작점 | 끝난 범위 확인. 다음 Task 허가 아님 |

`plan.md` 헤더의 `HITL: pending | approved | rejected`가 그 Task의 구현 허가 상태다.

---

## 6. 정본 문서 — 어디서 무엇을 확인하는가

에이전트는 채팅보다 아래를 우선한다. 사람과 에이전트가 같은 파일을 본다.

```text
docs/REQUIREMENTS.md   무엇을 만족해야 하는가 (FR)
docs/DESIGN.md         지금 코드와 맞는 구조. 없는 기술을 현재 구조처럼 쓰지 않음
docs/ROADMAP.md        어떤 문제를 어떤 순서로 볼 것인가
docs/TASKS.md          지금 포인터, Task 완료 조건, 이 Phase에서 하지 않는 것
docs/adr/              이미 고른 결정과 이유
docs/experiments/      측정 실험 (제품 Phase와 별개일 수 있음)
README.md              사람이 실행하고 시연하는 방법
.agent/config.yaml     런타임 / 예산 / 재시도
.agent/artifacts/      현재 Task 합의
```

문서와 코드가 충돌하면 에이전트가 임의로 맞추지 않는다. 어느 문서를 고칠지 사람에게 보고한다.

목표가 REQUIREMENTS / ROADMAP / README와 달라 보이면 구현하지 않는다.

제품 vs 테스트:

```text
Test         코드가 기대대로 동작하는가
Experiment   부하·동시성·중복에서 실제로 무엇이 일어나는가
화면         운영자·시청자가 같은 사실을 읽는가
```

코드를 썼다는 이유만으로 Task를 DONE 하지 않는다.

---

## 7. 피드백을 어디에 주는가

원칙: **고치고 싶은 층에 짧게 말한다.** 에이전트는 다음 턴에 TASKS와 artifact를 다시 연다.

### 7.1 Plan이 틀렸을 때

`plan.md`를 보고 채팅으로:

```text
거절
Redis 넣지 마라
완료 조건에서 Player E2E는 빼
Green은 dashboard.html과 CampaignStats만
```

구현 전에 끝나야 하는 피드백이다.

### 7.2 구현이 Plan과 다를 때

Summary 전후:

```text
spent는 보이는데 상태가 콘솔에만 있다. Dashboard 표를 확인해
T3-04 plan의 하지 않는 것에 새 엔드포인트가 있다. 경로를 되돌려
```

필요하면 사람이 `docs/TASKS.md` 완료 조건 한 줄을 직접 고친다. 다음 턴의 정본이 된다.

### 7.3 해법 기술을 고를 때

Human Gate 보고를 보고:

```text
A
후보 A. PostgreSQL 조건부 UPDATE. Redis는 다음 Phase
```

그다음 해법 Task의 Plan HITL이 한 번 더 있다. Gate에서 A를 골랐다고 Green이 즉시 시작되지는 않게 두는 것이 안전하다. (이 저장소는 Gate 선택 후 별도 Task + Plan 승인을 썼다.)

### 7.4 제품 순서가 틀렸을 때

채팅만으로 Scope를 넓히지 않는다. `docs/ROADMAP.md` / `REQUIREMENTS.md` / `TASKS.md`를 고치는 것이 피드백이다. 에이전트에게 “Phase 4 전부 구현”은 ADR 001과 충돌한다.

### 7.5 Git

```text
커밋
커밋 푸시
```

요청 전에는 커밋하지 않는다. 재현 커밋과 해법 커밋을 섞지 않는다.

---

## 8. 에이전트가 스스로 닫는 구간

Plan 승인 후 사람은 기다리면 된다. 여기서는 테스트 실패/통과가 게이트다.

```text
Red      새 테스트가 실패해야 한다. 통과하면 가짜 테스트로 보고하고 Red를 다시 한다
Green    그 실패만 없앤다. 신규 파일 create, 기존 파일 patch
Lint     동작 변경 없이 컴파일/린트
Refactor 테스트가 깨지면 되돌린다
Summary  그 Task만 문서 동기화. STOP
```

제외 기능은 요청에 없어도 넣지 않고 이유를 말한다. API·필드는 DESIGN / REQUIREMENTS에 있는 것만. 미래 확장용 인터페이스를 만들지 않는다.

---

## 9. 한 Task가 끝난 뒤

```text
summary.md     무엇이 끝났는가
TASKS 포인터   DONE, 다음은 개발자 요청 시
화면 / README  시연 경로가 코드와 같은가
```

다음 문장은 새 요청이다. 이전 대화 전체를 넣을 필요 없다.

---

## 10. 파일 지도

```text
HARNESS.md                                   이 설명 (사람용)
AGENTS.md                                    에이전트 한 장 요약
README.md                                    실행 / 시연 / 바이브 루프 한 줄
docs/Poc.md                                  상태 그래프 철학 (원문)
docs/REQUIREMENTS.md DESIGN.md ROADMAP.md TASKS.md
docs/adr/                                    결정
.cursor/rules/ad-flow.mdc                    항상 적용
.cursor/skills/ad-flow-vibe-coding/SKILL.md  Task 루프 절차
.agent/config.yaml                           거버넌스
.agent/artifacts/<task-id>/                  Task 증거
```

지금 제품을 돌리려면 README다. 지금 에이전트에게 일을 시키려면 `다음`이면 된다. 지금 승인을 검토하려면 `.agent/artifacts/<현재 Task>/plan.md`다.
