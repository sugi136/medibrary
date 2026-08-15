# 메디브러리 무료 배포 전체 로드맵

## 목표 아키텍처

메디브러리는 Vercel에 Vue 3/Vite 정적 프론트엔드를, Render에 Spring Boot REST API를, Supabase에 PostgreSQL 데이터베이스를 배포한다. 프론트엔드는 공개 가능한 API 주소만 보유하고, DB 연결 정보·JWT 비밀값·공공데이터 서비스 키는 Render 백엔드 환경변수에만 둔다.

```text
사용자 브라우저
  └─ Vercel: Vue 3 / Vite SPA
       └─ HTTPS API 요청
            └─ Render: Spring Boot API
                 ├─ Supabase: PostgreSQL
                 ├─ 식약처 공공데이터 API
                 └─ openFDA API
```

| 구분 | 플랫폼 | 배포 대상 | 중요한 제약 |
|---|---|---|---|
| 프론트엔드 | Vercel | `frontend/` | `VITE_` 변수는 브라우저에 노출됨 |
| 백엔드 | Render Free Web Service | `backend/` | 15분 유휴 후 중지, 재기동 약 1분 |
| DB | Supabase Free | PostgreSQL 프로젝트 1개 | 500 MB, 1주 유휴 후 프로젝트 일시 정지 |

## 단계별 로드맵

| 단계 | 목표 | 담당 | 완료 기준 |
|---:|---|---|---|
| 0 | 배포 기준 확정 | 사용자 | 완료 — Render + Supabase + Vercel, Public GitHub 저장소 사용 확정 |
| 1 | GitHub 기준선 확정 | 공동 | 다음 작업 — 현재 소스·문서·배포 설정을 `main`에 게시 |
| 2 | PostgreSQL 전환 | AI | 완료 — 의존성·Flyway SQL·설정을 PostgreSQL 호환으로 변경 |
| 3 | 로컬 전환 검증 | AI | 진행 중 — Spring Boot 테스트·패키징 통과, Vue 빌드 재검증 예정 |
| 4 | Supabase 프로젝트 생성 | 사용자 | 대기 — Free PostgreSQL 프로젝트와 연결 정보 확보 |
| 5 | Render 백엔드 배포 | 공동 | Docker 기반 Spring Boot 서비스가 공개 URL에서 기동 |
| 6 | Render 비밀 환경변수 설정 | 사용자 | DB·JWT·공공 API 키가 코드 밖에서 주입됨 |
| 7 | DB 마이그레이션 및 API 검증 | 공동 | Flyway 테이블 생성, `/api/actuator/health`가 UP |
| 8 | Vercel 프론트엔드 배포 | 공동 | Vite SPA가 공개 URL에서 렌더링 |
| 9 | CORS·프론트 API 연결 | 공동 | Vercel 도메인에서 Render API 호출 성공 |
| 10 | 기능 E2E 검증 | 공동 | 회원가입, 로그인, 검색, 상세, 즐겨찾기, DUR 흐름 확인 |
| 11 | 포트폴리오 마감 | 공동 | README, 배포 URL, 제약사항, 시연 절차 문서화 |

## 단계 0–1. 배포 기준과 GitHub 저장소

GitHub 저장소 `sugi136/medibrary`는 이미 생성되어 있다. 다음 배포 작업 전에는 `.env`, `.env.local`, DB 비밀번호, JWT 비밀값, 공공데이터 서비스 키가 Git 추적 대상이 아닌지 다시 확인한 뒤 코드와 문서를 `main` 브랜치에 게시한다. Render와 Vercel은 이 저장소를 각각 연결해 자동 배포한다.

> **사용자 확인 필요:** 실제 GitHub push는 공개 게시 행위이므로, 커밋 목록과 비밀값 제외 상태를 확인한 뒤 진행한다.

## 단계 2. MySQL에서 PostgreSQL로 코드 전환

