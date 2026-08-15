# 구현 검증 기록

## 프론트엔드 화면 점검

- 점검 일시: 2026-08-15 (KST)
- 점검 경로: `http://localhost:5173/search`
- 확인 결과: `SCR-SEARCH-001`의 공통 헤더, 메디브러리 브랜드, 약 이름/모양·색깔 검색 모드 탭, 검색 입력 영역, 주요 CTA, 서비스 안내 푸터가 정상적으로 표시되었다.
- 디자인 반영: 메인 테마색 `#73CBF1` 계열의 버튼과 선택 상태, 밝은 하늘색 배경, 둥근 검색 패널 및 카드 디자인을 확인했다.
- 검증 범위: 정적 화면 렌더링과 Vite 프로덕션 번들 생성까지 완료했다. 실제 검색·인증·외부 API 호출은 Supabase PostgreSQL과 공공데이터 서비스 키를 주입한 배포 환경에서 통합 확인이 필요하다.

## 리팩터링 후 프론트엔드 확인

2026-08-15(KST)에 `SCR-SEARCH-001`을 다시 점검했다. 검색 화면의 헤더, 검색 방식 탭, 입력 영역 및 주요 버튼은 정상적으로 렌더링되었으며, 빈 검색어로 검색 버튼을 눌렀을 때 `약 이름을 입력해 주세요.` 오류 메시지가 입력 영역 하단에 정상적으로 표시되었다. 이는 `useDrugSearch` composable로 분리한 입력 검증 및 상태 처리 로직이 화면과 정상 연결되어 있음을 확인한 결과다.

## H2 로컬 프로필 실행 확인

2026-08-15(KST)에 `SPRING_PROFILES_ACTIVE=local`로 백엔드를 실행했다. PostgreSQL 호환 모드의 H2 인메모리 DB(`jdbc:h2:mem:medibrary`)가 기동되었고, `/api/actuator/health`는 `{"status":"UP"}`을 반환했다. 로컬 초기 데이터로 적재되는 `타이레놀정 500밀리그램`을 `SCR-SEARCH-001`에서 검색한 결과 1건의 의약품 카드가 정상 표시되었다.

## 배포 전 회귀 검증

| 검증 항목 | 실행 명령 또는 방법 | 결과 |
|---|---|---|
| Spring Boot 테스트 | `cd backend && ./mvnw -q test` | 통과 |
| Spring Boot 패키징 | `cd backend && ./mvnw -q -DskipTests package` | 통과 |
| Vue 프로덕션 빌드 | `cd frontend && pnpm build` | 통과 |
| 실제 Supabase 마이그레이션 | Supabase 프로젝트 생성 후 Render 배포 로그 확인 | 사용자 환경 대기 |
| 실제 공공 API E2E | Render에 서비스 키 등록 후 검색·부작용·DUR 확인 | 사용자 환경 대기 |
