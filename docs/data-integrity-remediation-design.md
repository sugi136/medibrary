# 데이터 정합성 보완 설계

## 1. 보완 범위

본 설계는 `REQ-F-006`, `REQ-F-010`, `REQ-F-012`, `REQ-F-009` 및 `SCR-DETAIL-001`, `SCR-DASH-001`의 구현 가능성을 보장하기 위한 정합성 보완이다. 운영 DB는 Supabase PostgreSQL이며, Flyway의 신규 마이그레이션으로 기존 데이터를 보존한다.

| 이슈 | 설계 결정 | 근거 |
|---|---|---|
| 제조사·한글 성분 누락 | `drugs.manufacturer`, `drugs.ingredient_kr` 추가 | 상세·검색 DTO 및 기본정보 화면 요구 충족 |
| 최근 검색어 무저장 | `search_history` 신규 테이블 추가 | 사용자별 최근 5건 대시보드 요구 충족 |
| DUR 상대 품목 FK 실패 | 상대 품목을 `drugs`에 stub upsert 후 정규화된 `dur_pairs` 저장 | FK 무결성 유지, 상대 품목 상세 조회·즐겨찾기 확장 가능 |
| DUR 캐시 제약 누락 | 정렬된 쌍의 UNIQUE, `fetched_at`, 조회 인덱스 추가 | 중복 캐시 제거와 만료 정책 기반 마련 |
| 상세 즐겨찾기 해제 불가 | `DELETE /favorites/drug/{drugId}` 추가 | 상세 화면이 이미 알고 있는 `drugId`만으로 해제 가능 |

## 2. 스키마 정책

### 2.1 drugs

`manufacturer`는 낱알식별 API의 `ENTP_NAME`에서 저장한다. `ingredient_kr`는 제품명 괄호 안 한글 성분명을 우선 추출하고, 데이터가 없으면 null을 유지한다. 상세 기본정보는 `ingredient_kr` → `ingredient_en` → 정보 없음 순으로 표시한다.

### 2.2 search_history

검색 이력은 로그인 사용자에 한해 저장한다. 대시보드 요구사항이 사용자별 최근 검색어이므로, 비로그인 검색을 영속화하지 않는다. 이름·모양·색상 검색 조건을 사람이 읽는 `query_text`로 저장하며, 사용자별 최신 5건을 조회한다.

### 2.3 dur_pairs

DUR API에서 상대 품목의 식별 코드와 이름이 반환되면, 상대 약이 `drugs` 테이블에 없더라도 `Drug(id, name)` stub을 먼저 upsert한다. 이후 두 `drug_id`를 사전순으로 정렬해 `drug_id_a < drug_id_b`의 단일 규칙으로 저장한다. `(drug_id_a, drug_id_b)` UNIQUE 인덱스와 `fetched_at`을 두며, 상세 조회 캐시는 TTL 내 결과를 사용하고 만료 시 외부 API를 다시 조회한다.

## 3. API 변경

| 경로 | 변경 | 관련 요구사항 |
|---|---|---|
| `GET /drugs/{drugId}` | 제조사·한글 주성분 반환 | REQ-F-006 |
| `POST /drugs/search` 흐름 | 인증 사용자 검색 이력 저장 | REQ-F-012 |
| `GET /dashboard/summary` | 최근 검색어 최대 5건 반환 | REQ-F-012 |
| `DELETE /favorites/drug/{drugId}` | 상세 화면의 즐겨찾기 해제 | REQ-F-010 |
| `GET /drugs/{drugId}/contraindications` | 정렬된 DUR 캐시를 우선 조회하고 만료 시 재수집 | REQ-F-009, REQ-N-001 |

## 4. 검증 기준

1. 검색 결과·상세 기본정보에서 제조사 및 한글 주성분이 API 데이터 존재 시 표시된다.
2. 로그인 사용자의 이름·모양·색상 검색 뒤 대시보드에 최신 5건이 역순으로 표시된다.
3. DUR 상대 품목이 기존 `drugs`에 없을 때 stub upsert 후 FK 오류 없이 `dur_pairs`에 저장된다.
4. 반대 순서의 동일 DUR 쌍은 정렬 규칙과 UNIQUE 제약으로 중복 저장되지 않는다.
5. 상세 화면의 하트 버튼이 등록·해제를 모두 `drugId`로 수행한다.