Supabase는 PostgreSQL이므로 백엔드의 DB 계층을 전환한다. JPA Entity, Controller, DTO, Service, Adapter 구조는 유지하며, DB 드라이버와 Flyway SQL만 PostgreSQL에 맞춘다.

| 변경 대상 | 현재 구성 | 전환 작업 |
|---|---|---|
| `pom.xml` | `mysql-connector-j`, `flyway-mysql` | PostgreSQL JDBC 드라이버와 Flyway PostgreSQL 모듈로 교체 |
| `V1__initial_schema.sql` | `AUTO_INCREMENT`, `ON UPDATE CURRENT_TIMESTAMP`, MySQL 인덱스 문법 | `GENERATED ... AS IDENTITY`, 갱신 트리거 또는 JPA 갱신 처리, PostgreSQL 인덱스로 변환 |
| 운영 DB URL | `jdbc:mysql://...` | Supabase Connect 화면의 TLS PostgreSQL JDBC URL 사용 |
| 환경 프로필 | MySQL 운영 기본 | `prod` 프로필 또는 Render 변수 기반 PostgreSQL 운영 설정 추가 |
| Docker 이미지 | Java 21 기반 | 그대로 유지, Render 환경의 `PORT`를 자동 수용 |

변환 시 Flyway 마이그레이션 파일은 이미 배포된 MySQL의 데이터 이관용이 아니라, **새 Supabase PostgreSQL에 처음 스키마를 생성하는 용도**로 다룬다. 기존 개발용 H2 `local` 프로필은 유지해 빠른 화면 시연에 사용한다.

## 단계 3. 로컬 검증

코드 전환이 끝나면 세 가지 검증을 수행한다. 첫째, H2 `local` 프로필에서 샘플 검색이 동작하는지 확인한다. 둘째, PostgreSQL 호환 SQL을 테스트 DB 또는 Supabase의 개발 프로젝트에 적용해 Flyway가 테이블을 만들 수 있는지 확인한다. 셋째, `./mvnw test`와 `pnpm build`가 통과해야 한다.

| 검증 항목 | 명령 또는 확인 위치 | 성공 기준 |
|---|---|---|
| Spring Boot 테스트 | `backend/./mvnw test` | 모든 테스트 통과 |
| Vue 빌드 | `frontend/pnpm build` | `dist/` 생성 및 오류 없음 |
| H2 시연 | `SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run` | 타이레놀 샘플 검색 가능 |
| PostgreSQL/Flyway | Supabase 개발 DB 연결 | 스키마·인덱스·외래키 생성 성공 |

## 단계 4. Supabase Free PostgreSQL 생성

사용자는 Supabase에 로그인한 뒤 **New project**로 Free PostgreSQL 프로젝트를 하나 생성한다. 지역은 Render와 가능한 가까운 지역을 선택하고, 강한 DB 비밀번호를 설정한다. 프로젝트가 준비되면 **Connect** 화면에서 PostgreSQL 연결 정보(host, database, user, password, port, SSL 요구사항)를 확인한다.

| Supabase에서 확보할 값 | Render에서 쓸 변수 |
|---|---|
| PostgreSQL JDBC 연결 주소 | `DB_URL` |
| 사용자명 | `DB_USERNAME` |
| DB 비밀번호 | `DB_PASSWORD` |

Supabase Free 프로젝트는 500 MB 용량과 2개 활성 프로젝트 한도를 가지며, 1주 유휴 후 프로젝트가 일시 정지될 수 있다.[1] 이 계정 정보는 GitHub·Vercel·소스 파일에 넣지 않는다.

## 단계 5. Render Free Spring Boot 배포

권장 방식은 저장소 루트의 [`render.yaml`](../render.yaml)을 이용하는 **Blueprint 배포**입니다. Render에서 **New → Blueprint**를 선택해 GitHub의 `sugi136/medibrary` 저장소와 `main` 브랜치를 연결합니다. Blueprint는 Dockerfile 위치를 `backend/Dockerfile`, Docker build context를 `backend`로 지정하고, Free Web Service·Singapore 리전·`/api/actuator/health` 헬스체크·커밋 자동 배포를 선언합니다.[4]

