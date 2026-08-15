# 배포 상태 기록

- 2026-08-15: Vercel 프로젝트 `medibrary`를 `frontend` Root Directory와 Vite 프리셋으로 배포했다.
- Vercel 공개 운영 도메인 `https://medibrary-penguin16.vercel.app`는 외부 `HTTP 200` 응답을 확인했다.
- Render 서비스 `medibrary-api`의 `CORS_ALLOWED_ORIGINS` 편집값을 위 Vercel 공개 도메인으로 변경했으며, 아직 Render의 `Save, rebuild, and deploy` 실행이 남아 있다.
- Render API 헬스체크: `https://medibrary-api.onrender.com/api/actuator/health`.
- 이후 CORS 재배포 완료 후 브라우저 검색 요청으로 프론트엔드-백엔드 통합을 검증한다.
