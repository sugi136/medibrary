# 메디브러리(Medibrary)

메디브러리는 약 이름·모양·색깔로 의약품을 검색하고, 효능·복용 정보, 국내·해외 부작용 사례, 즐겨찾기, 병용금기(DUR) 확인을 지원하는 웹서비스입니다. 이 저장소는 확정된 요구사항 `REQ-F-001~012`, 비기능 요구사항 `REQ-N-001~007`, 화면 ID `SCR-XXX-XXX`를 기준으로 구현한 **Vue 3 + Spring Boot 3 + PostgreSQL** 통합 프로젝트입니다.

> 이 서비스의 정보는 참고용입니다. 실제 복용·병용 여부는 의사 또는 약사와 반드시 상담해야 합니다.

| 구분 | 구성 |
|---|---|
| 프론트엔드 | Vue 3, Vite, Vue Router 4, Pinia, Axios |
| 백엔드 | Java 21, Spring Boot 3.5, Spring Web, JPA, Spring Security, JWT, Swagger UI |
| 데이터베이스 | PostgreSQL, Flyway, Supabase PostgreSQL(운영) |
| 외부 연동 | 식약처 낱알식별/e약은요/DUR, openFDA `drug/event` |
| 프론트엔드 배포 | Vercel |
| 백엔드 배포 | Render Free Web Service |
| 기본 API 주소 | `http://localhost:8080/api` |
| 프론트엔드 주소 | `http://localhost:5173` |

## 포트폴리오 배포

프론트엔드는 Vercel, Spring Boot API는 Render, 운영 데이터베이스는 Supabase PostgreSQL에 배포하도록 전환했습니다. 전체 순서와 실제 계정 설정은 [`docs/render-supabase-vercel-deployment-roadmap.md`](docs/render-supabase-vercel-deployment-roadmap.md)를 확인하세요. Render Blueprint는 저장소 루트의 [`render.yaml`](render.yaml)에 포함되어 있습니다.

> Render Free Web Service는 15분 유휴 후 중지될 수 있고, Supabase Free 프로젝트는 1주 유휴 시 일시 정지될 수 있습니다. 포트폴리오 시연 전 API 헬스체크를 한 번 호출하고 Supabase Dashboard의 프로젝트 상태를 확인하세요.

## 구현 범위

현재 프로젝트에는 로그인과 회원가입, 이름·모양·색깔 기반 검색, 의약품 상세 탭, 국내·해외 부작용 조회, 즐겨찾기, 즐겨찾기 약물 간 DUR 검사, 대시보드 요약을 위한 화면·라우팅·API 골격이 포함됩니다. 상세 화면 `SCR-DETAIL-001`은 **기본정보 / 효능·복용정보 / 부작용 / 함께 먹으면 안 되는 약**의 네 개 탭으로 구성했으며, 부작용과 병용금기 정보는 해당 탭을 선택한 시점에 조회하도록 구현했습니다.

| 요구사항 영역 | 반영 위치 |
|---|---|
| 인증 및 JWT | `backend/config`, `backend/security/CurrentUserProvider`, `AuthService`, `SCR-AUTH-001`, `SCR-AUTH-002` |
| 약 검색·상세 | `DrugSearchService`, `DrugCacheService`, `DrugService`, `SCR-SEARCH-001`, `SCR-DETAIL-001` |
| 국내·해외 부작용 | `adapter/EyakClient`, `OpenFdaClient`, `SideEffectCacheService`, `SideEffectService` |
| 병용금기 | `adapter/DurClient`, `ContraindicationCacheService`, `DurService`, 상세 DUR 탭 및 `SCR-MY-001` 배너 |
| 즐겨찾기 | `FavoriteService`, `FavoriteController`, `SCR-MY-001` |
| 대시보드 | `DashboardService`, `DashboardController`, `SCR-DASH-001` |
| DB 마이그레이션 | `backend/src/main/resources/db/migration/V1__initial_schema.sql` |

## 로컬 PostgreSQL 실행

프로젝트 루트에서 아래 명령을 실행하면 PostgreSQL 16 컨테이너가 `5432` 포트에서 실행됩니다.

```bash
docker compose up -d
```

Spring Boot 실행 전 개발 환경변수를 설정합니다. 서비스 키는 소스 코드에 쓰지 말고 쉘 환경변수 또는 IDE 실행 구성에만 등록합니다. Spring Boot는 별도의 dotenv 라이브러리를 사용하지 않으므로 `.env` 파일 자체를 자동으로 읽지 않습니다.

```bash
export DB_URL='jdbc:postgresql://localhost:5432/medibrary'
export DB_USERNAME='postgres'
export DB_PASSWORD='postgres'
export JWT_SECRET='32바이트-이상의-충분히-긴-개발용-비밀문자열을-입력하세요'
export DATA_GO_KR_SERVICE_KEY='발급받은_공공데이터포털_서비스키'
```

다음 명령으로 백엔드를 시작합니다. Flyway가 PostgreSQL 스키마를 생성합니다.

```bash
cd backend
./mvnw spring-boot:run
```

백엔드 상태 확인은 `http://localhost:8080/api/actuator/health`, Swagger UI는 `http://localhost:8080/api/swagger-ui.html`에서 확인할 수 있습니다.

## PostgreSQL 없이 빠르게 실행하기

Docker나 PostgreSQL을 실행하지 않고 화면과 핵심 검색 흐름을 확인하려면 H2 인메모리 DB를 사용하는 `local` 프로필을 사용하세요. 이 프로필은 `타이레놀정 500밀리그램`, `이부프로펜정 200밀리그램`, `아스피린정 100밀리그램` 샘플 데이터를 자동으로 적재하며, 애플리케이션 종료 시 데이터도 사라집니다.

