# 메디브러리 완전 무료 배포 결정 기록

## 결론

메디브러리의 포트폴리오 배포 구성은 **Vercel + Render Free Web Service + Supabase Free PostgreSQL**로 확정했습니다. 프론트엔드는 Vercel에서 정적 SPA로 제공하고, Spring Boot API는 Render의 Docker Web Service로 실행하며, 영속 데이터는 Supabase PostgreSQL에 저장합니다. 프로젝트는 이 구성을 위해 PostgreSQL JDBC 드라이버와 PostgreSQL 전용 Flyway 모듈, PostgreSQL 호환 마이그레이션 SQL로 전환되었습니다.

| 계층 | 선택한 서비스 | 선택 이유 | 유의 사항 |
|---|---|---|---|
| 프론트엔드 | Vercel | Vue/Vite SPA 배포와 GitHub 연동에 적합 | `VITE_` 변수는 공개됨 |
| 백엔드 | Render Free Web Service | JVM 앱을 Dockerfile로 배포 가능 | 유휴 시 중지와 첫 요청 지연 가능 |
| 데이터베이스 | Supabase Free PostgreSQL | PostgreSQL 관리형 DB와 TLS 접속 제공 | 유휴 프로젝트 일시 정지 가능 |
| 로컬 빠른 시연 | H2 `local` 프로필 | DB 설치 없이 샘플 검색 확인 | 영속 데이터·실제 외부 API 검증용이 아님 |

> Render의 컨테이너 로컬 파일시스템은 영속 DB로 사용하면 안 됩니다. 회원·즐겨찾기·캐시 데이터는 반드시 Supabase PostgreSQL에 저장합니다.[1]

## 대체안 검토 결과

초기에는 Render + TiDB, Oracle Cloud Always Free VM을 검토했습니다. 그러나 현재 PostgreSQL 전환 범위와 포트폴리오의 단순한 운영 목적을 고려해 외부 관리형 PostgreSQL을 사용하는 Supabase를 채택했습니다. VM을 직접 운영하는 방식은 방화벽, 보안 패치, 백업, 장애 대응 부담이 커서 본 과제의 우선순위와 맞지 않습니다.

| 대체안 | 미채택 사유 |
|---|---|
| Render + TiDB Cloud | 현재 PostgreSQL 기반 구현과 별도의 MySQL 호환성 검증이 필요 |
| Oracle Cloud VM + MySQL | VM·네트워크·보안 운영 부담과 가입 조건이 증가 |
| 로컬 H2만 사용 | 공개 배포 URL과 데이터 영속성 요구를 충족하지 못함 |

## 운영 수칙

Render에는 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `DATA_GO_KR_SERVICE_KEY`를 등록합니다. Vercel에는 `VITE_API_BASE_URL`만 등록합니다. Render Free 인스턴스와 Supabase Free 프로젝트의 유휴 동작 특성을 고려하여, 시연 직전 API 헬스체크와 Supabase Dashboard 상태를 확인합니다.[1] [2]

실제 설정 절차는 [`render-supabase-vercel-deployment-roadmap.md`](render-supabase-vercel-deployment-roadmap.md)와 [`portfolio-deployment-guide.md`](portfolio-deployment-guide.md)를 참조합니다.

## 참고 자료

[1] [Render Free Instances](https://render.com/docs/free)

[2] [Supabase Pricing](https://supabase.com/pricing)
