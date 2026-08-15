# 메디브러리 Supabase Free PostgreSQL 생성 안내

이 문서는 메디브러리의 운영 데이터베이스를 Supabase Free PostgreSQL로 만들고, Render Spring Boot 서비스에 필요한 연결 정보만 안전하게 확보하는 절차입니다. 프론트엔드에서 Supabase API를 직접 호출하지 않으므로 Supabase의 anon key, service role key, Project URL은 이번 단계에서 필요하지 않습니다.

> **중요:** DB 비밀번호와 완성된 연결 문자열은 이 채팅, GitHub, Vercel 환경변수에 붙여넣지 마세요. 생성 과정에서 직접 보관하고, 이후 Render Dashboard에만 입력합니다.

## 1. Free 프로젝트 생성

[Supabase Dashboard](https://supabase.com/dashboard)에 로그인한 뒤 **New project**를 선택합니다. 조직(Organization)을 고르고 아래 값을 입력합니다.

| 입력 항목 | 권장값 | 설명 |
|---|---|---|
| Organization | 본인의 Free 조직 | 개인 계정을 선택합니다. |
| Name | `medibrary-db` | 프로젝트 식별용 이름입니다. |
| Database Password | 32자 이상 임의 문자열 | `postgres` DB 사용자의 비밀번호입니다. 비밀번호 관리 도구에 보관합니다. |
| Region | Render와 가까운 아시아 리전 | 이 프로젝트의 Render Blueprint는 Singapore 리전을 사용하므로, 화면에서 Singapore가 보이면 우선 선택합니다. 없으면 가장 가까운 아시아 리전을 선택합니다. |
| Plan | Free | 결제 정보 없이 사용할 수 있는 Free 플랜을 선택합니다. |

**Create new project**를 누른 뒤 데이터베이스가 준비될 때까지 기다립니다. Free 플랜은 최대 2개의 활성 프로젝트와 프로젝트당 500 MB 데이터베이스를 제공하며, 1주 유휴 뒤 프로젝트가 일시 정지될 수 있습니다.[1] [2]

## 2. Render용 PostgreSQL 연결 정보 확인

프로젝트 Dashboard가 열리면 상단의 **Connect** 버튼을 누릅니다. 연결 방법 중 **Session pooler**를 선택합니다. Render에서 실행되는 Spring Boot는 지속 실행 백엔드이므로, Supabase 공식 문서가 IPv4 네트워크의 지속 연결에 권장하는 Session Pooler를 사용합니다.[1]

Connect 화면에서 아래 네 값을 확인합니다. 화면에 보이는 실제 값만 복사하고, 비밀번호가 포함된 전체 문자열은 외부에 공유하지 않습니다.

| 필요한 값 | Connect 화면의 위치 또는 예시 | Render에 등록할 변수 |
|---|---|---|
| Host | `aws-<region>.pooler.supabase.com` | `DB_URL`의 호스트 부분 |
| Port | `5432` | `DB_URL`의 포트 부분 |
| User | `postgres.<project-ref>` | `DB_USERNAME` |
| Password | 프로젝트 생성 때 설정한 DB 비밀번호 | `DB_PASSWORD` |
| Database | `postgres` | `DB_URL`의 DB 이름 |

Render에 등록할 JDBC URL은 아래 형식입니다. `<...>` 부분은 실제 Connect 화면의 값으로 바꾸되, 이 값 자체는 GitHub나 Vercel에 넣지 않습니다.

```text
DB_URL=jdbc:postgresql://aws-<region>.pooler.supabase.com:5432/postgres?sslmode=require
DB_USERNAME=postgres.<project-ref>
DB_PASSWORD=<프로젝트 생성 시 정한 DB 비밀번호>
```

> Session Pooler의 URL은 일반적으로 `postgresql://...`로 표시됩니다. Spring Boot는 PostgreSQL JDBC 드라이버를 사용하므로, `DB_URL`에는 맨 앞을 `jdbc:postgresql://`로 바꾼 URL을 등록합니다. 사용자명은 단순 `postgres`가 아니라 Connect 화면의 `postgres.<project-ref>` 형식을 그대로 사용합니다.

## 3. 선택 시 주의할 연결 방식

| Connect 방식 | 메디브러리에서의 사용 여부 | 이유 |
|---|---|---|
| **Session pooler, 5432** | 사용 | IPv4 네트워크의 지속 실행 Spring Boot 백엔드에 적합합니다.[1] |
| Direct connection, 5432 | 기본값으로 사용하지 않음 | Supabase Free에서 Direct connection은 IPv6 기반입니다. Render 네트워크 호환성을 별도로 확인하지 않았다면 선택하지 않습니다.[1] |
| Transaction pooler, 6543 | 사용하지 않음 | 서버리스·Edge 같은 짧은 연결에 적합하며 prepared statement 제약이 있습니다.[1] |

## 4. 다음 Render 단계에서 입력할 값

Supabase 프로젝트를 만들었다면 비밀번호를 보내지 말고 **“Supabase 프로젝트 생성 완료”**라고만 알려주세요. 그다음 Render Blueprint 생성 화면에서 아래 항목을 직접 입력하면 됩니다.

| Render 환경변수 | 입력값 |
|---|---|
| `DB_URL` | 위의 Session Pooler JDBC URL |
| `DB_USERNAME` | `postgres.<project-ref>` |
| `DB_PASSWORD` | Supabase DB 비밀번호 |
| `JWT_SECRET` | Render Blueprint 자동 생성값 또는 32바이트 이상 임의 문자열 |
| `DATA_GO_KR_SERVICE_KEY` | 보유 중인 공공데이터포털 서비스 키 |
| `OPENFDA_BASE_URL` | `https://api.fda.gov` |
| `CORS_ALLOWED_ORIGINS` | 초기에는 `http://localhost:5173`, Vercel 배포 후 실제 Vercel URL로 변경 |

Flyway는 Render 백엔드가 처음 기동될 때 `V1__initial_schema.sql`을 실행해 필요한 테이블·인덱스·외래키를 생성합니다. Supabase SQL Editor에서 별도 DDL을 먼저 실행할 필요는 없습니다.

## 참고 자료

[1] [Supabase: Connect to your database](https://supabase.com/docs/guides/database/connecting-to-postgres)

[2] [Supabase: Free Plan과 사용 한도](https://supabase.com/docs/guides/platform/billing-on-supabase)