```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

> `local` 프로필은 공공데이터 서비스 키 없이도 검색·상세 화면을 확인하는 용도입니다. 운영 데이터 영속성과 실제 외부 API 연동은 Supabase/Render 환경에서 검증해야 합니다.

## 프론트엔드 실행

새 터미널에서 아래 명령을 실행합니다. 기본 API 주소는 `http://localhost:8080/api`이며, 다른 주소가 필요하면 `frontend/.env`에 `VITE_API_BASE_URL`을 설정합니다.

```bash
cd frontend
cp .env.example .env
pnpm install
pnpm dev
```

## Render와 Supabase 환경변수

Render에는 다음 값을 등록합니다. DB·JWT·공공데이터 서비스 키는 Render에만 입력하고 GitHub와 Vercel에는 등록하지 않습니다.

| 변수명 | 설정값 또는 출처 |
|---|---|
| `DB_URL` | Supabase Connect 화면에서 제공하는 TLS PostgreSQL JDBC URL |
| `DB_USERNAME` | Supabase PostgreSQL 사용자명 |
| `DB_PASSWORD` | Supabase DB 비밀번호 |
| `JWT_SECRET` | 32바이트 이상 임의 문자열 |
| `DATA_GO_KR_SERVICE_KEY` | 공공데이터포털 서비스 키 |
| `OPENFDA_BASE_URL` | `https://api.fda.gov` |
| `CORS_ALLOWED_ORIGINS` | `https://<your-project>.vercel.app` |

Vercel에는 오직 아래 공개 변수만 등록합니다.

```text
VITE_API_BASE_URL=https://<your-render-service>.onrender.com/api
```

`VITE_` 접두어 변수는 브라우저 번들에 포함되므로 `DB_PASSWORD`, `JWT_SECRET`, `DATA_GO_KR_SERVICE_KEY`를 절대 넣으면 안 됩니다.

## 외부 API 처리 방식

외부 연동은 `adapter` 계층으로 제한했고, `ExternalRestClientFactory`가 `app.external.timeout-millis` 설정을 모든 외부 API의 연결·응답 제한 시간에 일관되게 적용합니다. 부작용 조회는 `side_effect_cache`에서 최근 24시간 이내 캐시를 우선 확인하고, 캐시가 없을 때만 e약은요 또는 openFDA를 호출합니다. 외부 HTTP 호출은 DB 트랜잭션 밖에서 수행하며, 캐시 저장만 별도 짧은 쓰기 트랜잭션으로 처리합니다. 해외 부작용 조회는 `drugs.ingredient_en`의 영문 성분명이 없으면 정상 HTTP 응답 안에서 `available: false` 안내를 반환합니다. DUR 조회 역시 오류가 상세 화면 전체 오류로 전파되지 않도록 `available: false`와 사용자 안내 문구로 처리합니다.

> 공공데이터포털 API는 활용승인된 서비스의 실제 응답 필드명과 버전이 다를 수 있습니다. 구현된 어댑터는 명세의 기본 필드를 기준으로 작성되었으므로, 서비스 키 연결 후 실제 응답을 확인하여 매핑 키를 점검해야 합니다.

## 검증 결과

| 검증 항목 | 결과 |
|---|---|
| Spring Boot 단위·통합 테스트 | 통과 — `./mvnw -q test` (H2 PostgreSQL 호환 모드 사용) |
| Spring Boot 패키징 | 통과 — `./mvnw -q -DskipTests package` |
| H2 local 프로필 기동·샘플 검색 | 통과 — `타이레놀` 검색 결과 1건 확인 |
| Vue 프로덕션 빌드 | 통과 — `pnpm build` |
| `SCR-SEARCH-001` 정적·입력 검증 | 통과 — 검색 UI 렌더링 및 빈 검색어 오류 표시 확인 |
| Supabase 마이그레이션·실제 외부 API E2E | Supabase 프로젝트 생성 및 서비스 키 주입 후 확인 필요 |

## 설계 정합성 참고

기존 ERD에는 제조사와 최근 검색어를 저장하는 컬럼·테이블이 정의되어 있지 않습니다. 따라서 현재 초기 골격에서 제조사 값은 외부 API 캐시 확장 전까지 `null`로, 대시보드 최근 검색어는 빈 배열로 반환합니다. `REQ-F-010`의 즐겨찾기 해제 동작을 명확하게 표현하기 위해, 즐겨찾기 목록은 검색 결과용 `DrugSummary`와 구분된 `FavoriteDrugSummary` 응답 모델을 사용합니다. 이 변경은 `docs/03_API명세서.yaml` 사본에도 반영했습니다.

## 디렉터리 구성

```text
medibrary/
├── backend/                 # Spring Boot API
│   ├── Dockerfile            # Render Docker 이미지
│   ├── src/main/java/com/medibrary/api/
│   │   ├── adapter/         # 외부 API 격리
│   │   ├── config/          # JWT·Security·CORS
│   │   ├── exception/       # 전역 오류 응답 및 예외 처리
│   │   ├── security/        # 현재 인증 사용자 접근
│   │   ├── controller/      # REST 엔드포인트
│   │   ├── dto/             # 요청·응답 DTO
│   │   ├── entity/          # JPA 엔터티
│   │   ├── repository/      # Spring Data JPA
│   │   └── service/         # 비즈니스 로직, cache/search 하위 책임 분리
│   └── src/main/resources/db/migration/
├── frontend/                # Vue 3 UI 및 Vercel 설정
├── render.yaml              # Render Free Web Service Blueprint
├── docs/                    # 설계 산출물 사본, 검증 기록, 배포 로드맵
└── docker-compose.yml       # PostgreSQL 16 로컬 개발 환경
```
