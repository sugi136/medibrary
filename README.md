# 메디브러리(Medibrary)

**메디브러리**는 약 이름 또는 알약의 모양·색깔로 의약품을 찾고, 기본정보·효능·복용정보·국내/해외 부작용·DUR 안전정보를 확인할 수 있는 의약품 정보 웹 서비스입니다. 관심 약은 즐겨찾기에 저장할 수 있으며, 대시보드에서 최근 검색과 저장 약 조합의 안전 상태를 다시 확인할 수 있습니다.

> 이 서비스의 모든 의약품 정보는 참고용입니다. 실제 복용, 병용, 해외 의약품 구매 및 반입 전에는 반드시 의사 또는 약사와 상담해야 합니다.

| 구분 | 링크 |
|---|---|
| 운영 서비스 | [https://medibrary.vercel.app](https://medibrary.vercel.app) |
| API 서버 | [https://medibrary-api.onrender.com/api](https://medibrary-api.onrender.com/api) |
| API 문서 | [Swagger UI](https://medibrary-api.onrender.com/api/swagger-ui.html) |
| 저장소 | [https://github.com/sugi136/medibrary](https://github.com/sugi136/medibrary) |

## 핵심 기능

| 영역 | 구현 기능 | 관련 요구사항 |
|---|---|---|
| 회원 | 이메일 회원가입, 영문·숫자·기호 비밀번호 검증, 약관·의료정보 고지 필수 동의, JWT 로그인 | REQ-F-001, REQ-F-002, REQ-N-002, REQ-N-008 |
| 검색 | 제품명 부분 일치 검색 및 모양·색깔 조합 검색, 낱알 이미지, 전체 건수와 이전·다음 페이지 이동 | REQ-F-003~005 |
| 약 상세 | 기본정보, 효능·복용정보, 부작용, 병용 주의, 해외 동일 성분 제품의 5개 탭 | REQ-F-006, REQ-F-013~015 |
| 부작용 | 국내 허가사항과 openFDA FAERS 자발보고 상위 항목 비교, 해석 한계 고지 | REQ-F-007, REQ-F-008, REQ-N-008 |
| 복약 안전 | 병용금기·주의, 동일 성분·효능군 중복 가능성 안내 | REQ-F-009, REQ-F-014 |
| 개인화 | 즐겨찾기 등록·해제, 저장 약 목록과 안전 경고, 최근 검색·즐겨찾기·점검 요약 대시보드 | REQ-F-010~012 |

### 검색 페이지네이션

`GET /api/drugs/search`는 `page`와 `size`를 지원합니다. `page`는 0부터 시작하고 `size`는 1~50 범위이며 기본값은 20입니다. 응답에는 `totalCount`, `page`, `size`, `hasNext`, `items`가 포함됩니다. 따라서 한 번에 20건으로 잘리지 않고 결과를 이전·다음 페이지로 이동할 수 있습니다.

```text
GET /api/drugs/search?shape=원형&color=하양&page=0&size=20
```

외형 검색의 흰색 값은 서비스 전반에서 **하양**으로 표준화합니다. 과거 URL 또는 검색 이력에 `흰색`이 남아 있더라도 프런트엔드에서 하양으로 정규화하여 선택값이 비어 보이지 않도록 처리합니다.

## 기술 스택

| 계층 | 구성 |
|---|---|
| Frontend | Vue 3, Vite, Vue Router 4, Pinia, Axios |
| Backend | Java 21, Spring Boot 3.5, Spring Web, Spring Data JPA, Spring Security, JWT, springdoc-openapi |
| Database | PostgreSQL 17(Supabase 운영), Flyway, H2(local 프로필) |
| External API | 식약처 낱알식별정보, e약은요, DUR 품목정보, openFDA FAERS·NDC Directory |
| Deployment | Vercel(Frontend), Render Free Web Service(Backend), Supabase PostgreSQL(Database) |

## 구현 구조

외부 연동은 `adapter` 계층으로 격리하고, 서비스 계층은 검색·상세 보강·부작용·DUR·즐겨찾기·대시보드 책임으로 나눴습니다. `drugs` 테이블은 사용자가 검색하거나 상세 조회한 의약품을 품목기준코드 단위로 저장하는 마스터/캐시 역할을 합니다.

```text
Vue 3
  └─ Axios API Client
      └─ Spring Boot Controller
          └─ Service
              ├─ PostgreSQL Cache / Master
              └─ External API Adapters
                  ├─ 식약처 낱알식별·e약은요
                  ├─ HIRA DUR
                  └─ openFDA
```

검색은 우선 로컬 DB의 페이지 결과와 24시간 검색 메타데이터 캐시를 사용합니다. 처음 조회하거나 아직 저장되지 않은 다음 페이지가 필요할 때만 낱알식별 API를 호출하고, 조회 결과를 `drugs`에 저장합니다. 이 방식으로 같은 조건을 반복 검색할 때 외부 API 대기 시간을 줄입니다. 외부 API가 일시적으로 실패하면 이미 저장된 검색 결과를 우선 반환합니다.

## 로컬 실행

### 1. 백엔드

기본 개발 환경은 PostgreSQL을 사용합니다. `.env` 파일을 자동으로 읽지 않으므로, IDE 실행 환경 또는 셸 환경변수에 값을 등록해야 합니다.

```bash
export DB_URL='jdbc:postgresql://localhost:5432/medibrary'
export DB_USERNAME='postgres'
export DB_PASSWORD='postgres'
export JWT_SECRET='32바이트-이상의-충분히-긴-개발용-비밀문자열'
export DATA_GO_KR_SERVICE_KEY='공공데이터포털_서비스키'
export OPENFDA_BASE_URL='https://api.fda.gov'
export CORS_ALLOWED_ORIGINS='http://localhost:5173'

cd backend
./mvnw spring-boot:run
```

Docker로 PostgreSQL 16을 실행하려면 프로젝트 루트에서 아래 명령을 실행합니다.

```bash
docker compose up -d
```

빠르게 화면과 검색 흐름을 확인할 때는 H2 샘플 데이터를 사용하는 `local` 프로필을 실행할 수 있습니다.

```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

### 2. 프런트엔드

```bash
cd frontend
cp .env.example .env
pnpm install
pnpm dev
```

`frontend/.env`에는 공개 API 주소만 설정합니다.

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

## 운영 환경변수

| 변수 | 용도 | 등록 위치 |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Supabase PostgreSQL 연결 | Render |
| `JWT_SECRET`, `JWT_EXPIRATION_SECONDS` | JWT 서명·만료 시간 | Render |
| `DATA_GO_KR_SERVICE_KEY` | 식약처·DUR 공공데이터 API 서비스 키 | Render |
| `OPENFDA_BASE_URL` | openFDA API 기본 주소 | Render |
| `EXTERNAL_API_TIMEOUT_MILLIS` | 외부 API 연결·응답 제한 시간 | Render |
| `CORS_ALLOWED_ORIGINS` | 허용할 프런트엔드 출처 목록 | Render |
| `VITE_API_BASE_URL` | 공개 백엔드 API 주소 | Vercel |

`VITE_` 접두어 환경변수는 브라우저 번들에 포함됩니다. 따라서 DB 비밀번호, JWT 비밀키, 공공데이터 서비스 키는 Vercel에 등록하면 안 됩니다.

## 외부 데이터 해석 원칙

- **FAERS**는 자발보고 데이터입니다. 보고 건수는 실제 발생 빈도, 인과관계, 처방량 보정 결과를 의미하지 않습니다.
- **해외 동일 성분 제품**은 국내 약과 동일하거나 유사한 성분을 기준으로 찾은 미국 유통 제품의 참고 정보입니다. 함량·제형·허가사항이 국내 제품과 다를 수 있으므로 추천 또는 대체 처방으로 해석하지 않습니다.
- **DUR 결과**는 사용자가 저장한 약 조합을 바탕으로 확인하는 보조 정보입니다. 실제 복약 결정은 의료 전문가의 판단이 필요합니다.

## 검증

```bash
# Backend
cd backend
./mvnw -q test

# Frontend
cd frontend
pnpm lint
pnpm build
```

최신 변경 기준으로 백엔드 단위·통합 테스트, 프런트엔드 ESLint, Vite 프로덕션 빌드를 통과했습니다. 운영 Render Free 인스턴스는 유휴 상태에서 중지될 수 있으며, 첫 요청 시 콜드 스타트로 수십 초의 지연이 발생할 수 있습니다. 이는 무료 배포 환경의 제약이며, 애플리케이션 내부에서는 검색 결과·메타데이터 캐시와 부분 실패 폴백으로 반복 조회 시간을 줄입니다.

## 디렉터리 구성

```text
medibrary/
├── backend/
│   ├── src/main/java/com/medibrary/api/
│   │   ├── adapter/      # 공공데이터·openFDA 연동 격리
│   │   ├── controller/   # REST API 엔드포인트
│   │   ├── dto/          # 요청·응답 DTO
│   │   ├── entity/       # JPA 엔티티
│   │   ├── repository/   # 페이지 조회를 포함한 DB 접근
│   │   └── service/      # 검색·상세·안전점검·개인화 비즈니스 로직
│   └── src/main/resources/db/migration/
├── frontend/
│   └── src/
├── docs/                 # API 명세·프로젝트 기술서·검증 기록
├── docker-compose.yml
└── render.yaml
```

## 최신 변경 이력

| 일자 | 커밋 | 주요 변경 |
|---|---|---|
| 2026-08-19 | `77c0511` | 검색 페이지네이션, 하양 선택값 복원, `usageInfo` 표시 수정, 브라우저 탭 제목 변경, 검색 메타데이터 캐시 적용 |
| 2026-08-18 | `8b62748` | 즐겨찾기 목록 제조사 정보 매핑 수정 |
| 2026-08-18 | `f33663d` | 외형 검색의 조건 정규화·결과 후처리 필터 보강 |
| 2026-08-18 | `cb47b90` | Render 콜드 스타트 대기·오류 메시지 처리 보완 |

자세한 API 계약은 [`docs/03_API명세서.yaml`](docs/03_API명세서.yaml)에서 확인할 수 있습니다.
