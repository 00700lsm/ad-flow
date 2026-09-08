# Summary

```text
Task: T3-04
Phase: 3
```

## 결과

Dashboard JSON·화면에 사용 예산 / 잔여 / 상태가 있다. budget=1 고우선 GET 1회 후 `BUDGET_EXHAUSTED`, 다음 GET은 저우선. Redis / 새 경로 / 차감 SQL 변경 없음.

관찰: 1번째 아이폰 예산시연, 2번째 나이키 예산시연. Dashboard high spent=1 remaining=0 BUDGET_EXHAUSTED.

## 검증

```text
Red: DashboardPageTest·AdEventDashboardApiTest 신규 2건 실패 확인
Green: ./gradlew test 통과
수동: mem :8081에서 GET /ads 2회 + Dashboard JSON
```

브라우저 재생 버튼은 이 환경에서 누르지 못했다. 동일 HTTP 경로만 확인했다.

## 문서

TASKS T3-04 DONE. Phase 3 완료 체크. DESIGN 4절·12.3에 Dashboard 확인. README 시연 경로.

다음 Phase는 개발자 요청 시에만.
