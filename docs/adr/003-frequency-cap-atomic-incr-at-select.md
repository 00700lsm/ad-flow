# ADR 003 - Frequency Cap은 선택 시점 원자적 INCR로 막는다

## 문제

동시 GET /ads 후 Impression이 `frequencyCap`을 넘는다.
T2-02 측정: requests=16 selected=16 impressions=16 cap=1 overflow=15.
Human Gate 후보 A/B/C와 저장소를 고른다.

## 현재 Context

```text
관련 Phase      2
관련 Experiment T2-02 measurement.txt
현재 코드       GET은 AdEvent IMPRESSION COUNT (readOnly)
                INCR는 POST /events/impression INSERT
측정된 사실     Race는 있다. 해법 코드는 없다
이전 결정       ADR 002: 후보는 보류. 보류 ≠ 해법 승인
개발자 선택     후보 A
```

## 검토한 대안

```text
1. 후보 A  선택 시점에 카운터를 원자적으로 올린다
2. 후보 B  Impression 기록에 조건부 INSERT / 유니크 한도
3. 후보 C  DB lock으로 GET+기록 구간을 직렬화
4. Redis / Lua (DESIGN 12.2 개선 후보, 이번 선택 아님)
5. 계속 보류 (ADR 002)
```

## 선택

대안 1. 저장소는 PostgreSQL.

카운터는 AdEvent가 아니라 당일(userId, campaignId, UTC day) 행이다.
`GET /ads`가 후보를 고를 때 `count < cap`인 행만 원자적으로 +1 한다.
실패하면 다음 후보를 시도한다. `frequencyCap = 0`은 올리지 않는다.

PostgreSQL을 고른 이유: 이미 Serving이 쓰는 저장소다. Redis는 Human Gate에서 고르지 않았다.
Impression 한도(B)나 GET+이벤트 구간 lock(C)은 고르지 않았다.

측정하지 않았으므로 이 ADR만으로 NFR-06을 달성했다고 쓰지 않는다.

## 결과

```text
하는 것      T2-03에서 선택 시점 원자적 INCR (PostgreSQL)
             ADR 002의 보류를 이 결정으로 대체한다
하지 않는 것 Redis / Lua / 후보 B / 후보 C
             Impression을 캡 카운터로 유지
남은 한계    GET만 하고 Impression이 없어도 슬롯을 쓴다
             Player 확인은 Phase 2 완료 항목, T2-03이 아님
```
