ALTER TABLE user_word_status
    ADD COLUMN studied BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN memory_stage INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN memory_score DOUBLE PRECISION NOT NULL DEFAULT 0,
    ADD COLUMN correct_count INTEGER NOT NULL DEFAULT 0;

UPDATE user_word_status
SET studied = CASE
        WHEN study_status <> 'NEW' OR review_count > 0 OR last_reviewed_at IS NOT NULL THEN TRUE
        ELSE FALSE
    END,
    memory_stage = CASE study_status
        WHEN 'MASTERED' THEN 6
        WHEN 'LEARNING' THEN 2
        WHEN 'REVIEW_NEEDED' THEN 1
        ELSE 1
    END,
    memory_score = CASE study_status
        WHEN 'MASTERED' THEN 90
        WHEN 'LEARNING' THEN 75
        WHEN 'REVIEW_NEEDED' THEN 45
        ELSE 0
    END,
    correct_count = GREATEST(review_count - wrong_count, 0);

UPDATE user_word_status
SET last_reviewed_at = COALESCE(last_reviewed_at, updated_at, created_at)
WHERE studied = TRUE;

UPDATE user_word_status
SET next_review_at = COALESCE(next_review_at, last_reviewed_at + INTERVAL '1 day')
WHERE studied = TRUE;

CREATE INDEX idx_user_word_status_review_queue
    ON user_word_status (user_id, studied, next_review_at, memory_stage);
