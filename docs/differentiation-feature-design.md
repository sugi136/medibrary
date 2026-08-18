# 메디브러리 차별화 기능 최소 변경 설계

## 1. 설계 목표

본 설계는 기존 `SCR-DETAIL-001`의 **부작용** 및 **함께 먹으면 안 되는 약** 탭을 확장한다. 신규 화면을 만들거나 데이터베이스 스키마를 변경하지 않고, 기존 외부 연동과 의약품 상세 조회 흐름 안에서 국내 허가사항과 해외 FAERS 자발보고를 구분해 제공한다.

> FAERS의 보고 건수는 발생 빈도나 인과관계를 의미하지 않는다. 처방량으로 보정되지 않았으며, 하나의 보고에 여러 약물과 여러 반응이 함께 포함될 수 있다. 따라서 본 서비스는 이를 **안전 신호 탐색용 참고 정보**로만 표시한다.

## 2. 요구사항 변경

| ID | 요구사항명 | 최소 변경 내용 | 관련 화면 |
|---|---|---|---|
| REQ-F-007 | 국내 부작용 조회 | e약은요 허가사항의 국내 이상반응을 원문 기준으로 표시한다. | SCR-DETAIL-001 |
| REQ-F-008 | 해외 부작용 조회 | openFDA FAERS의 상위 10개 MedDRA 반응 용어와 보고 건수를 집계해 표시한다. | SCR-DETAIL-001 |
| REQ-F-013 | 국내·해외 이상반응 비교 | 해외 상위 반응에 국내 문서 언급 여부를 표시하고, 국내 문서 미확인 항목을 강조한다. | SCR-DETAIL-001 |
| REQ-F-014 | 중복 성분·효능군 복용 주의 | 즐겨찾기 약을 기준으로 동일 성분 중복을 탐지하고, DUR 효능군 중복 정보를 함께 표시한다. | SCR-DETAIL-001, SCR-MY-001 |
| REQ-N-004 | 데이터 매핑 예외처리 | 영문 성분명 매핑 실패, FAERS 무결과, 국내 문서 매칭 불가를 서로 구분해 안내한다. | SCR-DETAIL-001 |
| REQ-N-008 | FAERS 해석 투명성 | 부작용 비교 카드에 자발보고·비인과성·미보정 한계를 고정 문구로 표시한다. | SCR-DETAIL-001 |

## 3. 화면 변경: SCR-DETAIL-001

### 3.1 부작용 탭

| 영역 | 표시 내용 | 예외 처리 |
|---|---|---|
| 국내 허가사항 | e약은요에 기재된 이상반응 원문 | 데이터 없음 시 “국내 허가사항의 부작용 정보가 없습니다” |
| 해외 FAERS 상위 보고 | MedDRA 용어, 보고 건수, 국내 문서 언급 여부 | 영문 매핑 실패·무결과·연동 실패 상태를 분리 표기 |
| 비교 하이라이트 | 국내 문서에 대응 표현이 확인되지 않은 해외 상위 항목을 `국내 문서 미확인` 배지로 강조 | 기계적 단어 매칭 결과임을 명시 |
| 해석 유의사항 | “보고 건수 ≠ 발생 빈도”, “처방량 미보정”, “인과관계 확정 불가” | 항상 표시 |

### 3.2 DUR 탭

| 영역 | 표시 내용 | 데이터 원천 |
|---|---|---|
| 병용금기 | 기존 금기·주의 목록 | DUR 병용금기 API 및 캐시 |
| 동일 성분 중복 | 로그인 사용자의 즐겨찾기 약 중 영문 성분명이 일치하는 항목 | 기존 즐겨찾기 + 성분 매핑 데이터 |
| 효능군 중복 | 현재 약과 같은 DUR 효능군으로 조회되는 후보 약 | DUR `getEfcyDplctInfoList03` |

## 4. API 최소 변경

기존 `GET /api/drugs/{drugId}/side-effects` 응답의 `cases`를 객체 배열로 확장한다. 국내 데이터에는 `count`가 없고, 해외 FAERS 데이터에만 보고 건수를 채운다.

```json
{
  "domestic": {
    "available": true,
    "cases": [
      {"term": "구역, 구토", "count": null, "domesticMentioned": true}
    ],
    "message": null
  },
  "overseas": {
    "available": true,
    "cases": [
      {"term": "NAUSEA", "count": 26806, "domesticMentioned": true},
      {"term": "DRUG DEPENDENCE", "count": 51228, "domesticMentioned": false}
    ],
    "message": null
  },
  "disclaimer": "FAERS 자발보고 건수는 발생 빈도나 인과관계를 뜻하지 않습니다."
}
```

`GET /api/drugs/{drugId}/contraindications`에는 기존 병용금기 응답을 유지하고, `GET /api/drugs/{drugId}/duplicate-warnings`를 추가한다. 이 엔드포인트는 로그인 사용자의 즐겨찾기 목록과 DUR 효능군 중복 정보를 계산해 반환한다. 별도 테이블이나 마이그레이션은 필요하지 않다.

## 5. 비교 규칙

해외 MedDRA 용어와 국내 허가사항의 직접 비교는 언어가 달라 단순 문자열 비교로는 정확하지 않다. 따라서 `NAUSEA→구역`, `VOMITING→구토`, `DIZZINESS→어지러움`, `RASH→발진`, `FATIGUE→피로` 등 제한된 동의어 사전을 먼저 적용한다. 사전에 없는 용어는 `국내 문서 미확인`으로 표시하되, 이는 해당 반응이 국내 허가사항에 절대 존재하지 않는다는 의학적 판정이 아니라 **자동 비교 범위에서 확인되지 않았다는 UI 상태**로 한정한다.

## 6. 검증 기준

| 검증 항목 | 통과 기준 |
|---|---|
| FAERS 집계 | `count=patient.reaction.reactionmeddrapt.exact` 응답의 `term`, `count` 상위 10개 표시 |
| 국내·해외 비교 | 해외 각 항목에 국내 문서 언급 여부 배지 표시 |
| FAERS 유의 문구 | 비교 화면에 항상 노출 |
| 매핑 실패 | 해외 영역에 매핑 실패 안내, 페이지 정상 렌더링 |
| FAERS 무결과 | 오류 대신 “등록된 부작용 사례가 없습니다” 표시 |
| 동일 성분 중복 | 즐겨찾기 약 중 성분 일치 시 경고 표시 |
| 효능군 중복 | DUR API 응답 성공·무결과·실패를 구분 표시 |

## 7. 출처

1. [openFDA Drug Adverse Event API](https://open.fda.gov/apis/drug/event/)
2. [openFDA Query Syntax](https://open.fda.gov/apis/query-syntax/)
3. [식품의약품안전처 의약품안전사용서비스(DUR) 품목정보](https://data.go.kr/data/15059486/openapi.do)
