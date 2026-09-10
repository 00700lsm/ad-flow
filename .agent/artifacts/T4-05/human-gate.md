# Human Gate

```text
Task: T4-05
상태: 후보 A 선택 (2026-09-10). ADR 006 유지
```

## 현재 문제

워커가 큐에서 take한 뒤 INSERT 전에 죽으면, 워커를 다시 켜도 그 이벤트는 집계에 안 남는다. 재처리할 정본이 없다.

## 현재 측정

```text
accepted=1
afterCrashPersisted=0
replayed=0
persistDelayMs=400
take 이후 delay 중 interrupt. 같은 JVM에서 워커만 재시작. 프로세스 kill이 아님
```

## 원인 가설

정본이 힙 큐와 in-flight 한 건뿐이다. take가 큐에서 빼면 디스크·토픽에 남은 것이 없다.

## 후보

```text
A  감수. ADR 006 유지. JVM 큐
B  같은 PostgreSQL에 먼저 씀 (Outbox / 선 INSERT)
C  Kafka 등 브로커 (ADR 006에서 데모 미선택. 이 재현만으로 안 고름)
```

## 장점 / 단점

```text
A  추가 인프라 없음. FR-11 재처리는 미충족
B  이미 쓰는 DB. POST가 다시 커넥션을 씀 (T4-01과 긴장)
C  프로세스 밖 재처리. 브로커·Consumer 필요. ROADMAP은 측정 없는 도입을 금지
```

## 추천안

후보 A. T4-03·ADR 006과 같은 한계의 장애 버전이다. B/C는 유실이 제품 문제일 때.

## 예상 Trade-off

B/C를 고르면 FR-11 재처리에 가까워진다. 멱등(FR-12)은 그다음이다.

개발자 선택 A. Outbox·Kafka 없음. FR-11 재처리는 미충족.
