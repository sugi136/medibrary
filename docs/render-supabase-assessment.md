# Render + Supabase + Vercel 무료 배포 검토 결과

## 결론

**Render Free Web Service + Supabase Free PostgreSQL + Vercel**은 메디브러리의 포트폴리오 배포에 적합하며, 이 구성을 채택했습니다. PostgreSQL 전환 코드는 완료되었습니다. PostgreSQL JDBC 드라이버와 `flyway-database-postgresql` 모듈을 사용하고, `V1__initial_schema.sql`은 PostgreSQL의 identity·인덱스 문법에 맞춰 변환했습니다. JPA 도메인 모델과 DTO·서비스 계층의 구조는 유지했습니다.

| 계층 | 플랫폼 | 현재 상태 | 핵심 제약 |
|---|---|---|---|
| 프론트엔드 | Vercel | 배포 설정 완료, 프로덕션 빌드 검증 완료 | `VITE_API_BASE_URL`만 공개 변수로 사용 |
| 백엔드 | Render Free Web Service | Dockerfile·Blueprint 준비 완료 | 유휴 시 중지와 첫 요청 지연 가능 |
| DB | Supabase Free PostgreSQL | 코드 연결 준비 완료, 프로젝트 생성 대기 | 용량·활성 프로젝트·유휴 정책 확인 필요 |

## 완료된 전환 범위

| 대상 | 이전 구성 | 현재 구성 |
|---|---|---|
| JDBC 드라이버 | `mysql-connector-j` | PostgreSQL JDBC 드라이버 |
| Flyway 모듈 | `flyway-mysql` | `flyway-database-postgresql` |
| DB URL | `jdbc:mysql://...` | Supabase TLS PostgreSQL JDBC URL |
| 마이그레이션 | MySQL 전용 SQL | PostgreSQL identity·인덱스 문법 SQL |
| 로컬 Compose | MySQL 8 | PostgreSQL 16 |
| H2 테스트 모드 | MySQL 호환 모드 | PostgreSQL 호환 모드 |
| 배포 선언 | Railway 구성 | `render.yaml` Docker Blueprint |

## 남은 실제 계정 설정

사용자는 Supabase Free 프로젝트를 만들고 DB 연결 정보를 확보해야 합니다. 이후 Render Blueprint 가져오기 화면에서 DB·공공 API 비밀값을 입력하고, 생성된 Render URL을 Vercel의 `VITE_API_BASE_URL`에 설정합니다. 마지막으로 Vercel Production URL을 Render의 `CORS_ALLOWED_ORIGINS`에 반영하면 됩니다.

| 검증 항목 | 현재 상태 | 완료 기준 |
|---|---|---|
| Spring Boot 테스트 | 완료 | H2 PostgreSQL 호환 모드에서 통과 |
| Spring Boot 패키징 | 완료 | 실행 가능한 JAR 생성 |
| Vue 프로덕션 빌드 | 완료 | Vite `dist/` 생성 |
| Supabase Flyway 실행 | 사용자 환경 대기 | 테이블·인덱스·외래키 생성 성공 |
| Render 헬스체크 | 사용자 환경 대기 | `/api/actuator/health`가 `UP` 반환 |
| Vercel CORS 연결 | 사용자 환경 대기 | 브라우저에서 API 요청 성공 |

## 운영 제약

Render Free Web Service는 일정 시간 유휴 뒤 중지될 수 있으므로, 포트폴리오 시연 직전에 API 헬스체크를 한 번 호출하는 것이 좋습니다.[1] Supabase Free의 정확한 프로젝트 수·저장 용량·유휴 정책은 계정과 시점에 따라 달라질 수 있으므로, 생성 전에 Dashboard와 가격 정책을 다시 확인합니다.[2]

## 참고 자료

[1] [Render Free Instances](https://render.com/docs/free)

[2] [Supabase Pricing](https://supabase.com/pricing)
