# 메디브러리 포트폴리오 배포 조사 노트

> **상태: 대체됨.** 이 문서는 초기 Railway + MySQL 검토 기록입니다. 현재 구현과 배포 기준은 **Supabase PostgreSQL + Render Free Web Service + Vercel**입니다. 실제 배포에는 [`render-supabase-vercel-deployment-roadmap.md`](render-supabase-vercel-deployment-roadmap.md)와 [`portfolio-deployment-guide.md`](portfolio-deployment-guide.md)를 사용하세요.

## 초기 조사에서 유지된 결론

Vue/Vite SPA와 Spring Boot API는 서로 다른 실행 환경에 배포하는 것이 적절합니다. Vercel은 프론트엔드를 제공하고, JVM 기반의 지속 실행 API는 Render에서 Docker 컨테이너로 실행합니다. 프론트엔드는 공개 API 주소만 보유하며, 데이터베이스 접속 정보·JWT 비밀값·공공데이터 서비스 키는 백엔드 환경변수에만 보관합니다.

| 계층 | 현재 확정 플랫폼 | 배포 단위 | 공개 환경변수 |
|---|---|---|---|
| 프론트엔드 | Vercel | `frontend/` Vite SPA | `VITE_API_BASE_URL`만 사용 |
| 백엔드 | Render Free Web Service | `backend/` Spring Boot Docker 서비스 | 없음 |
| 데이터베이스 | Supabase PostgreSQL | 외부 관리형 PostgreSQL | 없음 |

## Vercel 관련 확인

Vue Router history 모드에서 직접 URL 접근을 지원하기 위해 `frontend/vercel.json`의 SPA rewrite가 필요합니다. Vite에서 `VITE_` 접두어가 붙은 환경변수는 클라이언트 빌드에 노출되므로 `VITE_API_BASE_URL`만 넣고, JWT 비밀값·DB 비밀번호·공공데이터 서비스 키는 절대 넣으면 안 됩니다.

## 현재 환경변수 매핑

| 설정 위치 | 변수 | 값의 출처 |
|---|---|---|
| Render | `DB_URL` | Supabase Connect 화면의 TLS PostgreSQL JDBC URL |
| Render | `DB_USERNAME` | Supabase Connect 정보 |
| Render | `DB_PASSWORD` | Supabase 프로젝트 생성 시 설정한 비밀번호 |
| Render | `JWT_SECRET` | 32바이트 이상의 임의 문자열 또는 Blueprint 생성 값 |
| Render | `DATA_GO_KR_SERVICE_KEY` | 공공데이터포털 서비스 키 |
| Vercel | `VITE_API_BASE_URL` | Render 공개 URL 뒤의 `/api` |

## 참고 링크

[1] [Render Docker 배포 문서](https://render.com/docs/docker)

[2] [Render Blueprint YAML Reference](https://render.com/docs/blueprint-spec)

[3] [Vercel Vite 배포 문서](https://vercel.com/docs/frameworks/frontend/vite)
