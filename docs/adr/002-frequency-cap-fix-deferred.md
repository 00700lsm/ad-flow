# ADR 002 - Frequency Cap 동시성 해법을 보류한다

## 문제

T2-02에서 동시 GET /ads 후 Impression이 frequencyCap을 넘는 것을 재현했다.
해법(선택 시점 원자적 소비 / Impression 한도 / DB lock, 저장소)을 지금 고를지.

## 현재 Context

```text
관련 Phase     2
관련 Experiment T2-02 measurement: requests=16 selected=16 impressions=16 cap=1 overflow=15
현재 코드      GET은 readOnly COUNT, INCR은 Impression INSERT
측정된 사실    Race는 있다. 해법 비교 구현은 없다
```

## 검토한 대안

```text
1. 후보 A  선택 시점에 카운터를 원자적으로 올린다 (저장소 미정)
2. 후보 B  Impression 기록에 조건부 INSERT / 유니크 한도
3. 후보 C  DB lock으로 GET+기록 구간을 직렬화
4. 지금은 고르지 않고 보류한다
```

## 선택

대안 4.

T2-02는 재현까지다. A/B/C와 Redis 여부는 개발자가 Human Gate를 다시 열 때 고른다.
보류를 해법 승인으로 해석하지 않는다.

## 결과

```text
하는 것      TASKS 포인터와 T2-02 산출물에 보류를 남긴다
하지 않는 것 Redis / Lock / Lua / 선택 시점 INCR 구현
남은 한계    FR-09 동시 한도는 아직 만족하지 않는다
```
