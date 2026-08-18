ALTER TABLE users
    ADD COLUMN terms_agreed_at TIMESTAMP NULL,
    ADD COLUMN medical_notice_agreed_at TIMESTAMP NULL;

COMMENT ON COLUMN users.terms_agreed_at IS '서비스 약관 동의 시각. 기존 계정은 동의 이력 미확보로 NULL을 허용한다.';
COMMENT ON COLUMN users.medical_notice_agreed_at IS '의료정보 참고용 고지 동의 시각. 기존 계정은 동의 이력 미확보로 NULL을 허용한다.';
