# Analysis

```text
Task: T4-01
Phase: 4
Date: 2026-09-08
```

## 요청

Phase 4를 연다. 첫 작업은 이벤트 적재가 느릴 때 GET /ads가 같이 느려지는지를 **재현**하는 것이다. Kafka·비동기 분리는 이 Task가 아니다.

## 근거 문서

```text
REQUIREMENTS  FR-11 이벤트 처리가 Serving과 분리, 대량 수신, Consumer 재처리
              Phase 완료 조건이지 T4-01이 아니다. Phase 1은 동기 저장 허용 (FR-06)
DESIGN        4절: Impression/Click은 같은 앱이 PostgreSQL에 동기 저장. Kafka 없음
              12.4: Kafka 파이프라인은 Phase 4 목표 그림. 현재 구조가 아님
              13.1: 서빙 경로 DB 병목은 측정 후 기술
ROADMAP       Phase 4 질문: 이벤트 적재가 느려지면 선택 API도 같이 느려지는가
              기술은 결과가 필요성을 말할 때만
TASKS         Phase 3 DONE. 다음: Phase 4는 개발자가 요청할 때 → 이번 요청
ADR           001: Phase 진행 ≠ FR-11 전체. Kafka는 Human Gate
```

문서 충돌:

```text
구현 차단 충돌 없음.
DESIGN 12.4 제목은 Kafka다. 4절과 ROADMAP은 측정 전 도입을 금지한다.
T4-01은 12.4를 구현하지 않는다. 결합이 있는지만 본다.
```

현재 코드(문서 4절과 일치):

```text
GET /ads 와 POST /events/* 는 같은 Spring 앱
둘 다 @Transactional 로 같은 DataSource (테스트는 H2 풀)
AdEventService.record 는 INSERT 후 반환. 큐 없음
GET /ads 는 findAll + Frequency Cap INCR + Budget tryCharge
MockMvc는 Tomcat 스레드풀을 쓰지 않음. 스레드 기아 결합은 이 테스트로 안 보임
연결 풀이 작으면 INSERT가 커넥션을 붙잡고 GET이 기다릴 수 있다
```

## 제약

```text
이 Phase에서 해도 되는 것
  이벤트 경로가 커넥션을 오래 잡을 때 GET /ads 대기를 테스트로 재현
  테스트에서만 풀 크기·지연을 준다 (프로덕션 sleep 프로퍼티 없음)
  대기 시간을 artifacts/T4-01/measurement.txt 에 남긴다
  Human Gate 전에 Kafka / 비동기 저장을 넣지 않는다

하면 안 되는 것
  Kafka / Redis / Consumer / 재처리 토픽
  @Async, 별도 스레드 큐로 Serving 분리 (해법)
  Frequency Cap / Budget 계약 변경
  Simulator / SSE / k6 / Prometheus
  DESIGN에 없는 이벤트 API
  FR-12 멱등 / FR-13 실시간 지표

영향 파일 후보
  테스트: ServingEventCouplingTest (Spy + 작은 Hikari 풀)
  (승인 후 Summary) TASKS T4-01, DESIGN 4절·12.4에 결합 재현만
```

## 하지 않는 이유

Kafka는 12.4 목표이지 현재 구조가 아니다. ROADMAP이 결합을 먼저 묻는다.
이벤트 INSERT를 빠르게 만들거나 큐에 넣으면 재현이 사라진다. T3-02가 한도 준수로 고치지 않은 것과 같다.
Tomcat 스레드 결합은 MockMvc로 안 보이므로 이 Task에서 서버를 띄워 k6 하지 않는다.

## Exit

```text
Valid: yes
다음: plan.md
```
