ALTER TABLE drugs
    ADD COLUMN atc_code VARCHAR(10);

CREATE INDEX idx_drugs_atc_code
    ON drugs (atc_code);
