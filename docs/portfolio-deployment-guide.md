# 메디브러리 포트폴리오 배포 빠른 가이드

## 1. 확정 아키텍처

메디브러리는 **Vercel + Render Free Web Service + Supabase PostgreSQL**로 배포합니다. Vercel은 Vue/Vite SPA를 제공하고, Render는 Docker 기반 Spring Boot API를 실행하며, Supabase는 운영 PostgreSQL을 제공합니다. DB 비밀번호·JWT 비밀값·공공데이터 서비스 키는 Render에만 두고 Vercel에는 공개 API 주소만 등록합니다.[1] [2]

| 계층 | 플랫폼 | 저장소 경로 | 공개 여부 |
|---|---|---|---|
| 프론트엔드 | Vercel | `frontend/` | 공개 |
| REST API | Render Free Web Service | `backend/` | 공개 URL |
| 데이터베이스 | Supabase PostgreSQL | 외부 관리형 DB | Render에서만 접속 |

> **보안 원칙:** `VITE_`로 시작하는 Vercel 변수는 브라우저 번들에 포함됩니다. `VITE_API_BASE_URL`만 등록하고 `DB_PASSWORD`, `JWT_SECRET`, `DATA_GO_KR_SERVICE_KEY`를 절대 등록하지 않습니다.

## 2. GitHub 게시 전 확인

루트 `.gitignore`는 실제 `.env`와 `.env.*` 파일을 무시하고 `.env.example` 파일만 추적하도록 설정되어 있습니다. 다음 명령을 실행해 테스트와 프론트엔드 빌드를 확인한 뒤, 비밀값이 없는 변경만 `main` 브랜치에 게시합니다.

```bash
cd backend && ./mvnw -q test
cd ../frontend && pnpm build
cd ..
git status --short
git add .
git diff --cached --check
git commit -m "chore: configure Supabase Render Vercel deployment"
git remote add origin https://github.com/sugi136/medibrary.git
git push -u origin main
```

## 3. Supabase Free PostgreSQL 생성

Supabase Dashboard에서 **New project**를 만들고, Render와 가까운 리전을 선택합니다. 프로젝트가 준비되면 **Connect** 화면에서 PostgreSQL 호스트·포트·사용자명·비밀번호를 확인합니다. Render에는 Supabase가 제공하는 TLS 연결 값을 JDBC 형식으로 등록합니다.

| Render 변수 | 예시 형식 |
|---|---|
| `DB_URL` | `jdbc:postgresql://aws-<region>.pooler.supabase.com:5432/postgres?sslmode=require` |
| `DB_USERNAME` | `postgres.<project-ref>` (Connect의 Session Pooler 값) |
| `DB_PASSWORD` | 프로젝트 생성 시 설정한 DB 비밀번호 |

Render Free와 Supabase Free를 연결할 때는 프로젝트 상단 **Connect → Session pooler**에서 보이는 값을 기준으로 사용합니다. Supabase Free의 Direct connection은 IPv6 기반이므로, IPv4 환경의 지속 실행 백엔드에는 Session Pooler가 적합합니다.[4] 복사한 비밀번호와 연결 문자열은 소스 파일·GitHub·Vercel에 저장하지 않습니다.

## 4. Render API 배포

가장 간단한 방법은 저장소 루트의 `render.yaml`을 사용하는 Blueprint 배포입니다. Render에서 **New → Blueprint**를 선택한 뒤 `sugi136/medibrary` 저장소와 `main` 브랜치를 연결합니다. 이 설정은 `backend/Dockerfile`, `backend` 빌드 컨텍스트, Free 플랜, Singapore 리전, `/api/actuator/health` 헬스체크와 자동 배포를 선언합니다.[1]

| Render Blueprint 항목 | 설정값 |
|---|---|
| Service Type | Web Service |
| Runtime | Docker |
| Plan | Free |
| Dockerfile | `backend/Dockerfile` |
| Docker Build Context | `backend` |
| Health Check Path | `/api/actuator/health` |
| 자동 배포 | `main` 브랜치 커밋 시 |

Blueprint 가져오기 과정에서 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DATA_GO_KR_SERVICE_KEY`를 Render 화면에 입력합니다. `JWT_SECRET`은 Blueprint가 무작위 값을 생성하지만, 필요하면 32바이트 이상의 별도 임의 문자열로 교체할 수 있습니다. 초기 `CORS_ALLOWED_ORIGINS` 값은 로컬 개발 URL이므로 Vercel 배포 뒤 실제 Production URL로 변경합니다.

배포가 완료되면 아래 URL에서 `{"status":"UP"}` 응답을 확인합니다.

```text
https://<your-render-service>.onrender.com/api/actuator/health
```

## 5. Vercel SPA 배포

Vercel에서 GitHub 저장소를 import하고 **Root Directory**를 `frontend`로 지정합니다. Vite 프리셋을 유지하고 다음 Production 환경변수만 추가합니다.

```text
VITE_API_BASE_URL=https://<your-render-service>.onrender.com/api
```

배포 후 Vercel Production URL을 확인합니다. `frontend/vercel.json`의 rewrite 설정은 Vue Router history 모드에서 `/search`, `/drugs/<id>`, `/favorites` 직접 접근을 지원합니다.[3]

## 6. CORS 연결과 최종 확인

Render Environment에서 `CORS_ALLOWED_ORIGINS`를 실제 Vercel Production URL로 변경한 뒤 서비스 재배포를 실행합니다.

```text
CORS_ALLOWED_ORIGINS=https://<your-project>.vercel.app
```

| 최종 검증 항목 | 성공 기준 |
|---|---|
| API 헬스체크 | Render `/api/actuator/health`가 `UP` 응답 |
| SPA 직접 접근 | Vercel `/search`, `/drugs/<id>`, `/favorites`가 404 없이 표시 |
| CORS | Vercel 도메인에서 API 요청이 차단되지 않음 |
| 데이터 영속성 | 회원가입·즐겨찾기 데이터가 Render 재시작 뒤에도 유지 |
| 비밀값 | Vercel·GitHub에 DB/JWT/공공 API 키가 없음 |

> Render Free 서비스는 일정 시간 유휴 뒤 중지될 수 있습니다. 포트폴리오 시연 전에 헬스체크 URL을 열어 API를 깨우고, 최초 응답 지연 가능성을 안내합니다.[2]

## 참고 자료

[1] [Render Blueprint YAML Reference](https://render.com/docs/blueprint-spec)

[2] [Render Free Instances](https://render.com/docs/free)

[3] [Vercel Vite 배포 문서](https://vercel.com/docs/frameworks/frontend/vite)

[4] [Supabase PostgreSQL 연결 방식](https://supabase.com/docs/guides/database/connecting-to-postgres)