Blueprint를 처음 가져올 때 Render는 `sync: false`로 표시된 DB·공공 API 비밀값을 입력하도록 요청합니다. 아직 Vercel URL이 없으므로 `CORS_ALLOWED_ORIGINS`의 초기값은 `http://localhost:5173`이며, 프론트엔드 배포 후 반드시 실제 Vercel Production URL로 변경합니다. `JWT_SECRET`은 Blueprint가 안전한 난수 값으로 생성합니다.

| Render 설정 | Blueprint 값 또는 선택값 |
|---|---|
| Service Type | Web Service |
| Instance Type | Free |
| Repository | `sugi136/medibrary` |
| Runtime | Docker |
| Dockerfile | `backend/Dockerfile` |
| Docker Build Context | `backend` |
| Region | Singapore |
| Health Check Path | `/api/actuator/health` |
| Auto Deploy | `main` 브랜치 커밋 시 활성화 |

Blueprint 대신 Dashboard에서 수동으로 만들 경우에는 **New → Web Service**를 선택하고, Root Directory를 `backend`, Runtime을 Docker로 선택합니다. Dockerfile이 Root Directory 안에 있으므로 Dockerfile Path는 기본값을 유지할 수 있습니다. 현재 `backend/Dockerfile`은 Java 21 애플리케이션을 빌드해 실행하며 Render가 할당한 `PORT`를 Spring Boot가 수용합니다.[5]

Render Free Web Service는 15분 유휴 후 중지되며, 다음 요청에서 약 1분의 기동 지연이 있다. 또한 월 750 무료 인스턴스 시간이 적용된다.[2] 포트폴리오 첫 접속 지연은 README와 화면 안내에 명시한다.

## 단계 6. Render 환경변수와 공공 API 키 설정

Render 서비스의 **Environment**에서 비밀값을 등록한다. 값은 Render Dashboard에만 입력하고 화면 캡처, GitHub, Vercel, 커밋 메시지에는 남기지 않는다.

| 변수명 | 값의 출처 | 노출 여부 |
|---|---|---|
| `DB_URL` | Supabase PostgreSQL JDBC TLS 연결 문자열 | 비밀 |
| `DB_USERNAME` | Supabase Connect 정보 | 비밀 |
| `DB_PASSWORD` | Supabase 프로젝트 생성 시 설정한 비밀번호 | 비밀 |
| `JWT_SECRET` | 32바이트 이상 임의 문자열 | 비밀 |
| `DATA_GO_KR_SERVICE_KEY` | 사용자 보유 공공데이터포털 서비스 키 | 비밀 |
| `OPENFDA_BASE_URL` | `https://api.fda.gov` | 공개 가능 |
| `CORS_ALLOWED_ORIGINS` | Vercel 배포 완료 후의 Production URL | 공개 가능 |

`CORS_ALLOWED_ORIGINS`는 아직 Vercel URL이 없으므로 단계 8 뒤에 입력하거나 수정한다. Render에 환경변수를 바꾼 뒤에는 새 배포를 실행해 반영한다.

## 단계 7. 백엔드 및 DB 검증

Render 배포 로그에서 Flyway 마이그레이션 완료와 Spring Boot 기동을 확인한다. Render가 생성한 공개 URL을 `<RENDER_API_URL>`로 표기할 때 다음 주소가 성공해야 한다.

```text
https://<RENDER_API_URL>/api/actuator/health
```

그 다음 검색 API와 회원가입 API를 호출한다. 의약품 캐시·회원·즐겨찾기 데이터가 Render 재시작 뒤에도 남는지 확인해 Supabase 영속 연결을 검증한다. 공공데이터 키가 아직 준비되지 않았다면 샘플·캐시 기반 경로부터 검증하고, 실제 외부 API 테스트는 키 설정 뒤 수행한다.

## 단계 8. Vercel 프론트엔드 배포

