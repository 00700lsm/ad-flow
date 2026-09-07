# Observation

```text
Task: T2-04
Date: 2026-09-07
```

브라우저 클릭은 이 세션에 자동화 도구가 없어 하지 못했다.
Player가 쓰는 것과 같은 경로를 현재 코드(mem, port 8081)에서 실행했다.

```text
Console와 동일 POST
  아이폰 캡시연  cap=1 priority=20  스포츠 20-39
  나이키 캡시연  cap=2 priority=10  스포츠 20-39

Player와 동일
  GET /ads?userId=1&contentId=1  후 Impression
  두 번 반복
```

결과:

```text
1번째  아이폰 캡시연
2번째  나이키 캡시연
player.html  이번 세션 노출 (#adHistory) 영역 있음
```

localhost:8080의 기존 mem 프로세스는 T2-03 이전 코드라 같은 고우선 광고가 두 번 나왔다. 현재 코드가 아니다.

확인하지 않은 것: 실제 브라우저에서 재생 버튼을 두 번 누른 화면.
