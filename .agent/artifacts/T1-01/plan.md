# Plan

```text
Task: T1-01 ~ T1-09
Phase: 1
HITL: approved
```

## 완료 조건

```text
T1-01  Campaign 필드와 상태, ACTIVE/PAUSED 전이
T1-02  Creative를 캠페인에 붙이고 mediaUrl/clickUrl 저장
T1-03  POST/GET/PATCH /campaigns, POST /campaigns/{id}/creatives
T1-04  User / Content 샘플
T1-05  GET /ads 활성/기간/연령/장르/Priority
T1-06  POST /events/impression, /events/click
T1-07  GET /dashboard/campaigns/{id}, /dashboard/summary
T1-08  Console에서 생성 → Player에서 광고 → Click
T1-09  README Demo 경로, DESIGN을 Phase 1 코드에 맞춤
```

## Red Tests

```text
1. CampaignTest.create_storesRequiredFields
2. CampaignTest.pause_fromActive / activate_fromPaused
3. CampaignTest.reject_blankName / endBeforeStart
4. CampaignTest.isActiveAt_respectsStatusAndPeriod
5. CreativeTest.attachToCampaign
6. CampaignApiTest.create_list_get_patch_addCreative
7. AdSelectionTest.picksHighestPriorityMatching
8. AdSelectionTest.rejectsInactivePausedOutOfPeriodAgeGenre
9. AdEventApiTest.impressionAndClickIncreaseDashboard
```

## Green 최소 구현

```text
단일 Spring Boot 앱
PostgreSQL + Docker Compose
도메인: Campaign, Creative, User, Content, AdEvent
API: DESIGN 경로 (Simulator 제외)
시연: /console /player /dashboard-ui 정적 페이지
테스트: 도메인은 순수 단위, API는 MockMvc + H2
```

## 검증 명령

```text
./gradlew test
docker compose -f docker/docker-compose.yml up -d
./gradlew bootRun
브라우저: /console → /player → /dashboard-ui
```

## 하지 않는 것

```text
Redis Kafka SSE k6 Simulator Lock Lua
BUDGET_EXHAUSTED 자동 전환
Frequency Cap / Budget 필터
Next.js 별도 서버
```