Vercel에서 GitHub 저장소를 import한 뒤 Root Directory를 `frontend`로 설정한다. Vite 프리셋을 확인하고 `VITE_API_BASE_URL`을 Render API의 `/api`까지 포함한 URL로 등록한다.

```text
VITE_API_BASE_URL=https://<RENDER_API_URL>/api
```

Vercel에는 `VITE_API_BASE_URL` 외의 비밀값을 추가하지 않는다. `VITE_` 접두어 변수는 브라우저 번들에 포함되므로 `DB_PASSWORD`, `JWT_SECRET`, `DATA_GO_KR_SERVICE_KEY`를 넣으면 안 된다. `frontend/vercel.json`의 SPA rewrite가 Vue Router 직접 경로 접근을 처리한다.

## 단계 9. CORS와 프론트 API 연결

Vercel Production URL이 생성되면 Render의 `CORS_ALLOWED_ORIGINS`를 해당 URL로 설정한다.

```text
CORS_ALLOWED_ORIGINS=https://<your-project>.vercel.app
```

커스텀 도메인을 추가하면 쉼표로 구분해 추가할 수 있다. 환경변수를 적용한 뒤 Render 서비스를 재배포하고 Vercel 사이트에서 검색 요청이 CORS 오류 없이 처리되는지 확인한다.

## 단계 10. 배포 후 E2E 검증

| 기능 | 검증 시나리오 | 성공 기준 |
|---|---|---|
| 공개 화면 | `/search` 직접 접속 | Vercel SPA가 404 없이 표시 |
| 의약품 검색 | 이름·모양·색깔 조건 입력 | 결과 카드 또는 정상 빈 결과 표시 |
| 상세 탭 | 기본·복용·부작용·DUR 탭 전환 | 지연 조회와 오류 안내가 정상 동작 |
| 인증 | 신규 가입 후 로그인 | JWT 발급과 보호 화면 접근 성공 |
| 즐겨찾기 | 등록·해제 후 새로고침 | Supabase에 영속 저장 |
| 병용금기 | 2개 이상 즐겨찾기 | 정상 결과 또는 부분 실패 안내 표시 |
| 외부 API | 공공데이터 키 설정 후 검색 | 키 노출 없이 API 결과/캐시 처리 |

## 단계 11. 포트폴리오 마감과 운영 수칙

README에 Vercel 화면 URL, Render API 헬스체크 URL, 사용 기술, 무료 티어의 첫 요청 지연을 기록한다. Supabase Free 프로젝트는 1주 유휴 뒤 멈출 수 있으므로 시연 전에 Dashboard 상태를 확인한다. Render Free도 15분 뒤 잠들므로 시연 직전 헬스체크 URL을 한 번 열어 서버를 깨운다. 무료 플랜에는 자동 백업이 포함되지 않으므로, 중요한 데모 데이터는 주기적으로 SQL 파일로 내보내 보관한다.[1]

## 현재 시점의 권장 시작 순서

1. 완료된 **MySQL → PostgreSQL 전환 코드**와 Render Blueprint, README 변경을 로컬에서 검증한다.
2. 비밀값이 제외된 상태를 확인한 뒤 **GitHub `main` 브랜치에 전환 코드를 게시**한다.
3. 사용자가 **Supabase Free 프로젝트**를 만들고 DB 연결 정보만 별도로 보관한다.
4. Render에서 Blueprint를 생성하고 Supabase·JWT·공공 API 환경변수를 주입한다.
5. Render API가 정상 기동된 뒤 **Vercel**에 프론트엔드를 배포한다.
6. Vercel URL을 Render CORS 설정에 연결하고 전체 기능을 재검증한다.

## 참고 자료

[1] [Supabase Pricing](https://supabase.com/pricing)

[2] [Render Free Instances](https://render.com/docs/free)

[3] [Vercel Vite 배포 문서](https://vercel.com/docs/frameworks/frontend/vite)

[4] [Render Blueprint YAML Reference](https://render.com/docs/blueprint-spec)

[5] [Render Docker 배포 문서](https://render.com/docs/docker)
